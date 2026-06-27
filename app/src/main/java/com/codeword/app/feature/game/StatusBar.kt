package com.codeword.app.feature.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codeword.app.R
import com.codeword.app.core.model.GamePhase
import com.codeword.app.core.model.Team
import com.codeword.app.ui.theme.CardBlue
import com.codeword.app.ui.theme.CardRed

@Composable
fun StatusBar(state: GameUiState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TurnLabel(state)
            ScoreLabel(redLeft = state.score.redLeft, blueLeft = state.score.blueLeft)
        }
        if (state.phase == GamePhase.GUESS && state.clue != null) {
            ClueLabel(state)
        }
    }
}

@Composable
private fun TurnLabel(state: GameUiState) {
    val teamName = stringResource(if (state.currentTeam == Team.RED) R.string.team_red else R.string.team_blue)
    val phaseLabel = stringResource(if (state.phase == GamePhase.CLUE) R.string.game_phase_clue else R.string.game_phase_guess)
    val teamColor = if (state.currentTeam == Team.RED) CardRed else CardBlue

    val statusText = when {
        state.phase == GamePhase.CLUE && state.isActiveSpymaster -> stringResource(R.string.game_status_your_turn_clue)
        state.phase == GamePhase.CLUE -> stringResource(R.string.game_status_waiting_clue, teamName)
        state.isActiveOperative -> stringResource(R.string.game_status_your_turn_guess)
        else -> stringResource(R.string.game_status_waiting_guess, teamName)
    }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = "●", color = teamColor, fontSize = 12.sp)
            Text(text = "$teamName • $phaseLabel", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Text(
            text = statusText,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
    }
}

@Composable
private fun ScoreLabel(redLeft: Int, blueLeft: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "● $redLeft", color = CardRed, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(text = "● $blueLeft", color = CardBlue, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
private fun ClueLabel(state: GameUiState) {
    val clue = state.clue ?: return
    val guessesText = if (state.guessesLeft == Int.MAX_VALUE) "осталось: ∞" else "осталось: ${state.guessesLeft}"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "${clue.word.uppercase()} × ${clue.count}",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
        )
        Text(
            text = guessesText,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )
    }
}
