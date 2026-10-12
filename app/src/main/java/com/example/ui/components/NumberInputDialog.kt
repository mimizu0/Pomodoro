package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Density
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.unit.dp

const val MIN_INPUT_VALUE = 1

// 円の縮小前の密度。円の中から開くダイアログまで縮小されないよう、ダイアログ側で元に戻すために使う
val LocalUnscaledDensity = staticCompositionLocalOf<Density?> { null }

@Composable
fun NumberInputDialog(
    title: String,
    initialValue: Int,
    maxValue: Int,
    unit: String,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val minValue = MIN_INPUT_VALUE
    val initialStr = initialValue.toString()
    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = initialStr,
                selection = TextRange(0, initialStr.length)
            )
        )
    }
    val focusRequester = remember { FocusRequester() }
    var isError by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalDensity provides (LocalUnscaledDensity.current ?: LocalDensity.current)) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = textFieldValue,
                        onValueChange = { input ->
                            val digits = input.text.filter { it.isDigit() }
                            textFieldValue = input.copy(text = digits)
                            val num = digits.toIntOrNull()
                            isError = num == null || num !in minValue..maxValue
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                val num = textFieldValue.text.toIntOrNull()
                                if (num != null && num in minValue..maxValue) {
                                    onConfirm(num)
                                    onDismiss()
                                }
                            }
                        ),
                        suffix = if (unit.isNotEmpty()) { { Text(unit) } } else null,
                        isError = isError,
                        supportingText = { Text("$minValue〜$maxValue$unit") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                    )

                    // 入力欄が配置された後でフォーカスを要求する（ダイアログの外側で要求すると、
                    // 中身の構築より先に実行されて例外になることがある）
                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val num = textFieldValue.text.toIntOrNull()
                        if (num != null && num in minValue..maxValue) {
                            onConfirm(num)
                            onDismiss()
                        }
                    },
                    enabled = !isError && textFieldValue.text.isNotEmpty()
                ) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("キャンセル")
                }
            }
        )
    }
}
