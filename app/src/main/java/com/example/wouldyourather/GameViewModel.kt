package com.example.wouldyourather

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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

    private var answeredQuestionIds = emptySet<String>()
    private var snapshotListener: ListenerRegistration? = null

    init {
        // Load answered IDs from DataStore
        viewModelScope.launch {
            dataStoreManager.answeredQuestionIds.collectLatest { ids ->
                answeredQuestionIds = ids
            }
        }
        
        // Load from Room first for immediate offline availability
        viewModelScope.launch {
            localDb.questionDao().getAllQuestions().collectLatest { localQuestions ->
                if (localQuestions.isNotEmpty()) {
                    val mapped = localQuestions.map { 
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
                    _questions.value = mapped
                    if (_currentQuestion.value == null) {
                        loadNextQuestion()
                    }
                }
            }
        }

        fetchQuestions()
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
                    val fetchedQuestions = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(FirestoreQuestion::class.java)
                        } catch (e: Exception) {
                            null
                        }
                    }

                    _questions.value = fetchedQuestions
                    
                    // Cache to Room
                    viewModelScope.launch {
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

                    if (_currentQuestion.value == null) {
                        loadNextQuestion()
                    }
                }
                _isLoading.value = false
            }
    }

    override fun onCleared() {
        super.onCleared()
        snapshotListener?.remove()
    }

    fun loadNextQuestion() {
        val available = _questions.value.filter { it.questionId !in answeredQuestionIds }
        if (available.isNotEmpty()) {
            _currentQuestion.value = available.shuffled().first()
        } else if (_questions.value.isNotEmpty()) {
            _currentQuestion.value = _questions.value.shuffled().first()
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
            db.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val currentVotesA = snapshot.getLong("votesA") ?: 0
                val currentVotesB = snapshot.getLong("votesB") ?: 0
                val currentTotal = snapshot.getLong("totalVotes") ?: 0

                if (option == "A") {
                    transaction.update(docRef, "votesA", currentVotesA + 1)
                } else {
                    transaction.update(docRef, "votesB", currentVotesB + 1)
                }
                transaction.update(docRef, "totalVotes", currentTotal + 1)
            }.await()
            
            // Persist "answered" status to DataStore
            dataStoreManager.saveAnsweredQuestion(qId)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            _questions.value = oldQuestions
            false
        }
    }
}
