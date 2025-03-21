package com.example.cinequizroyale.utils

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.InputStreamContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.storage.Storage
import com.google.api.services.storage.StorageScopes
import com.google.api.services.storage.model.StorageObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.util.*

object QuestionUploader {
    private const val TAG = "QuestionUploader"
    private const val BUCKET_NAME = "cinequizroyale-bucket1"
    private const val QUESTIONS_PATH = "questions/cinema_questions.json"

    // Sample cinema questions to upload
    private val sampleQuestions = """
    [
        {
            "text": "Which film won the Oscar for Best Picture in 2023?",
            "options": ["Everything Everywhere All at Once", "The Banshees of Inisherin", "Top Gun: Maverick", "Avatar: The Way of Water"],
            "correctAnswerIndex": 0
        },
        {
            "text": "Who directed the movie 'Inception'?",
            "options": ["Steven Spielberg", "Christopher Nolan", "James Cameron", "Quentin Tarantino"],
            "correctAnswerIndex": 1
        },
        {
            "text": "Which actor played Iron Man in the Marvel Cinematic Universe?",
            "options": ["Chris Evans", "Chris Hemsworth", "Robert Downey Jr.", "Mark Ruffalo"],
            "correctAnswerIndex": 2
        },
        {
            "text": "What year was the first Star Wars movie released?",
            "options": ["1975", "1977", "1980", "1983"],
            "correctAnswerIndex": 1
        },
        {
            "text": "Which of these films is NOT directed by Martin Scorsese?",
            "options": ["The Departed", "Goodfellas", "The Shawshank Redemption", "Taxi Driver"],
            "correctAnswerIndex": 2
        },
        {
            "text": "Which actress played Katniss Everdeen in 'The Hunger Games'?",
            "options": ["Emma Watson", "Jennifer Lawrence", "Shailene Woodley", "Emma Stone"],
            "correctAnswerIndex": 1
        },
        {
            "text": "Which of these movies is based on a Marvel comic?",
            "options": ["The Dark Knight", "Black Panther", "Justice League", "Suicide Squad"],
            "correctAnswerIndex": 1
        },
        {
            "text": "Who played the character of Jack Dawson in the movie 'Titanic'?",
            "options": ["Brad Pitt", "Leonardo DiCaprio", "Tom Cruise", "Johnny Depp"],
            "correctAnswerIndex": 1
        },
        {
            "text": "Which movie features a character named Forrest Gump?",
            "options": ["Forrest Gump", "The Shawshank Redemption", "Saving Private Ryan", "Cast Away"],
            "correctAnswerIndex": 0
        },
        {
            "text": "Which film franchise features a character named Harry Potter?",
            "options": ["Lord of the Rings", "Star Wars", "Harry Potter", "The Chronicles of Narnia"],
            "correctAnswerIndex": 2
        }
    ]
    """.trimIndent()

    suspend fun uploadQuestions(context: Context, account: GoogleSignInAccount): Boolean {
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
                    .setApplicationName("CineQuizRoyale")
                    .build()

                // Convert to input stream
                val inputStream = ByteArrayInputStream(sampleQuestions.toByteArray())

                // Create StorageObject metadata
                val storageObject = StorageObject()
                    .setName(QUESTIONS_PATH)
                    .setContentType("application/json")

                // Upload to Cloud Storage
                storage.objects().insert(
                    BUCKET_NAME,
                    storageObject,
                    InputStreamContent("application/json", inputStream)
                ).execute()

                Log.d(TAG, "Questions uploaded successfully")
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading questions: ${e.message}", e)
            false
        }
    }
}