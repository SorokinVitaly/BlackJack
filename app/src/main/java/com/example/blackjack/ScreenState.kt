package com.example.blackjack

import androidx.compose.runtime.Immutable


@Immutable
data class ScreenState(
    val dealerCards: List<Card> = emptyList(),
    val playerCards: List<Card> = emptyList(),
    val savedDeck: List<Card> = emptyList(),
    val dealerPoints: Int,
    val playerPoints: Int,
    val playerChips: Int,
    val isGameStarted: Boolean,
    val isResetAvailable: Boolean,
    val isActionAvailable: Boolean
)