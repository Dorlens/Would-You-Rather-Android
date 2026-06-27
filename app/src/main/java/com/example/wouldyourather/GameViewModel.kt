package com.example.wouldyourather

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class GameViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _questions = mutableStateOf<List<FirestoreQuestion>>(emptyList())
    val questions: State<List<FirestoreQuestion>> = _questions

    private val _currentQuestion = mutableStateOf<FirestoreQuestion?>(null)
    val currentQuestion: State<FirestoreQuestion?> = _currentQuestion

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private val answeredQuestionIds = mutableSetOf<String>()
    private var snapshotListener: ListenerRegistration? = null

    init {
        fetchQuestions()
    }

    private fun fetchQuestions() {
        snapshotListener?.remove()
        snapshotListener = db.collection("questions")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("FirestoreDebug: Fetch error: ${error.message}")
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    println("FirestoreDebug: Fetched ${snapshot.size()} documents")

                    val fetchedQuestions = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(FirestoreQuestion::class.java)
                        } catch (e: Exception) {
                            println("FirestoreDebug: Error mapping document ${doc.id}: ${e.message}")
                            null
                        }
                    }

                    println("FirestoreDebug: Successfully mapped ${fetchedQuestions.size} questions")

                    _questions.value = fetchedQuestions
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
            // Optional: Reset if all questions are answered
            answeredQuestionIds.clear()
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
            answeredQuestionIds.add(qId)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            // Revert optimistic update on failure
            _questions.value = oldQuestions
            false
        }
    }
}
