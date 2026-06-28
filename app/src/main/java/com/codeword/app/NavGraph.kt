package com.codeword.app

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.codeword.app.core.model.Role
import com.codeword.app.core.model.Team
import com.codeword.app.core.model.WinReason
import com.codeword.app.feature.game.GameScreen
import com.codeword.app.feature.home.HomeScreen
import com.codeword.app.feature.home.HomeViewModel
import com.codeword.app.feature.lobby.LobbyScreen
import com.codeword.app.feature.result.ResultScreen
import com.google.firebase.auth.FirebaseAuth

private object Routes {
    const val HOME = "home"
    const val LOBBY = "lobby/{roomCode}"
    const val GAME = "game/{roomCode}"
    const val RESULT = "result/{winner}/{winReason}/{myRole}/{myTeam}/{roomCode}"

    fun lobby(code: String) = "lobby/$code"
    fun game(code: String) = "game/$code"
    fun result(winner: Team, winReason: WinReason, myRole: Role, myTeam: Team, roomCode: String) =
        "result/${winner.name}/${winReason.name}/${myRole.name}/${myTeam.name}/$roomCode"
}

@Composable
fun CodeWordNavGraph() {
    val navController = rememberNavController()
    val activity = LocalContext.current as ComponentActivity
    val uid = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    // Activity-scoped so the name survives navigation back to HomeScreen
    val homeViewModel: HomeViewModel = viewModel(activity)

    NavHost(navController = navController, startDestination = Routes.HOME) {

        composable(Routes.HOME) {
            HomeScreen(
                onRoomReady = { code ->
                    homeViewModel.reset()
                    navController.navigate(Routes.lobby(code))
                },
                viewModel = homeViewModel,
            )
        }

        composable(
            route = Routes.LOBBY,
            arguments = listOf(navArgument("roomCode") { type = NavType.StringType }),
        ) { backStackEntry ->
            val code = backStackEntry.arguments?.getString("roomCode") ?: ""
            LobbyScreen(
                roomCode = code,
                uid = uid,
                onGameStarted = {
                    navController.navigate(Routes.game(code)) {
                        popUpTo(Routes.HOME) { inclusive = false }
                    }
                },
            )
        }

        composable(
            route = Routes.GAME,
            arguments = listOf(navArgument("roomCode") { type = NavType.StringType }),
        ) { backStackEntry ->
            val code = backStackEntry.arguments?.getString("roomCode") ?: ""
            GameScreen(
                roomCode = code,
                uid = uid,
                onGameEnd = { winner, winReason, myRole, myTeam ->
                    navController.navigate(Routes.result(winner, winReason, myRole, myTeam, code)) {
                        popUpTo(Routes.GAME) { inclusive = true }
                    }
                },
            )
        }

        composable(
            route = Routes.RESULT,
            arguments = listOf(
                navArgument("winner") { type = NavType.StringType },
                navArgument("winReason") { type = NavType.StringType },
                navArgument("myRole") { type = NavType.StringType },
                navArgument("myTeam") { type = NavType.StringType },
                navArgument("roomCode") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val winner = Team.valueOf(backStackEntry.arguments?.getString("winner") ?: Team.RED.name)
            val winReason = WinReason.valueOf(backStackEntry.arguments?.getString("winReason") ?: WinReason.ALL_CARDS.name)
            val myRole = Role.valueOf(backStackEntry.arguments?.getString("myRole") ?: Role.OPERATIVE.name)
            val myTeam = Team.valueOf(backStackEntry.arguments?.getString("myTeam") ?: Team.RED.name)
            val roomCode = backStackEntry.arguments?.getString("roomCode") ?: ""
            ResultScreen(
                winner = winner,
                winReason = winReason,
                onPlayAgain = {
                    navController.navigate(Routes.lobby(roomCode)) {
                        popUpTo(Routes.HOME) { inclusive = false }
                    }
                },
                onExit = {
                    // Сброс Done-состояния: без него HomeScreen сразу навигирует в лобби заново
                    homeViewModel.reset()
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
            )
        }
    }
}
