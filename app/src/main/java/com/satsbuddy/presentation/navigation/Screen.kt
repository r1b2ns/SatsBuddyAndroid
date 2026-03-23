package com.satsbuddy.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {

    @Serializable
    data object CardList : Screen

    @Serializable
    data class CardDetail(val cardIdentifier: String) : Screen

    @Serializable
    data class SlotList(val cardIdentifier: String) : Screen

    @Serializable
    data class SlotHistory(val cardIdentifier: String, val slotNumber: Int) : Screen

    @Serializable
    data class Receive(val address: String) : Screen

    @Serializable
    data class SendFlow(val cardIdentifier: String, val slotNumber: Int) : Screen
}
