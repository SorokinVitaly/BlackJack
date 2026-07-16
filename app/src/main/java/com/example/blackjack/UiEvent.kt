package com.example.blackjack

interface UiEvent {
    data class ShowToast(val message: String) : UiEvent
}