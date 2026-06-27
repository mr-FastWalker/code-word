package com.codeword.app.feature.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.codeword.app.R

@Composable
fun ClueInputDialog(onSubmit: (word: String, count: Int) -> Unit) {
    var word by rememberSaveable { mutableStateOf("") }
    var countText by rememberSaveable { mutableStateOf("") }
    var wordError by rememberSaveable { mutableStateOf(false) }
    var countError by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { /* спаймастер обязан дать подсказку — закрытие заблокировано */ },
        title = { Text(stringResource(R.string.clue_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = word,
                    onValueChange = { word = it; wordError = false },
                    label = { Text(stringResource(R.string.clue_dialog_word_hint)) },
                    isError = wordError,
                    supportingText = if (wordError) {
                        { Text(stringResource(R.string.clue_error_blank_word)) }
                    } else null,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = countText,
                    onValueChange = { countText = it; countError = false },
                    label = { Text(stringResource(R.string.clue_dialog_count_hint)) },
                    isError = countError,
                    supportingText = if (countError) {
                        { Text(stringResource(R.string.clue_error_invalid_count)) }
                    } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val trimmed = word.trim()
                val count = countText.trim().toIntOrNull()
                wordError = trimmed.isBlank()
                countError = count == null || count !in 0..9
                if (!wordError && !countError) onSubmit(trimmed, count!!)
            }) {
                Text(stringResource(R.string.clue_dialog_submit))
            }
        },
    )
}
