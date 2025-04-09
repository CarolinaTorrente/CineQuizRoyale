package com.example.cinequizroyale

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.cinequizroyale.ui.theme.BackgroundDark
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

class FriendsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val friendsList = listOf(
            Friend("1", "Alice", "alice@example.com", 320),
            Friend("2", "Bob", "bob@example.com", 250),
            Friend("3", "Charlie", "charlie@example.com", 180),
            Friend("4", "Diana", "diana@example.com", 400)
        )

        setContent {
            MaterialTheme {
                FriendsScreen(friends = friendsList) {
                    finish() // Go back to previous screen
                }
            }
        }
    }
}

// Data class for friends
data class Friend(
    val id: String,
    val name: String,
    val email: String,
    val points: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(friends: List<Friend>, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Friends",
                        fontSize = 20.sp,
                        fontFamily = FontFamily(Font(R.font.luckiest_guy))
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", fontSize = 24.sp)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BackgroundDark)
            )
        },
        containerColor = BackgroundDark
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            items(friends) { friend ->
                FriendCard(friend)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun FriendCard(friend: Friend) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(friend.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(friend.email, fontSize = 14.sp, color = Color.Gray)
            Text("${friend.points} pts", fontSize = 14.sp, color = Color(0xFFB8860B))
        }
    }
}
