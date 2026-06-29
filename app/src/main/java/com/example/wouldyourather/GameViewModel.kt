package com.example.wouldyourather

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val db = FirebaseFirestore.getInstance()
    private val localDb = AppDatabase.getDatabase(application)
    private val dataStoreManager = DataStoreManager(application)
    
    private val _questions = mutableStateOf<List<FirestoreQuestion>>(emptyList())
    val questions: State<List<FirestoreQuestion>> = _questions

    private val _currentQuestion = mutableStateOf<FirestoreQuestion?>(null)
    val currentQuestion: State<FirestoreQuestion?> = _currentQuestion

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private val _history = mutableStateOf<List<HistoryEntry>>(emptyList())
    val history: State<List<HistoryEntry>> = _history

    private val _isGameOver = mutableStateOf(false)
    val isGameOver: State<Boolean> = _isGameOver

    private var answeredQuestionIds = emptySet<String>()
    private var snapshotListener: ListenerRegistration? = null

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            // 1. Load answered IDs
            try {
                answeredQuestionIds = dataStoreManager.answeredQuestionIds.first()
            } catch (e: Exception) {
                answeredQuestionIds = emptySet()
            }
            
            // 2. Load from Room (Local cache)
            try {
                val localQuestions = localDb.questionDao().getAllQuestions().first()
                if (localQuestions.isNotEmpty()) {
                    _questions.value = localQuestions.map { 
                        FirestoreQuestion(
                            questionId = it.questionId,
                            question = it.question,
                            optionA = it.optionA,
                            optionB = it.optionB,
                            colorA = it.colorA,
                            colorB = it.colorB,
                            votesA = it.votesA,
                            votesB = it.votesB,
                            totalVotes = it.totalVotes
                        )
                    }
                    loadNextQuestion()
                    _isLoading.value = false
                }
            } catch (e: Exception) {}
            
            // 3. Start fetching from Firebase
            fetchQuestions()
        }
    }

    private fun fetchQuestions() {
        snapshotListener?.remove()
        snapshotListener = db.collection("questions")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    viewModelScope.launch {
                        val fetchedQuestions = withContext(Dispatchers.Default) {
                            snapshot.documents.mapNotNull { doc ->
                                try {
                                    doc.toObject(FirestoreQuestion::class.java)
                                } catch (e: Exception) {
                                    null
                                }
                            }
                        }

                        _questions.value = fetchedQuestions
                        
                        // Cache to Room
                        withContext(Dispatchers.IO) {
                            val localList = fetchedQuestions.map {
                                LocalQuestion(
                                    questionId = it.questionId ?: "",
                                    question = it.question,
                                    optionA = it.optionA,
                                    optionB = it.optionB,
                                    colorA = it.colorA,
                                    colorB = it.colorB,
                                    votesA = it.votesA,
                                    votesB = it.votesB,
                                    totalVotes = it.totalVotes
                                )
                            }
                            localDb.questionDao().insertAll(localList)
                        }

                        if (_currentQuestion.value == null && !_isGameOver.value) {
                            loadNextQuestion()
                        }
                        _isLoading.value = false
                    }
                } else {
                    _isLoading.value = false
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        snapshotListener?.remove()
    }

    fun loadNextQuestion() {
        val available = _questions.value.filter { it.questionId != null && it.questionId !in answeredQuestionIds }
        if (available.isNotEmpty()) {
            _currentQuestion.value = available.shuffled().first()
            _isGameOver.value = false
        } else if (_questions.value.isNotEmpty()) {
            _isGameOver.value = true
            _currentQuestion.value = null
        }
    }

    fun restartGame() {
        answeredQuestionIds = emptySet()
        _history.value = emptyList()
        _isGameOver.value = false
        
        loadNextQuestion()
        
        viewModelScope.launch {
            dataStoreManager.clearAllAnswers()
        }
    }

    suspend fun submitVote(question: FirestoreQuestion, option: String): Boolean {
        val qId = question.questionId ?: return false
        
        // Optimistically update the local state
        val oldQuestions = _questions.value
        val updatedQuestions = oldQuestions.map {
            if (it.questionId == qId) {
                val newVotesA = if (option == "A") it.votesA + 1 else it.votesA
                val newVotesB = if (option == "B") it.votesB + 1 else it.votesB
                it.copy(
                    votesA = newVotesA,
                    votesB = newVotesB,
                    totalVotes = it.totalVotes + 1
                )
            } else it
        }
        _questions.value = updatedQuestions

        return try {
            val docRef = db.collection("questions").document(qId)
            var stats = Triple(0L, 0L, 0L)

            db.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val currentVotesA = snapshot.getLong("votesA") ?: 0
                val currentVotesB = snapshot.getLong("votesB") ?: 0
                val currentTotal = snapshot.getLong("totalVotes") ?: 0

                val nextA = if (option == "A") currentVotesA + 1 else currentVotesA
                val nextB = if (option == "B") currentVotesB + 1 else currentVotesB
                val nextTotal = currentTotal + 1

                stats = Triple(nextA, nextB, nextTotal)

                if (option == "A") {
                    transaction.update(docRef, "votesA", nextA)
                } else {
                    transaction.update(docRef, "votesB", nextB)
                }
                transaction.update(docRef, "totalVotes", nextTotal)
            }.await()
            
            val entry = HistoryEntry(
                question = question,
                chosen = option,
                percentageA = if (stats.third > 0) ((stats.first.toFloat() / stats.third) * 100).toInt() else 0,
                percentageB = if (stats.third > 0) ((stats.second.toFloat() / stats.third) * 100).toInt() else 0
            )
            _history.value = _history.value + entry

            answeredQuestionIds = answeredQuestionIds + qId
            dataStoreManager.saveAnsweredQuestion(qId)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            _questions.value = oldQuestions
            false
        }
    }
}
