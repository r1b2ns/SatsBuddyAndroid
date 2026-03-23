package com.satsbuddy.presentation.navigation

sealed class Screen(val route: String) {
    data object CardList : Screen("card_list")

    data object CardDetail : Screen("card_detail/{cardIdentifier}") {
        fun createRoute(cardIdentifier: String) = "card_detail/$cardIdentifier"
    }

    data object SlotList : Screen("slot_list/{cardIdentifier}") {
        fun createRoute(cardIdentifier: String) = "slot_list/$cardIdentifier"
    }

    data object SlotHistory : Screen("slot_history/{cardIdentifier}/{slotNumber}") {
        fun createRoute(cardIdentifier: String, slotNumber: Int) =
            "slot_history/$cardIdentifier/$slotNumber"
    }

    data object Receive : Screen("receive/{address}") {
        fun createRoute(address: String) = "receive/$address"
    }

    data object SendFlow : Screen("send_flow/{cardIdentifier}/{slotNumber}") {
        fun createRoute(cardIdentifier: String, slotNumber: Int) =
            "send_flow/$cardIdentifier/$slotNumber"
    }
}
