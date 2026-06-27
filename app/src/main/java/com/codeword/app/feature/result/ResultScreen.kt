package com.codeword.app.feature.result

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codeword.app.R
import com.codeword.app.core.model.Team
import com.codeword.app.core.model.WinReason
import com.codeword.app.ui.theme.CardBlue
import com.codeword.app.ui.theme.CardBlueLight
import com.codeword.app.ui.theme.CardRed
import com.codeword.app.ui.theme.CardRedLight

@Composable
fun ResultScreen(
    winner: Team,
    winReason: WinReason,
    onPlayAgain: () -> Unit,
    onExit: () -> Unit,
) {
    val teamColor = if (winner == Team.RED) CardRed else CardBlue
    val teamColorLight = if (winner == Team.RED) CardRedLight else CardBlueLight
    val teamName = stringResource(if (winner == Team.RED) R.string.team_red else R.string.team_blue)
    val reasonText = stringResource(
        if (winReason == WinReason.ASSASSIN) R.string.result_reason_assassin
        else R.string.result_reason_all_cards
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(teamColorLight.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .systemBarsPadding()
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Цветной кружок команды
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(teamColor),
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.result_win_title, teamName),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = teamColor,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = reasonText,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(56.dp))

            Button(
                onClick = onPlayAgain,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = teamColor),
            ) {
                Text(
                    text = stringResource(R.string.result_play_again),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                )
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = onExit,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = teamColor),
            ) {
                Text(
                    text = stringResource(R.string.result_exit),
                    fontSize = 16.sp,
                )
            }
        }
    }
}
