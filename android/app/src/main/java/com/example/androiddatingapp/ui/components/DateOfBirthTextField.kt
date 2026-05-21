package com.example.androiddatingapp.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.TextUnit
import com.example.androiddatingapp.ui.util.DateOfBirthInput

@Composable
fun DateOfBirthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    scaleSp: (Float) -> TextUnit,
    modifier: Modifier = Modifier,
    label: String = "Дата рождения",
) {
    var digits by remember(value) {
        mutableStateOf(value.filter { it.isDigit() }.take(8))
    }
    val displayText = DateOfBirthInput.formatFromDigits(digits)
    var fieldValue by remember(value) {
        mutableStateOf(TextFieldValue(displayText, TextRange(displayText.length)))
    }

    OutlinedTextField(
        value = fieldValue,
        onValueChange = { newValue ->
            digits = DateOfBirthInput.updateDigits(digits, newValue.text)
            val display = DateOfBirthInput.formatFromDigits(digits)
            fieldValue = TextFieldValue(display, TextRange(display.length))
            onValueChange(display)
        },
        label = { Text(label, fontSize = scaleSp(12f)) },
        placeholder = { Text("ДД-ММ-ГГГГ", fontSize = scaleSp(12f)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier.fillMaxWidth(),
    )
}
