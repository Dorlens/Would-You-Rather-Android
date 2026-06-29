package com.example.wouldyourather

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "local_questions")
data class LocalQuestion(
    @PrimaryKey val questionId: String,
    val question: String,
    val optionA: String,
    val optionB: String,
    val colorA: String,
    val colorB: String,
    val votesA: Long,
    val votesB: Long,
    val totalVotes: Long
)

@Dao
interface QuestionDao {
    @Query("SELECT * FROM local_questions")
    fun getAllQuestions(): Flow<List<LocalQuestion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<LocalQuestion>)

    @Query("SELECT * FROM local_questions WHERE questionId = :id")
    suspend fun getQuestionById(id: String): LocalQuestion?
}

@Database(entities = [LocalQuestion::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "would_you_rather_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
