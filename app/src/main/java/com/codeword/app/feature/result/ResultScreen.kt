package com.codeword.app.feature.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.codeword.app.core.model.Team
import com.codeword.app.core.model.WinReason

// Заглушка — полная реализация на шаге 6
@Composable
fun ResultScreen(
    winner: Team,
    winReason: WinReason,
    onPlayAgain: () -> Unit,
    onExit: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = if (winner == Team.RED) "Красные победили!" else "Синие победили!",
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (winReason == WinReason.ASSASSIN) "Соперник вскрыл убийцу" else "Все карты открыты",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
        Spacer(Modifier.height(40.dp))
        Button(onClick = onPlayAgain) { Text("Играть снова") }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onExit) { Text("Выйти") }
    }
}
