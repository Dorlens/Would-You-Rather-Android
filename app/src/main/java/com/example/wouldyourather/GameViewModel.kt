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

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    init {
        fetchQuestions()
    }

    private fun fetchQuestions() {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("questions")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .await()
                _questions.value = snapshot.toObjects(FirestoreQuestion::class.java)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun submitVote(questionId: String, option: String) {
        viewModelScope.launch {
            val docRef = db.collection("questions").document(questionId)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val votesA = snapshot.getLong("votesA") ?: 0
                val votesB = snapshot.getLong("votesB") ?: 0
                val totalVotes = snapshot.getLong("totalVotes") ?: 0

                if (option == "A") {
                    transaction.update(docRef, "votesA", votesA + 1)
                } else {
                    transaction.update(docRef, "votesB", votesB + 1)
                }
                transaction.update(docRef, "totalVotes", totalVotes + 1)
            }.await()
            // Optionally refetch or update local state
            fetchQuestions()
        }
    }

    fun hexToColor(hex: String): Color {
        return try {
            Color(android.graphics.Color.parseColor(hex))
        } catch (e: Exception) {
            Color.Gray
        }
    }
}
