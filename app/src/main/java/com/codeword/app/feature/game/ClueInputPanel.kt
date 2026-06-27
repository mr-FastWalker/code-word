package com.codeword.app.feature.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codeword.app.R
import com.codeword.app.core.model.Team
import com.codeword.app.ui.theme.CardBlue
import com.codeword.app.ui.theme.CardRed

@Composable
fun ClueInputPanel(
    myTeam: Team,
    maxCount: Int,
    enabled: Boolean = true,
    onSubmit: (word: String, count: Int) -> Unit,
) {
    val teamColor = if (myTeam == Team.RED) CardRed else CardBlue

    var word by rememberSaveable { mutableStateOf("") }
    var selectedCount by rememberSaveable { mutableIntStateOf(1) }
    var wordError by rememberSaveable { mutableStateOf(false) }

    fun trySubmit() {
        val trimmed = word.trim()
        wordError = trimmed.isBlank()
        if (!wordError) {
            onSubmit(trimmed, selectedCount)
            word = ""
            selectedCount = 1
        }
    }

    Surface(shadowElevation = 8.dp) {
        HorizontalDivider()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(R.string.clue_dialog_title),
                style = MaterialTheme.typography.labelLarge,
            )

            OutlinedTextField(
                value = word,
                onValueChange = { word = it; wordError = false },
                label = { Text(stringResource(R.string.clue_dialog_word_hint)) },
                isError = wordError,
                supportingText = if (wordError) {
                    { Text(stringResource(R.string.clue_error_blank_word)) }
                } else null,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            CountTokenSelector(
                maxCount = maxCount,
                selected = selectedCount,
                teamColor = teamColor,
                onSelect = { selectedCount = it },
            )

            Button(
                onClick = { trySubmit() },
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = teamColor),
            ) {
                Text(stringResource(R.string.clue_dialog_submit))
            }
        }
    }
}

@Composable
private fun CountTokenSelector(
    maxCount: Int,
    selected: Int,
    teamColor: Color,
    onSelect: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(R.string.clue_dialog_count_hint),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            (1..maxCount).forEach { count ->
                CountChip(
                    label = count.toString(),
                    isSelected = count <= selected,
                    teamColor = teamColor,
                    onClick = { onSelect(count) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun CountChip(
    label: String,
    isSelected: Boolean,
    teamColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(shape)
            .then(
                if (isSelected) Modifier.background(teamColor)
                else Modifier.border(1.5.dp, teamColor.copy(alpha = 0.5f), shape)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else teamColor,
            textAlign = TextAlign.Center,
        )
    }
}
