package com.example.blackjack

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage


@Composable
fun Player(
    cards: List<Card>,
    chips: Int,
    points: Int,
    isDealer: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Cards(cards)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            if (!isDealer) {
                Text(
                    text = chips.toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
                ChipIcon()
            }
            if (points > 0) {
                Text(
                    text = points.toString(),
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.Yellow
                )
            }
        }
    }
}

@Composable
fun Cards(cards: List<Card>) =
    Box {
        cards.forEachIndexed { index, card ->
            Box(modifier = Modifier
                .padding(start = (index * 20).dp)
                .animateContentSize()
            ) {
                Card(card)
            }
        }
    }

@Composable
fun Card(card: Card) {
    Card(
        border = BorderStroke(width = 1.dp, color = Color.Black),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.height(cardHeight).width(cardWidth)
    ) {
        val imageLoader = (LocalContext.current.applicationContext as MainApp).imageLoader
        AsyncImage(
            model = card.faceAssetName,
            imageLoader = imageLoader,
            contentDescription = "Card",
        )
    }
}

@Composable
fun AppButton(text: String, onClick: () -> Unit = {}) {
    TextButton(
        onClick = onClick,
        border = BorderStroke(2.dp, Color.Yellow),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            color = Color.Yellow
        )
    }
}

@Composable
fun ResetGameButton(onResetGame: () -> Unit) {
    var showConfirmation by rememberSaveable { mutableStateOf(false) }
    AppButton("Reset game") { showConfirmation = true }
    if (showConfirmation) {
        AlertDialog(
            onDismissRequest = { showConfirmation = false },
            title = { Text("Reset game?") },
            text = { Text("Your current progress will be lost") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmation = false
                        onResetGame()
                    }
                ) { Text("Reset") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmation = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun ChipIcon() {
    Icon(
        imageVector = ImageVector.vectorResource(R.drawable.poker_chip),
        tint = Color.Red,
        contentDescription = "Poker chip",
        modifier = Modifier.size(24.dp)
    )
}

val cardHeight = 88.dp
val cardWidth = 63.dp