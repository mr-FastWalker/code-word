package com.codeword.app.feature.start

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.codeword.app.R
import com.codeword.app.core.model.Role
import com.codeword.app.core.model.Team
import com.codeword.app.ui.theme.CardBlue
import com.codeword.app.ui.theme.CardRed

@Composable
fun StartScreen(onRoleSelected: (Role, Team) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.ExtraBold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.start_play_as),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
        Spacer(modifier = Modifier.height(40.dp))

        TeamBlock(
            teamLabelRes = R.string.team_red,
            spymasterLabelRes = R.string.start_red_spymaster,
            operativeLabelRes = R.string.start_red_operative,
            team = Team.RED,
            onRoleSelected = onRoleSelected,
        )
        Spacer(modifier = Modifier.height(20.dp))
        TeamBlock(
            teamLabelRes = R.string.team_blue,
            spymasterLabelRes = R.string.start_blue_spymaster,
            operativeLabelRes = R.string.start_blue_operative,
            team = Team.BLUE,
            onRoleSelected = onRoleSelected,
        )
    }
}

@Composable
private fun TeamBlock(
    teamLabelRes: Int,
    spymasterLabelRes: Int,
    operativeLabelRes: Int,
    team: Team,
    onRoleSelected: (Role, Team) -> Unit,
) {
    val teamColor = if (team == Team.RED) CardRed else CardBlue

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(teamLabelRes),
            style = MaterialTheme.typography.labelLarge,
            color = teamColor,
            fontWeight = FontWeight.Bold,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = { onRoleSelected(Role.SPYMASTER, team) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = teamColor),
            ) {
                Text(stringResource(spymasterLabelRes))
            }
            OutlinedButton(
                onClick = { onRoleSelected(Role.OPERATIVE, team) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = teamColor),
            ) {
                Text(stringResource(operativeLabelRes))
            }
        }
    }
}
