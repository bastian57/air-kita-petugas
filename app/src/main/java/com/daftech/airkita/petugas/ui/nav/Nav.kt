package com.daftech.airkita.petugas.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.daftech.airkita.petugas.data.Repository
import com.daftech.airkita.petugas.ui.screen.HomeScreen
import com.daftech.airkita.petugas.ui.screen.ReceiveScreen
import com.daftech.airkita.petugas.ui.screen.RecordScreen
import com.daftech.airkita.petugas.ui.screen.SendScreen

object Routes {
    const val HOME = "home"
    const val RECORD = "record/{customerId}"
    const val SEND = "send"
    const val RECEIVE = "receive"

    fun record(customerId: Long) = "record/$customerId"
}

@Composable
fun PetugasNav(repo: Repository) {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.HOME) {
        composable(Routes.HOME) { HomeScreen(repo, nav) }
        composable(
            Routes.RECORD,
            arguments = listOf(navArgument("customerId") { type = NavType.LongType })
        ) { backStack ->
            RecordScreen(repo, nav, backStack.arguments?.getLong("customerId") ?: 0L)
        }
        composable(Routes.SEND) { SendScreen(repo, nav) }
        composable(Routes.RECEIVE) { ReceiveScreen(repo, nav) }
    }
}
