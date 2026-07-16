package com.example.blackjack

import android.content.SharedPreferences
import javax.inject.Inject


interface LocalDataRepository {
    var dealerCards: String
    var playerCards: String
    var playerChips: Int
    var savedDeck: String
    var isGameStarted: Boolean
    var isResetAvailable: Boolean

    fun resetGame()
}

class LocalDataRepositoryImpl @Inject constructor(override val prefs: SharedPreferences) :
    LocalDataRepository,
    PrefsOwner
{
    override var dealerCards: String by PreferencesDelegate(
        ::dealerCards.name,
        ""
    )
    override var playerCards: String by PreferencesDelegate(
        ::playerCards.name,
        ""
    )
    override var playerChips: Int by PreferencesDelegate(
        ::playerChips.name,
        DEFAULT_CHIP_NUMBER
    )
    override var savedDeck: String by PreferencesDelegate(
        ::savedDeck.name,
        ""
    )
    override var isGameStarted: Boolean by PreferencesDelegate(
        ::isGameStarted.name,
        false
    )
    override var isResetAvailable: Boolean by PreferencesDelegate(
        ::isResetAvailable.name,
        true
    )

    override fun resetGame() {
        dealerCards = ""
        playerCards = ""
        playerChips = DEFAULT_CHIP_NUMBER
        savedDeck = ""
        isGameStarted = false
        isResetAvailable = false
    }

    companion object {
        const val DEFAULT_CHIP_NUMBER = 100
    }
}