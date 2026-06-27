package com.example.wouldyourather

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    init {
        fetchQuestions()
    }

    private fun fetchQuestions() {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("questions")
                    .get()
                    .await()
                
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
            } catch (e: Exception) {
                println("FirestoreDebug: Fetch error: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
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

    fun submitVote(question: FirestoreQuestion, option: String) {
        answeredQuestionIds.add(question.questionId)
        
        // Optimistically update the local state so the user sees the percentage change immediately
        val updatedQuestions = _questions.value.map {
            if (it.questionId == question.questionId) {
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

        viewModelScope.launch {
            val docRef = db.collection("questions").document(question.questionId)
            try {
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
            } catch (e: Exception) {
                e.printStackTrace()
                // If transaction fails, we might want to reload or handle error
            }
        }
    }
}
