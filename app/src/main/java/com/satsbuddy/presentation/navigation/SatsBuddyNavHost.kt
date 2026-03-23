package com.satsbuddy.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
        startDestination = Screen.CardList.route
    ) {
        composable(Screen.CardList.route) {
            CardListScreen(
                onCardClick = { cardIdentifier ->
                    navController.navigate(Screen.CardDetail.createRoute(cardIdentifier))
                }
            )
        }

        composable(
            route = Screen.CardDetail.route,
            arguments = listOf(navArgument("cardIdentifier") { type = NavType.StringType })
        ) {
            CardDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSlotList = { cardId ->
                    navController.navigate(Screen.SlotList.createRoute(cardId))
                },
                onNavigateToReceive = { address ->
                    navController.navigate(Screen.Receive.createRoute(address))
                },
                onNavigateToSend = { cardId, slotNumber ->
                    navController.navigate(Screen.SendFlow.createRoute(cardId, slotNumber))
                }
            )
        }

        composable(
            route = Screen.SlotList.route,
            arguments = listOf(navArgument("cardIdentifier") { type = NavType.StringType })
        ) {
            SlotListScreen(
                onNavigateBack = { navController.popBackStack() },
                onSlotClick = { cardId, slotNumber ->
                    navController.navigate(Screen.SlotHistory.createRoute(cardId, slotNumber))
                }
            )
        }

        composable(
            route = Screen.SlotHistory.route,
            arguments = listOf(
                navArgument("cardIdentifier") { type = NavType.StringType },
                navArgument("slotNumber") { type = NavType.IntType }
            )
        ) {
            SlotHistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSend = { cardId, slotNumber ->
                    navController.navigate(Screen.SendFlow.createRoute(cardId, slotNumber))
                }
            )
        }

        composable(
            route = Screen.Receive.route,
            arguments = listOf(navArgument("address") { type = NavType.StringType })
        ) {
            ReceiveScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.SendFlow.route,
            arguments = listOf(
                navArgument("cardIdentifier") { type = NavType.StringType },
                navArgument("slotNumber") { type = NavType.IntType }
            )
        ) {
            SendFlowScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
