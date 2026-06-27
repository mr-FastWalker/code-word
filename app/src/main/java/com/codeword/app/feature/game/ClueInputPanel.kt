package com.codeword.app.feature.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.codeword.app.R

@Composable
fun ClueInputPanel(onSubmit: (word: String, count: Int) -> Unit) {
    var word by rememberSaveable { mutableStateOf("") }
    var countText by rememberSaveable { mutableStateOf("") }
    var wordError by rememberSaveable { mutableStateOf(false) }
    var countError by rememberSaveable { mutableStateOf(false) }

    fun trySubmit() {
        val trimmed = word.trim()
        val count = countText.trim().toIntOrNull()
        wordError = trimmed.isBlank()
        countError = count == null || count !in 0..9
        if (!wordError && !countError) onSubmit(trimmed, count!!)
    }

    Surface(shadowElevation = 8.dp) {
        HorizontalDivider()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.clue_dialog_title),
                style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top,
            ) {
                OutlinedTextField(
                    value = word,
                    onValueChange = { word = it; wordError = false },
                    label = { Text(stringResource(R.string.clue_dialog_word_hint)) },
                    isError = wordError,
                    supportingText = if (wordError) {
                        { Text(stringResource(R.string.clue_error_blank_word)) }
                    } else null,
                    // Без принудительной капитализации — кириллица вводится свободно
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = countText,
                    onValueChange = { countText = it; countError = false },
                    label = { Text(stringResource(R.string.clue_dialog_count_hint)) },
                    isError = countError,
                    supportingText = if (countError) {
                        { Text(stringResource(R.string.clue_error_invalid_count)) }
                    } else null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { trySubmit() }),
                    singleLine = true,
                    modifier = Modifier.width(100.dp),
                )
            }
            Button(
                onClick = { trySubmit() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.clue_dialog_submit))
            }
        }
    }
}
