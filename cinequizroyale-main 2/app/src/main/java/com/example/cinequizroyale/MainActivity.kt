package com.example.cinequizroyale

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.cinequizroyale.ui.theme.AccentRed
import com.example.cinequizroyale.ui.theme.BackgroundDark
import com.example.cinequizroyale.ui.theme.ButtonBg
import com.example.cinequizroyale.ui.theme.CinequizroyaleTheme
import com.example.cinequizroyale.ui.theme.PrimaryText
import com.example.cinequizroyale.ui.theme.SecondaryText
import com.example.cinequizroyale.utils.QuestionManager
import com.example.cinequizroyale.utils.QuestionUploader
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.InputStreamContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.storage.Storage
import com.google.api.services.storage.StorageScopes
import com.google.api.services.storage.model.StorageObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import androidx.compose.ui.platform.LocalContext
import com.example.cinequizroyale.utils.BucketVerifier
import com.example.cinequizroyale.utils.CinemaDataManager
import org.json.JSONArray
import android.net.Uri

class MainActivity : ComponentActivity() {
    private val tag = "MainActivity"
    private lateinit var googleSignInClient: GoogleSignInClient
    private var currentAccount by mutableStateOf<GoogleSignInAccount?>(null)
    private lateinit var questionManager: QuestionManager

    // Add user points state
    private var userPoints by mutableStateOf(1000)

    // Activity result launcher for Google Sign-In
    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleSignInResult(task)
        }
    }

    private var redemptionHistory by mutableStateOf<List<RedemptionItem>>(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        CloudStorageProvider.initialize(this)
        questionManager = QuestionManager(this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestId()
            .requestProfile()
            .requestScopes(Scope(StorageScopes.DEVSTORAGE_READ_WRITE))
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        currentAccount = GoogleSignIn.getLastSignedInAccount(this)

        setContent {
            CinequizroyaleTheme {
                val currentScreen = remember { mutableStateOf("main") }
                val friendsState = remember { mutableStateOf<List<Friend>>(emptyList()) }
                val fallbackFriends = listOf(
                    Friend("1", "Alice", 320),
                    Friend("2", "Bob", 250),
                    Friend("3", "Charlie", 180),
                    Friend("4", "Diana", 400)
                )

                // Load friends data when account changes
                LaunchedEffect(currentAccount) {
                    if (currentAccount != null) {
                        loadFriendsData(currentAccount!!) { friends ->
                            friendsState.value = friends
                        }
                    } else {
                        friendsState.value = fallbackFriends
                    }
                }

                // Load user data
                LaunchedEffect(currentAccount) {
                    currentAccount?.let {
                        loadUserData(
                            it,
                            onPointsLoaded = { points -> userPoints = points },
                            onRedemptionsLoaded = { redemptions -> redemptionHistory = redemptions }
                        )
                    }
                }

                when {
                    currentAccount == null -> {
                        LoginScreen(onGoogleSignIn = { signInWithGoogle() })
                    }

                    currentScreen.value == "main" -> {
                        MainScreen(
                            account = currentAccount!!,
                            userPoints = userPoints,
                            onLogout = { signOut() },
                            onPlayClick = { currentScreen.value = "questions" },
                            onPrizesClick = { currentScreen.value = "prizes" },
                            onHistoryClick = { currentScreen.value = "history" },
                            onProfileClick = { currentScreen.value = "userProfile" },
                            onNeighborhoodClick = { currentScreen.value = "friends" },
                            onCinemasClick = {
                                val intent = Intent(this@MainActivity, MapsActivity::class.java)
                                startActivity(intent)
                            }
                        )
                    }

                    currentScreen.value == "questions" -> {
                        CinemaQuestionsScreen(
                            account = currentAccount!!,
                            onBack = { currentScreen.value = "main" },
                            onQuizComplete = { points -> updateUserPoints(points) }
                        )
                    }

                    currentScreen.value == "prizes" -> {
                        RedeemPrizesScreen(
                            userPoints = userPoints,
                            onBack = { currentScreen.value = "main" },
                            onRedeemPrize = { prize ->
                                redeemPrize(prize)
                                currentScreen.value = "main"
                            }
                        )
                    }

                    currentScreen.value == "history" -> {
                        RedemptionHistoryScreen(
                            redemptions = redemptionHistory,
                            onBack = { currentScreen.value = "main" }
                        )
                    }

                    currentScreen.value == "userProfile" -> {
                        UserProfileScreen(
                            account = currentAccount!!,
                            userPoints = userPoints,
                            onBack = { currentScreen.value = "main" }
                        )
                    }

                    currentScreen.value == "friends" -> {
                        FriendsScreen(
                            friends = friendsState.value,
                            onBack = { currentScreen.value = "main" }
                        )
                    }
                }
            }
        }
    }

    private fun loadUserData(
        account: GoogleSignInAccount,
        onPointsLoaded: (Int) -> Unit,
        onRedemptionsLoaded: (List<RedemptionItem>) -> Unit
    ) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Setting up credentials for Cloud Storage
                val credential = GoogleAccountCredential.usingOAuth2(
                    this@MainActivity,
                    listOf(StorageScopes.DEVSTORAGE_READ_WRITE)
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

                // Get user data from Cloud Storage
                val userDataStream = storage.objects().get("cinequizroyale-bucket-1", "users/${account.id}.json")
                    .executeMedia()
                    .content

                val userData = userDataStream.bufferedReader().use { it.readText() }

                // JSON
                val jsonObject = org.json.JSONObject(userData)

                // Get points
                if (jsonObject.has("points")) {
                    val points = jsonObject.getInt("points")
                    withContext(Dispatchers.Main) {
                        onPointsLoaded(points)
                    }
                }

                // Get redemption history
                val redemptions = mutableListOf<RedemptionItem>()
                if (jsonObject.has("redemptions")) {
                    val redemptionsArray = jsonObject.getJSONArray("redemptions")
                    for (i in 0 until redemptionsArray.length()) {
                        val redemptionObj = redemptionsArray.getJSONObject(i)
                        redemptions.add(
                            RedemptionItem(
                                id = redemptionObj.getString("id"),
                                name = redemptionObj.getString("name"),
                                pointsRequired = redemptionObj.getInt("pointsRequired"),
                                redeemedAt = redemptionObj.getString("redeemedAt")
                            )
                        )
                    }
                }

                withContext(Dispatchers.Main) {
                    onRedemptionsLoaded(redemptions)
                }

            } catch (e: Exception) {
                Log.e(tag, "Error loading user data: ${e.message}", e)
                // Default values if there's an error
            }
        }
    }

    // Function to load user points from Cloud Storage (currently not using)
    private fun loadUserPoints(account: GoogleSignInAccount) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Set up credentials for Cloud Storage
                val credential = GoogleAccountCredential.usingOAuth2(
                    this@MainActivity,
                    listOf(StorageScopes.DEVSTORAGE_READ_WRITE)
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

                // Get user data from Cloud Storage
                val userDataStream = storage.objects().get("cinequizroyale-bucket-1", "users/${account.id}.json")
                    .executeMedia()
                    .content

                val userData = userDataStream.bufferedReader().use { it.readText() }

                // Parse JSON
                val jsonObject = org.json.JSONObject(userData)
                if (jsonObject.has("points")) {
                    val points = jsonObject.getInt("points")
                    withContext(Dispatchers.Main) {
                        userPoints = points
                    }
                }

            } catch (e: Exception) {
                Log.e(tag, "Error loading user points: ${e.message}", e)
                // Default points value if there's an error
            }
        }
    }

    private fun loadFriendsData(
        account: GoogleSignInAccount,
        onFriendsLoaded: (List<Friend>) -> Unit
    ) {
        Log.d("FriendsDebug", "Starting to load friends data")
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val credential = GoogleAccountCredential.usingOAuth2(
                    this@MainActivity,
                    listOf(StorageScopes.DEVSTORAGE_READ_WRITE)
                )
                credential.selectedAccount = account.account

                Log.d("FriendsDebug", "Creating Google Cloud Storage client...")
                val storage = Storage.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    GsonFactory.getDefaultInstance(),
                    credential
                )
                    .setApplicationName("CineQuizRoyale")
                    .build()


                Log.d("FriendsDebug", "Making request to Cloud Storage...")
                val friendsDataStream = storage.objects()
                    .get("cinequizroyale-bucket1", "friends/names.json")  // Asegúrate de que el archivo tenga un nombre correcto
                    .executeMedia()
                    .content


                Log.d("FriendsDebug", "Making request to Cloud Storage2...")
                val friendsData = friendsDataStream.reader(Charsets.UTF_8).use {
                    it.readText()
                }
                Log.d("FriendsDebug", "Raw JSON: $friendsData")

                val jsonArray = JSONArray(friendsData)

                val currentUserId = account.id
                val friendsList = mutableListOf<Friend>()

                for (i in 0 until jsonArray.length()) {
                    val friendObj = jsonArray.getJSONObject(i)
                    if (friendObj.getString("id") != currentUserId) {
                        friendsList.add(
                            Friend(
                                id = friendObj.getString("id"),
                                name = friendObj.getString("name"),
                                points = friendObj.getInt("points")
                            )
                        )
                    }
                }

                withContext(Dispatchers.Main) {
                    Log.d("FriendsDebug", "Calling onFriendsLoaded with: ${friendsList.size} friends")
                    onFriendsLoaded(friendsList)
                }

            } catch (e: Exception) {
                Log.e(tag, "Error loading friends data: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onFriendsLoaded(emptyList())
                }
            }
        }
    }


    // Function to update user points
    private fun updateUserPoints(newPoints: Int) {
        currentAccount?.let { account ->
            // Update local points
            userPoints += newPoints

            // Update points in Cloud Storage
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    // Set up credentials for Cloud Storage
                    val credential = GoogleAccountCredential.usingOAuth2(
                        this@MainActivity,
                        listOf(StorageScopes.DEVSTORAGE_READ_WRITE)
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

                    // Get existing user data
                    val userDataStream = storage.objects().get("cinequizroyale-bucket-1", "users/${account.id}.json")
                        .executeMedia()
                        .content

                    val userData = userDataStream.bufferedReader().use { it.readText() }

                    // Parse and update JSON
                    val jsonObject = org.json.JSONObject(userData)
                    jsonObject.put("points", userPoints)

                    // Upload updated data
                    val inputStream = ByteArrayInputStream(jsonObject.toString().toByteArray())

                    // Create StorageObject metadata
                    val storageObject = StorageObject()
                        .setName("users/${account.id}.json")
                        .setContentType("application/json")

                    // Upload to Cloud Storage
                    storage.objects().insert(
                        "cinequizroyale-bucket-1",
                        storageObject,
                        InputStreamContent("application/json", inputStream)
                    ).execute()

                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Points updated successfully!", Toast.LENGTH_SHORT).show()
                    }

                } catch (e: Exception) {
                    Log.e(tag, "Error updating user points: ${e.message}", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MainActivity,
                            "Error updating points: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }


    // Function to redeem a prize
    private fun redeemPrize(prize: PrizeItem) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                currentAccount?.let { account ->
                    // Only deduct points if user has enough
                    if (userPoints >= prize.pointsRequired) {
                        // Deduct points
                        val newPoints = userPoints - prize.pointsRequired

                        // Update local points
                        withContext(Dispatchers.Main) {
                            userPoints = newPoints
                        }

                        // Update points in Cloud Storage
                        // Set up credentials for Cloud Storage
                        val credential = GoogleAccountCredential.usingOAuth2(
                            this@MainActivity,
                            listOf(StorageScopes.DEVSTORAGE_READ_WRITE)
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

                        // Get existing user data
                        val userDataStream = storage.objects().get("cinequizroyale-bucket-1", "users/${account.id}.json")
                            .executeMedia()
                            .content

                        val userData = userDataStream.bufferedReader().use { it.readText() }

                        // Parse and update JSON
                        val jsonObject = org.json.JSONObject(userData)
                        jsonObject.put("points", newPoints)

                        // Add redemption history
                        val redemptionsArray = if (jsonObject.has("redemptions")) {
                            jsonObject.getJSONArray("redemptions")
                        } else {
                            org.json.JSONArray()
                        }

                        val redemption = org.json.JSONObject()
                        redemption.put("id", prize.id)
                        redemption.put("name", prize.name)
                        redemption.put("pointsRequired", prize.pointsRequired)
                        redemption.put("redeemedAt", java.util.Date().toString())

                        redemptionsArray.put(redemption)
                        jsonObject.put("redemptions", redemptionsArray)

                        // Upload updated data
                        val inputStream = ByteArrayInputStream(jsonObject.toString().toByteArray())

                        // Create StorageObject metadata
                        val storageObject = StorageObject()
                            .setName("users/${account.id}.json")
                            .setContentType("application/json")

                        // Upload to Cloud Storage
                        storage.objects().insert(
                            "cinequizroyale-bucket-1",
                            storageObject,
                            InputStreamContent("application/json", inputStream)
                        ).execute()

                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@MainActivity,
                                "Prize redeemed successfully! Check your email for details.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@MainActivity,
                                "Not enough points to redeem this prize.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Error redeeming prize: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Error redeeming prize: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }


    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            currentAccount = account

            // Save user data to Cloud Storage
            saveUserToCloudStorage(account)

            lifecycleScope.launch {
                try {
                    // Check and upload questions if needed
                    val questionsExist = checkIfQuestionsExist(account)
                    if (!questionsExist) {
                        val success = QuestionUploader.uploadQuestions(this@MainActivity, account)
                        if (success) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Sample questions uploaded",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    // Add bucket verification here
                    verifyCloudStorageBucket()

                    // Initialize cinema data if needed
                    val cinemaDataManager = CinemaDataManager(this@MainActivity)
                    val cinemaInitialized = cinemaDataManager.initializeCinemaData(account)
                    if (cinemaInitialized) {
                        Log.d(tag, "Cinema data successfully initialized")
                    } else {
                        Log.e(tag, "Failed to initialize cinema data, will use hardcoded values")
                    }

                } catch (e: Exception) {
                    Log.e(tag, "Error during initialization: ${e.message}", e)
                }
            }
        } catch (e: ApiException) {
            // Sign in failed
            Log.w(tag, "signInResult:failed code=" + e.statusCode)
            Toast.makeText(this, "Sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            currentAccount = null
        }
    }

    // In MainActivity.kt, add this to your onCreate method after GoogleSignIn is initialized
// or to a specific debugging function

    private fun verifyCloudStorageBucket() {
        Log.d(tag, "Verifying cloud storage bucket")
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            val bucketName = "cinequizroyale-bucket-1" // The bucket name you're using
            val message = BucketVerifier.verifyBucket(this, account, bucketName)

            // Show result in a toast
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()

            // Log the result
            Log.d(tag, "Bucket verification result: $message")

            // Optionally try to create the bucket if it doesn't exist
            if (message.contains("not found") || message.contains("doesn't exist")) {
                Log.d(tag, "Attempting to create bucket")
                val created = BucketVerifier.createBucketIfNeeded(this, account, bucketName)
                if (created) {
                    Toast.makeText(this, "Bucket created successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to create bucket", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Log.d(tag, "Cannot verify bucket - user not signed in")
            Toast.makeText(this, "Sign in first to verify cloud storage", Toast.LENGTH_SHORT).show()
        }
    }

    // not using
    private suspend fun checkIfQuestionsExist(account: GoogleSignInAccount): Boolean {
        return try {
            // Set up credentials for Cloud Storage
            val credential = GoogleAccountCredential.usingOAuth2(
                this@MainActivity,
                listOf(StorageScopes.DEVSTORAGE_READ_WRITE)
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

            // Check if questions file exists
            val objList = storage.objects().list("cinequizroyale-1")
                .setPrefix("questions/cinema_questions.json")
                .execute()

            objList.items?.isNotEmpty() ?: false

        } catch (e: Exception) {
            Log.e(tag, "Error checking if questions exist: ${e.message}", e)
            false
        }
    }

    private fun saveUserToCloudStorage(account: GoogleSignInAccount) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Set up credentials for Cloud Storage
                val credential = GoogleAccountCredential.usingOAuth2(
                    this@MainActivity,
                    listOf(StorageScopes.DEVSTORAGE_READ_WRITE)
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

                // Create user data JSON
                val userData = """
                {
                    "id": "${account.id}",
                    "name": "${account.displayName ?: ""}",
                    "email": "${account.email ?: ""}",
                    "photoUrl": "${account.photoUrl ?: ""}",
                    "lastLogin": "${java.util.Date()}",
                    "points": 0
                }
                    """.trimIndent()

                // Convert to input stream
                val inputStream = ByteArrayInputStream(userData.toByteArray())

                // Create StorageObject metadata
                val storageObject = StorageObject()
                    .setName("users/${account.id}.json")
                    .setContentType("application/json")

                // Upload to Cloud Storage
                storage.objects().insert(
                    "cinequizroyale-bucket-1",
                    storageObject,
                    InputStreamContent("application/json", inputStream)
                ).execute()

                // Switch to Main thread only for UI updates
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "User data saved to Cloud", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Log.e(tag, "Error saving user data: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Error saving user data: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun signOut() {
        googleSignInClient.signOut().addOnCompleteListener(this) {
            currentAccount = null
            Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show()
        }
    }
}

// Redemption History Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RedemptionHistoryScreen(
    redemptions: List<RedemptionItem>,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Redemption History",
                        color = PrimaryText,
                        fontFamily = FontFamily(Font(R.font.luckiest_guy))
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        // Usamos un texto con el símbolo de la flecha
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
                .padding(16.dp)
        ) {
            if (redemptions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No redemption history yet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = PrimaryText,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn {
                    items(redemptions) { redemption ->
                        RedemptionHistoryItem(redemption)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

// Redemption history item data class
data class RedemptionItem(
    val id: String,
    val name: String,
    val pointsRequired: Int,
    val redeemedAt: String
)

// Individual redemption history item
@Composable
fun RedemptionHistoryItem(redemption: RedemptionItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Prize icon (not done yet)
            Image(
                painter = painterResource(id = R.drawable.photouser),
                contentDescription = "Redemption",
                modifier = Modifier
                    .size(40.dp)
                    .padding(end = 16.dp)
            )

            // Redemption details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = redemption.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Text(
                    text = "${redemption.pointsRequired} points",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Text(
                    text = "Redeemed on: ${redemption.redeemedAt}",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

// Dialog when redeeming a prize
@Composable
fun RedeemConfirmationDialog(
    prize: PrizeItem,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Confirm Redemption",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                Text("Are you sure you want to redeem:")
                Text(
                    text = prize.name,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text("This will deduct ${prize.pointsRequired} points from your account.")
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun LoginScreen(onGoogleSignIn: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(BackgroundDark),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "CineQuiz Royale",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryText,
            fontFamily = FontFamily(Font(R.font.luckiest_guy))
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onGoogleSignIn,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ButtonBg)
        ) {
            Text(
                text = "Sign in with Google",
                color = SecondaryText,
                fontFamily = FontFamily(Font(R.font.luckiest_guy)),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

// Update the MainScreen function to include the onPrizesClick parameter
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    account: GoogleSignInAccount,
    userPoints: Int,
    onLogout: () -> Unit,
    onPlayClick: () -> Unit,
    onPrizesClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onProfileClick: () -> Unit,
    onCinemasClick: () -> Unit,
    onNeighborhoodClick: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BackgroundDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Hello ${account.displayName ?: "User"}!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText,
                fontFamily = FontFamily(Font(R.font.luckiest_guy)),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            UserProfileCard(
                account = account,
                userPoints = userPoints,
                onClick = onProfileClick
            )

            Spacer(modifier = Modifier.height(16.dp))


            ExploreNeighborhoodCard(onClick = onNeighborhoodClick)


            Spacer(modifier = Modifier.height(16.dp))

            // Modified PlayButton to handle navigation
            Button(
                onClick = onPlayClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ButtonBg)
            ) {
                Text(
                    "Start playing",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryText,
                    fontFamily = FontFamily(Font(R.font.luckiest_guy))
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // New Metacritic Button
            Button(
                onClick = {
                    val intent = Intent(context, WebViewActivity::class.java)
                    intent.putExtra("url", "https://www.metacritic.com/")
                    intent.putExtra("title", "Metacritic")
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF117711))
            ) {
                Text(
                    "Check Metacritic Reviews",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontFamily = FontFamily(Font(R.font.luckiest_guy))
                )
            }

            Spacer(modifier = Modifier.height(16.dp))


            // Update the BottomButtons to pass onPrizesClick and onHistoryClick
            BottomButtons(
                onCinemasClick = onCinemasClick,
                onPrizesClick = onPrizesClick,
                onHistoryClick = onHistoryClick  // Pass the history click handler
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Logout Button
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
            ) {
                Text(
                    "Log Out",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryText,
                    fontFamily = FontFamily(Font(R.font.luckiest_guy))
                )
            }
        }
    }
}

@Composable
fun UserProfileCard(account: GoogleSignInAccount, userPoints: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {

            Image(
                painter = painterResource(id = R.drawable.photouser),
                contentDescription = "User Icon",
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))


            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.displayName ?: "User",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
                Text(
                    text = account.email ?: "",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "${userPoints} pts",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB8860B),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}



@Composable
fun ExploreNeighborhoodCard(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Friends",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                fontFamily = FontFamily(Font(R.font.luckiest_guy))
            )
        }
    }
}

// Update the BottomButtons to include the onPrizesClick parameter and History button
@Composable
fun BottomButtons(
    onCinemasClick: () -> Unit = {},
    onPrizesClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {}
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = onCinemasClick,  // Call this when "Cinemas" is clicked
            modifier = Modifier
                .weight(1f)
                .padding(end = 4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Cinemas", fontSize = 14.sp)
        }


        Button(
            onClick = onPrizesClick,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Prizes", fontSize = 14.sp)
        }

        Button(
            onClick = onHistoryClick,
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("History", fontSize = 14.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    account: GoogleSignInAccount,
    userPoints: Int,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("User Profile", color = PrimaryText, fontSize = 20.sp)
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(id = R.drawable.photouser),
                contentDescription = "User Photo",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Gray, CircleShape)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "$userPoints pts",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD700)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Profile Info
            val firstName = account.displayName?.split(" ")?.firstOrNull() ?: "N/A"
            ProfileInfoRow("Nombre", firstName)

            ProfileInfoRow("Correo", account.email ?: "N/A")
            ProfileInfoRow("Apellidos", account.familyName ?: "N/A")
            ProfileInfoRow("Lugar", "Madrid")

            Spacer(modifier = Modifier.height(32.dp))


        }
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryText
        )
        Text(
            text = value,
            fontSize = 16.sp,
            color = Color.Gray
        )
    }
}

