package com.example.blackjack

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(private val localData: LocalDataRepository) : ViewModel() {
    private val savedState = loadState()
    private val _state = MutableStateFlow(savedState)
    val state = _state.asStateFlow()
    private val _events = MutableSharedFlow<UiEvent>()
    val events = _events.asSharedFlow()

    private val deck = savedState.savedDeck.toMutableList()

    enum class SavePoint {
        ON_DEAL_NEXT,
        ON_HIT,
        ON_STAND,
        GAME_OVER
    }

    init {
        val savePoint = SavePoint.entries[localData.savePoint]
        when (savePoint) {
            SavePoint.ON_DEAL_NEXT -> onDealNext()
            SavePoint.ON_HIT -> onHit()
            SavePoint.ON_STAND -> onStand()
            SavePoint.GAME_OVER -> {}
        }
    }

    fun onResetGame() {
        viewModelScope.launch {
            localData.resetGame()
            _state.update { loadState() }
            deck.clear()
            delay(300L)
            logAndShow("Game was restarted")
        }
    }

    fun onDealNext() {
        viewModelScope.launch {
            if (deck.size != 52) {
                deck.clear()
                deck.addAll(deckPoker)
                deck.shuffle()
            }
            initialState()
            saveState(SavePoint.ON_DEAL_NEXT)
            dealToPlayer()
            dealToPlayer()
            dealToDealer()
            if (state.value.playerPoints == POINTS_LIMIT) {
                dealToDealer()
                gameOver()
            } else {
                _state.update { it.copy(isActionAvailable = true) }        }
            }
    }

    fun onHit() {
        saveState(SavePoint.ON_HIT)
        viewModelScope.launch {
            dealToPlayer()
            when {
                state.value.playerPoints > POINTS_LIMIT -> gameOver()
                state.value.playerPoints == POINTS_LIMIT -> onStand()
            }
        }
    }

    fun onStand() {
        saveState(SavePoint.ON_STAND)
        _state.update { it.copy(isActionAvailable = false) }
        viewModelScope.launch {
            while (state.value.dealerPoints < DEALER_MIN_LIMIT) {
                dealToDealer()
            }
            gameOver()
        }
    }

    private fun initialState() {
        _state.update {
            it.copy(
                dealerCards = emptyList(),
                playerCards = emptyList(),
                savedDeck = deck,
                playerChips = it.playerChips - 1,
                isGameStarted = true,
                isResetAvailable = true,
                isActionAvailable = false
            )
        }
    }

    private suspend fun dealToPlayer() {
        val card = deck.removeAt(deck.lastIndex)
        val newCards = state.value.playerCards + card
        delay(300L)
        _state.update { it.copy(
            playerCards = newCards,
            playerPoints = calcPoints(newCards)
        ) }
    }

    private suspend fun dealToDealer() {
        val card = deck.removeAt(deck.lastIndex)
        val newCards = state.value.dealerCards + card
        delay(300L)
        _state.update { it.copy(
            dealerCards = newCards,
            dealerPoints = calcPoints(newCards)
        ) }
    }

    private suspend fun gameOver() {
        val dealer = state.value.dealerPoints
        val player = state.value.playerPoints
        val (mess, wonChips) = when {
            player > POINTS_LIMIT -> "Bust" to 0
            dealer > POINTS_LIMIT || player > dealer -> "You won" to 2
            player == dealer -> "Push" to 1
            else -> "Dealer won" to 0
        }
        logAndShow(mess)
        _state.update { it.copy(
            isActionAvailable = true,
            isGameStarted = false,
            playerChips = it.playerChips + wonChips
        ) }
        saveState(SavePoint.GAME_OVER)
    }

    private fun saveState(savePoint: SavePoint) = with(state.value) {
        localData.dealerCards = Card.serializeList(dealerCards)
        localData.playerCards = Card.serializeList(playerCards)
        localData.savedDeck = Card.serializeList(deck)
        localData.playerChips = playerChips
        localData.isGameStarted = isGameStarted
        localData.isResetAvailable = isResetAvailable
        localData.savePoint = savePoint.ordinal
    }

    private fun restoreSnapshot(): ScreenState = with(localData) {
        val dealerCards = Card.unserializeList(dealerCards)
        val playerCards = Card.unserializeList(playerCards)
        val dealerPoints = calcPoints(dealerCards)
        val playerPoints = calcPoints(playerCards)
        ScreenState(
            dealerCards = dealerCards,
            playerCards = playerCards,
            savedDeck = Card.unserializeList(savedDeck),
            dealerPoints = dealerPoints,
            playerPoints = playerPoints,
            playerChips = playerChips,
            isGameStarted = isGameStarted,
            isResetAvailable = isResetAvailable,
            isActionAvailable = true
        )
    }

    private fun calcPoints(cards: List<Card>): Int =
        cards.sorted().fold(0) { acc, card ->
            val rank = card.rank
            val point = when {
                rank == CardRank.ACE -> if (acc > 10) 1 else 11
                rank >= CardRank.JACK -> 10
                else -> rank.ordinal + 2
            }
            acc + point
        }

    private fun loadState(): ScreenState =
        try {
            restoreSnapshot()
        } catch(_: Exception) {
            log("Local data is broken. Game was restarted")
            localData.resetGame()
            restoreSnapshot()
        }

    private fun log(mess: String) = Log.e("GamePlay", mess)

    private suspend fun logAndShow(mess: String) {
        log(mess)
        _events.emit(UiEvent.ShowToast(mess))
    }

    companion object {
        const val POINTS_LIMIT = 21
        const val DEALER_MIN_LIMIT = 17
    }
}