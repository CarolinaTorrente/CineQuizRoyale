package com.example.cinequizroyale

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cinequizroyale.ui.theme.*
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.storage.Storage
import com.google.api.services.storage.StorageScopes
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

data class Question(
    val text: String,
    val options: List<String>,
    val correctAnswerIndex: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CinemaQuestionsScreen(account: GoogleSignInAccount, onBack: () -> Unit, onQuizComplete: (Int) -> Unit) {
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var questions by remember { mutableStateOf<List<Question>>(emptyList()) }

    // Load questions from Cloud Storage when component is first displayed
    LaunchedEffect(Unit) {
        try {
            val cloudQuestions = withContext(Dispatchers.IO) {
                fetchQuestionsFromCloud(account)
            }
            questions = cloudQuestions.shuffled().take(3)
            isLoading = false
        } catch (e: Exception) {
            loadError = e.message
            isLoading = false
        }
    }

    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableStateOf(0) }
    var showResult by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Cinema Quiz",
                        fontFamily = FontFamily(Font(R.font.luckiest_guy)),
                        color = PrimaryText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", fontSize = 24.sp, color = PrimaryText)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BackgroundDark)
            )
        },
        containerColor = BackgroundDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(color = PrimaryText)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Loading questions...",
                        color = SecondaryText,
                        fontSize = 18.sp
                    )
                }

                loadError != null -> {
                    Text(
                        "Failed to load questions: $loadError",
                        color = AccentRed,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                    Button(
                        onClick = onBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ButtonBg)
                    ) {
                        Text("Go Back", color = SecondaryText)
                    }
                }

                questions.isEmpty() -> {
                    Text(
                        "No questions available at this time",
                        color = SecondaryText,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                    Button(
                        onClick = onBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ButtonBg)
                    ) {
                        Text("Go Back", color = SecondaryText)
                    }
                }

                !showResult -> {
                    val currentQuestion = questions[currentQuestionIndex]

                    // Question count
                    Text(
                        "Question ${currentQuestionIndex + 1}/${questions.size}",
                        fontSize = 18.sp,
                        color = SecondaryText,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Question text
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = ButtonBg)
                    ) {
                        Text(
                            currentQuestion.text,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    // Answer options
                    currentQuestion.options.forEachIndexed { index, option ->
                        val isSelected = selectedAnswer == index
                        Button(
                            onClick = { selectedAnswer = index },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) AccentRed else ButtonBg
                            )
                        ) {
                            Text(
                                option,
                                fontSize = 16.sp,
                                color = SecondaryText,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Next button
                    Button(
                        onClick = {
                            if (selectedAnswer != null) {
                                if (selectedAnswer == currentQuestion.correctAnswerIndex) {
                                    score++
                                }

                                if (currentQuestionIndex < questions.size - 1) {
                                    currentQuestionIndex++
                                    selectedAnswer = null
                                } else {
                                    showResult = true
                                }
                            }
                        },
                        enabled = selectedAnswer != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ButtonBg)
                    ) {
                        Text(
                            "Next Question",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryText,
                            fontFamily = FontFamily(Font(R.font.luckiest_guy))
                        )
                    }
                }

                else -> {
                    // Show results
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = ButtonBg)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Quiz Complete!",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryText,
                                fontFamily = FontFamily(Font(R.font.luckiest_guy)),
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            Text(
                                "Your Score: $score/${questions.size}",
                                fontSize = 24.sp,
                                color = PrimaryText,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )

                            Text(
                                "You earned ${score * 100} points!",
                                fontSize = 20.sp,
                                color = SecondaryText
                            )
                        }
                    }

                    Button(
                        onClick = {
                            // Call the onQuizComplete callback with earned points
                            onQuizComplete(score * 100)
                            onBack()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ButtonBg)
                    ) {
                        Text(
                            "Return to Main Menu",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryText,
                            fontFamily = FontFamily(Font(R.font.luckiest_guy))
                        )
                    }
                }
            }
        }
    }
}

// Function to fetch questions from Cloud Storage
suspend fun fetchQuestionsFromCloud(account: GoogleSignInAccount): List<Question> {
    try {
        // Set up credentials for Cloud Storage
        val credential = GoogleAccountCredential.usingOAuth2(
            CloudStorageProvider.context,
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

        // Get questions file from Cloud Storage
        val obj = storage.objects().get("cinequizroyale-bucket1", "questions/cinema_questions.json").execute()

        // Download content
        val content = storage.objects().get("cinequizroyale-bucket1", "questions/cinema_questions.json")
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

        return questions
    } catch (e: Exception) {
        // If there's an error, fallback to local questions
        return getFallbackQuestions()
    }
}

// Fallback questions in case Cloud Storage fails
fun getFallbackQuestions(): List<Question> {
    return listOf(
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
        ),
        Question(
            "What year was the first Star Wars movie released?",
            listOf("1975", "1977", "1980", "1983"),
            1
        ),
        Question(
            "Which of these films is NOT directed by Martin Scorsese?",
            listOf("The Departed", "Goodfellas", "The Shawshank Redemption", "Taxi Driver"),
            2
        )
    )
}