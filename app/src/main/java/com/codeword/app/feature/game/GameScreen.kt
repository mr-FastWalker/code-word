package com.codeword.app.feature.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.codeword.app.core.model.Role
import com.codeword.app.core.model.Team
import com.codeword.app.core.model.WinReason

@Composable
fun GameScreen(
    roomCode: String,
    uid: String,
    onGameEnd: (winner: Team, winReason: WinReason, myRole: Role, myTeam: Team) -> Unit,
    viewModel: GameViewModel = viewModel(factory = GameViewModel.factory(roomCode, uid)),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state?.winner) {
        val winner = state?.winner ?: return@LaunchedEffect
        val reason = state?.winReason ?: return@LaunchedEffect
        val myRole = state?.myRole ?: return@LaunchedEffect
        val myTeam = state?.myTeam ?: return@LaunchedEffect
        onGameEnd(winner, reason, myRole, myTeam)
    }

    if (state == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val s = state!!

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .imePadding(),
    ) {
        StatusBar(
            state = s,
            modifier = Modifier.fillMaxWidth(),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            BoardGrid(
                board = s.board,
                myRole = s.myRole,
                isActiveOperative = s.isActiveOperative,
                onCardTap = viewModel::onCardTap,
                modifier = Modifier.fillMaxSize(),
            )
            val showOverlay = if (s.myRole == Role.SPYMASTER) !s.isMyTurn else !s.isActiveOperative
            if (showOverlay) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.White.copy(alpha = 0.7f)),
                )
            }
        }
        if (s.myRole == Role.SPYMASTER) {
            ClueInputPanel(
                myTeam = s.myTeam,
                maxCount = s.myTeamCardsLeft,
                enabled = s.isActiveSpymaster,
                onSubmit = viewModel::onClueSubmit,
            )
        }
    }
}
