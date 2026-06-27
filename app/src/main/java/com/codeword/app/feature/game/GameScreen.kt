package com.codeword.app.feature.game

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.codeword.app.core.model.Role
import com.codeword.app.core.model.Team
import com.codeword.app.core.model.WinReason

@Composable
fun GameScreen(
    myRole: Role,
    myTeam: Team,
    onGameEnd: (winner: Team, winReason: WinReason) -> Unit,
    viewModel: GameViewModel = viewModel(),
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

    if (state.isActiveSpymaster) {
        ClueInputDialog(onSubmit = viewModel::onClueSubmit)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        StatusBar(
            state = state,
            modifier = Modifier.fillMaxWidth(),
        )
        BoardGrid(
            board = state.board,
            myRole = state.myRole,
            isActiveOperative = state.isActiveOperative,
            onCardTap = viewModel::onCardTap,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )
    }
}
