package com.example.aichatbot.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun StockAppNavigation (chatViewModel : ChatViewModel) {
    val navController = rememberNavController()
    NavHost(
        navController = navController, startDestination = "WatchList", builder = {
            composable ("WatchList"){
                WatchList(navController)
            }

            composable ("AiChatBot"){
                    backStackEntry -> ChatPage(modifier = Modifier, chatViewModel, navController)
            }
        }
    )
}
