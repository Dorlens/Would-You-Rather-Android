package com.example.wouldyourather

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class DataStoreManager(private val context: Context) {
    private val ANSWERED_QUESTIONS_KEY = stringSetPreferencesKey("answered_questions")

    val answeredQuestionIds: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[ANSWERED_QUESTIONS_KEY] ?: emptySet()
        }

    suspend fun saveAnsweredQuestion(id: String) {
        context.dataStore.edit { preferences ->
            val currentIds = preferences[ANSWERED_QUESTIONS_KEY] ?: emptySet()
            preferences[ANSWERED_QUESTIONS_KEY] = currentIds + id
        }
    }

    suspend fun clearAllAnswers() {
        context.dataStore.edit { preferences ->
            preferences.remove(ANSWERED_QUESTIONS_KEY)
        }
    }
}
