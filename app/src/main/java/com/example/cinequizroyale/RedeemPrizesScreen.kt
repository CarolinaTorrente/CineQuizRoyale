package com.example.cinequizroyale

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.cinequizroyale.ui.theme.AccentRed
import com.example.cinequizroyale.ui.theme.BackgroundDark
import com.example.cinequizroyale.ui.theme.ButtonBg
import com.example.cinequizroyale.ui.theme.PrimaryText
import com.example.cinequizroyale.ui.theme.SecondaryText


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RedeemPrizesScreen(
    userPoints: Int,
    onBack: () -> Unit,
    onRedeemPrize: (PrizeItem) -> Unit
) {
    val prizes = listOf(
        PrizeItem(
            id = "discount_50",
            name = "50% Off Cinema Ticket",
            description = "Get 50% off a cinema ticket at your nearest theater",
            pointsRequired = 1000,
            iconResId = R.drawable.photouser
        ),
        PrizeItem(
            id = "popcorn_drink",
            name = "Free Popcorn & Drink",
            description = "Enjoy a free popcorn and drink combo at any cinema in your city",
            pointsRequired = 2000,
            iconResId = R.drawable.photouser
        ),
        PrizeItem(
            id = "free_ticket",
            name = "Free Movie Ticket",
            description = "Redeem a free movie ticket for any standard screening",
            pointsRequired = 3000,
            iconResId = R.drawable.photouser
        ),
        PrizeItem(
            id = "vip_experience",
            name = "VIP Cinema Experience",
            description = "Upgrade to VIP seating with premium services",
            pointsRequired = 5000,
            iconResId = R.drawable.photouser
        )
    )


    var showConfirmDialog by remember { mutableStateOf(false) }
    var selectedPrize by remember { mutableStateOf<PrizeItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Redeemable Prizes",
                        color = PrimaryText,
                        fontFamily = FontFamily(Font(R.font.luckiest_guy))
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
                .padding(16.dp)
        ) {
            // User points display
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = ButtonBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your Points",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryText,
                        fontFamily = FontFamily(Font(R.font.caveat))
                    )
                    Text(
                        text = "$userPoints pts",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText,
                        fontFamily = FontFamily(Font(R.font.luckiest_guy))
                    )
                }
            }

            // Available prizes list
            Text(
                text = "Available Prizes",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText,
                fontFamily = FontFamily(Font(R.font.luckiest_guy)),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            LazyColumn {
                items(prizes) { prize ->
                    PrizeItemCard(
                        prize = prize,
                        userPoints = userPoints,
                        onRedeemClick = {
                            selectedPrize = prize
                            showConfirmDialog = true
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        // Show confirmation dialogue
        if (showConfirmDialog && selectedPrize != null) {
            RedeemConfirmationDialog(
                prize = selectedPrize!!,
                onConfirm = {
                    onRedeemPrize(selectedPrize!!)
                    showConfirmDialog = false
                    selectedPrize = null
                },
                onDismiss = {
                    showConfirmDialog = false
                    selectedPrize = null
                }
            )
        }
    }
}

// Prize item data class
data class PrizeItem(
    val id: String,
    val name: String,
    val description: String,
    val pointsRequired: Int,
    val iconResId: Int
)

// Individual prize card
@Composable
fun PrizeItemCard(
    prize: PrizeItem,
    userPoints: Int,
    onRedeemClick: () -> Unit
) {
    val canRedeem = userPoints >= prize.pointsRequired

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
            // Prize icon
            Image(
                painter = painterResource(id = prize.iconResId),
                contentDescription = prize.name,
                modifier = Modifier
                    .size(60.dp)
                    .padding(end = 16.dp)
            )

            // Prize details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = prize.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Text(
                    text = prize.description,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 2
                )

                Text(
                    text = "${prize.pointsRequired} points required",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (canRedeem) Color.Green else Color.Red,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Redeem button
            Button(
                onClick = onRedeemClick,
                enabled = canRedeem,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canRedeem) AccentRed else Color.Gray,
                    disabledContainerColor = Color.Gray
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = "Redeem",
                    color = Color.White,
                    fontFamily = FontFamily(Font(R.font.caveat))
                )
            }
        }
    }
}



