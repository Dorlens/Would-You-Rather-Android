package com.example.wouldyourather

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class FirestoreQuestion(
    @DocumentId val questionId: String = "",
    val question: String = "",
    val optionA: String = "",
    val optionB: String = "",
    val colorA: String = "#FF6B35",
    val colorB: String = "#3A86FF",
    val votesA: Long = 0,
    val votesB: Long = 0,
    val totalVotes: Long = 0,
    val createdAt: Timestamp? = null
)
