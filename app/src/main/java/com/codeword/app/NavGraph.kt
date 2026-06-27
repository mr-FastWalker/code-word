package com.codeword.app

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.codeword.app.core.model.Role
import com.codeword.app.core.model.Team
import com.codeword.app.core.model.WinReason
import com.codeword.app.feature.game.GameScreen
import com.codeword.app.feature.result.ResultScreen
import com.codeword.app.feature.start.StartScreen

private object Routes {
    const val START = "start"
    const val GAME = "game/{myRole}/{myTeam}"
    const val RESULT = "result/{winner}/{winReason}/{myRole}/{myTeam}"

    fun game(role: Role, team: Team) = "game/${role.name}/${team.name}"
    fun result(winner: Team, winReason: WinReason, myRole: Role, myTeam: Team) =
        "result/${winner.name}/${winReason.name}/${myRole.name}/${myTeam.name}"
}

@Composable
fun CodeWordNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.START) {

        composable(Routes.START) {
            StartScreen(
                onRoleSelected = { role, team ->
                    navController.navigate(Routes.game(role, team))
                },
            )
        }

        composable(
            route = Routes.GAME,
            arguments = listOf(
                navArgument("myRole") { type = NavType.StringType },
                navArgument("myTeam") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val myRole = Role.valueOf(backStackEntry.arguments?.getString("myRole") ?: Role.OPERATIVE.name)
            val myTeam = Team.valueOf(backStackEntry.arguments?.getString("myTeam") ?: Team.RED.name)
            GameScreen(
                myRole = myRole,
                myTeam = myTeam,
                onGameEnd = { winner, winReason ->
                    navController.navigate(Routes.result(winner, winReason, myRole, myTeam)) {
                        // убираем GameScreen из стека — нет петли навигации при возврате назад
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
            ),
        ) { backStackEntry ->
            val winner = Team.valueOf(backStackEntry.arguments?.getString("winner") ?: Team.RED.name)
            val winReason = WinReason.valueOf(backStackEntry.arguments?.getString("winReason") ?: WinReason.ALL_CARDS.name)
            val myRole = Role.valueOf(backStackEntry.arguments?.getString("myRole") ?: Role.OPERATIVE.name)
            val myTeam = Team.valueOf(backStackEntry.arguments?.getString("myTeam") ?: Team.RED.name)
            ResultScreen(
                winner = winner,
                winReason = winReason,
                onPlayAgain = {
                    navController.navigate(Routes.game(myRole, myTeam)) {
                        popUpTo(Routes.START) { inclusive = false }
                    }
                },
                onExit = {
                    navController.popBackStack(Routes.START, inclusive = false)
                },
            )
        }
    }
}
