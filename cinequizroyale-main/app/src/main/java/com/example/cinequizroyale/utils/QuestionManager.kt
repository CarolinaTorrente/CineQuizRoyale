package com.example.cinequizroyale.utils

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.storage.Storage
import com.google.api.services.storage.StorageScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.util.*

data class Question(
    val text: String,
    val options: List<String>,
    val correctAnswerIndex: Int
)

class QuestionManager(private val context: Context) {
    private val tag = "QuestionManager"
    private val bucketName = "cinequizroyale-bucket1"
    private val questionsPath = "questions/cinema_questions.json"

    // Fallback questions in case Cloud Storage fails
    private val fallbackQuestions = listOf(
        Question(
            "Which film won the Oscar for Best Picture in 2023?",
            listOf("Everything Everywhere All at Once", "The Banshees of Inisherin", "Top Gun: Maverick", "Avatar: The Way of Water"),
            0
        ),
        Question(
            "Who directed the movie 'Inception'?",
            listOf("Steven Spielberg", "Christopher Nolan", "James Cameron", "Quentin Tarantino"),
            1
        ),
        Question(
            "Which actor played Iron Man in the Marvel Cinematic Universe?",
            listOf("Chris Evans", "Chris Hemsworth", "Robert Downey Jr.", "Mark Ruffalo"),
            2
        )
    )

    suspend fun fetchQuestions(account: GoogleSignInAccount): List<Question> {
        return try {
            withContext(Dispatchers.IO) {
                // Set up credentials for Cloud Storage
                val credential = GoogleAccountCredential.usingOAuth2(
                    context,
                    Collections.singleton(StorageScopes.DEVSTORAGE_READ_WRITE)
                )
                credential.selectedAccount = account.account

                // Set up Cloud Storage client
                val storage = Storage.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    GsonFactory.getDefaultInstance(),
                    credential
                )
                    .setApplicationName("CineQuizRoyaleB")
                    .build()

                // Download content
                val content = storage.objects().get(bucketName, questionsPath)
                    .executeMedia()
                    .content
                    .bufferedReader()
                    .use { it.readText() }

                // Parse JSON
                val questionsJson = JSONArray(content)
                val questions = mutableListOf<Question>()

                for (i in 0 until questionsJson.length()) {
                    val questionObj = questionsJson.getJSONObject(i)
                    val options = mutableListOf<String>()
                    val optionsArray = questionObj.getJSONArray("options")

                    for (j in 0 until optionsArray.length()) {
                        options.add(optionsArray.getString(j))
                    }

                    questions.add(
                        Question(
                            text = questionObj.getString("text"),
                            options = options,
                            correctAnswerIndex = questionObj.getInt("correctAnswerIndex")
                        )
                    )
                }

                questions
            }
        } catch (e: Exception) {
            Log.e(tag, "Error fetching questions: ${e.message}", e)
            // Return fallback questions if there's an error
            fallbackQuestions
        }
    }

    suspend fun getRandomQuestions(account: GoogleSignInAccount, count: Int = 3): List<Question> {
        val allQuestions = fetchQuestions(account)
        return if (allQuestions.size <= count) {
            allQuestions
        } else {
            allQuestions.shuffled().take(count)
        }
    }
}