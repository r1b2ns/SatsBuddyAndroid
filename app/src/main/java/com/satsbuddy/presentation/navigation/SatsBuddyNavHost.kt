package com.satsbuddy.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.satsbuddy.presentation.carddetail.CardDetailScreen
import com.satsbuddy.presentation.cardlist.CardListScreen
import com.satsbuddy.presentation.receive.ReceiveScreen
import com.satsbuddy.presentation.send.SendFlowScreen
import com.satsbuddy.presentation.slothistory.SlotHistoryScreen
import com.satsbuddy.presentation.slotlist.SlotListScreen

@Composable
fun SatsBuddyNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.CardList
    ) {
        composable<Screen.CardList> {
            CardListScreen(
                onCardClick = { cardIdentifier ->
                    navController.navigate(Screen.CardDetail(cardIdentifier))
                }
            )
        }

        composable<Screen.CardDetail> {
            CardDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSlotList = { cardId ->
                    navController.navigate(Screen.SlotList(cardId))
                },
                onNavigateToReceive = { address ->
                    navController.navigate(Screen.Receive(address))
                },
                onNavigateToSend = { cardId, slotNumber ->
                    navController.navigate(Screen.SendFlow(cardId, slotNumber))
                }
            )
        }

        composable<Screen.SlotList> {
            SlotListScreen(
                onNavigateBack = { navController.popBackStack() },
                onSlotClick = { cardId, slotNumber ->
                    navController.navigate(Screen.SlotHistory(cardId, slotNumber))
                }
            )
        }

        composable<Screen.SlotHistory> {
            SlotHistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSend = { cardId, slotNumber ->
                    navController.navigate(Screen.SendFlow(cardId, slotNumber))
                }
            )
        }

        composable<Screen.Receive> {
            ReceiveScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable<Screen.SendFlow> {
            SendFlowScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
