package com.codeword.app.feature.game

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.codeword.app.core.model.Role
import com.codeword.app.core.model.Team
import com.codeword.app.core.model.WinReason

@Composable
fun GameScreen(
    myRole: Role,
    myTeam: Team,
    onGameEnd: (winner: Team, winReason: WinReason) -> Unit,
    viewModel: GameViewModel = viewModel(LocalContext.current as ComponentActivity),
) {
    LaunchedEffect(myRole, myTeam) {
        viewModel.init(myRole, myTeam)
    }

    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.winner) {
        val winner = state.winner
        val reason = state.winReason
        if (winner != null && reason != null) onGameEnd(winner, reason)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .imePadding(),
    ) {
        StatusBar(
            state = state,
            modifier = Modifier.fillMaxWidth(),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            BoardGrid(
                board = state.board,
                myRole = state.myRole,
                isActiveOperative = state.isActiveOperative,
                onCardTap = viewModel::onCardTap,
                modifier = Modifier.fillMaxSize(),
            )
            val showOverlay = if (state.myRole == Role.SPYMASTER) !state.isMyTurn else !state.isActiveOperative
            if (showOverlay) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.White.copy(alpha = 0.7f)),
                )
            }
        }
        if (state.myRole == Role.SPYMASTER) {
            ClueInputPanel(
                myTeam = state.myTeam,
                maxCount = state.myTeamCardsLeft,
                enabled = state.isActiveSpymaster,
                onSubmit = viewModel::onClueSubmit,
            )
        }
    }
}
