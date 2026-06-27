package com.codeword.app.feature.lobby

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.codeword.app.core.model.GameStatus
import com.codeword.app.core.model.Player
import com.codeword.app.core.model.Role
import com.codeword.app.core.model.Team
import com.codeword.app.ui.theme.CardBlue
import com.codeword.app.ui.theme.CardBlueLight
import com.codeword.app.ui.theme.CardRed
import com.codeword.app.ui.theme.CardRedLight

@Composable
fun LobbyScreen(
    roomCode: String,
    uid: String,
    onGameStarted: () -> Unit,
    viewModel: LobbyViewModel = viewModel(factory = LobbyViewModel.factory(roomCode, uid)),
) {
    val room by viewModel.room.collectAsState()

    LaunchedEffect(room?.status) {
        if (room?.status == GameStatus.PLAYING) onGameStarted()
    }

    if (room == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val players = room!!.players
    val myPlayer = players[uid]
    val isHost = room!!.hostUid == uid

    fun slotOwner(role: Role, team: Team): Player? =
        players.values.find { it.role == role && it.team == team }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(24.dp))

        Text(
            text = "Комната",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            text = roomCode,
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            letterSpacing = 6.sp,
        )

        Text(
            text = "Поделитесь кодом с друзьями",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(32.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))

        Text(
            text = "Команды",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            TeamColumn(
                label = "Красные",
                color = CardRed,
                colorLight = CardRedLight,
                team = Team.RED,
                spymasterOwner = slotOwner(Role.SPYMASTER, Team.RED),
                operativeOwner = slotOwner(Role.OPERATIVE, Team.RED),
                myUid = uid,
                myRole = myPlayer?.role,
                myTeam = myPlayer?.team,
                onClaimSpymaster = { viewModel.claimSlot(Role.SPYMASTER, Team.RED) },
                onClaimOperative = { viewModel.claimSlot(Role.OPERATIVE, Team.RED) },
                modifier = Modifier.weight(1f),
            )
            TeamColumn(
                label = "Синие",
                color = CardBlue,
                colorLight = CardBlueLight,
                team = Team.BLUE,
                spymasterOwner = slotOwner(Role.SPYMASTER, Team.BLUE),
                operativeOwner = slotOwner(Role.OPERATIVE, Team.BLUE),
                myUid = uid,
                myRole = myPlayer?.role,
                myTeam = myPlayer?.team,
                onClaimSpymaster = { viewModel.claimSlot(Role.SPYMASTER, Team.BLUE) },
                onClaimOperative = { viewModel.claimSlot(Role.OPERATIVE, Team.BLUE) },
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.weight(1f))

        val isReady = myPlayer?.ready ?: false
        OutlinedButton(
            onClick = { viewModel.toggleReady() },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (isReady) "Готов ✓" else "Не готов")
        }

        Spacer(Modifier.height(8.dp))

        if (isHost) {
            Button(
                onClick = { viewModel.startGame() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Начать игру")
            }
        } else {
            Text(
                text = "Ожидаем хоста…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
            )
        }

        Spacer(Modifier.height(16.dp))
        Spacer(Modifier.navigationBarsPadding())
    }
}

@Composable
private fun TeamColumn(
    label: String,
    color: Color,
    colorLight: Color,
    team: Team,
    spymasterOwner: Player?,
    operativeOwner: Player?,
    myUid: String,
    myRole: Role?,
    myTeam: Team?,
    onClaimSpymaster: () -> Unit,
    onClaimOperative: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = color,
            fontWeight = FontWeight.SemiBold,
        )

        SlotCard(
            roleLabel = "Спаймастер",
            owner = spymasterOwner,
            isMine = myTeam == team && myRole == Role.SPYMASTER,
            teamColor = color,
            teamColorLight = colorLight,
            isOccupiedByOther = spymasterOwner != null && spymasterOwner.uid != myUid,
            onClick = onClaimSpymaster,
        )

        SlotCard(
            roleLabel = "Оперативник",
            owner = operativeOwner,
            isMine = myTeam == team && myRole == Role.OPERATIVE,
            teamColor = color,
            teamColorLight = colorLight,
            isOccupiedByOther = operativeOwner != null && operativeOwner.uid != myUid,
            onClick = onClaimOperative,
        )
    }
}

@Composable
private fun SlotCard(
    roleLabel: String,
    owner: Player?,
    isMine: Boolean,
    teamColor: Color,
    teamColorLight: Color,
    isOccupiedByOther: Boolean,
    onClick: () -> Unit,
) {
    val shape = MaterialTheme.shapes.medium
    val bgColor = when {
        isMine -> teamColorLight
        isOccupiedByOther -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surface
    }
    val borderColor = if (isMine) teamColor else MaterialTheme.colorScheme.outlineVariant

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, shape)
            .border(if (isMine) 2.dp else 1.dp, borderColor, shape)
            .clip(shape)
            .clickable(enabled = !isOccupiedByOther, onClick = onClick)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = roleLabel,
            style = MaterialTheme.typography.bodySmall,
            color = if (isMine) teamColor else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isMine) FontWeight.SemiBold else FontWeight.Normal,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = owner?.name ?: "Свободно",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isMine) FontWeight.Bold else FontWeight.Normal,
            color = if (owner == null) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}
