package com.example.androiddatingapp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.example.androiddatingapp.ui.theme.AppRedLight
import kotlinx.coroutines.delay

@Composable
fun CityAutocompleteField(
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: suspend (String) -> Result<List<String>>,
    scaleSp: (Float) -> TextUnit,
    scaleDp: (Float) -> Dp,
    modifier: Modifier = Modifier,
    label: String = "Город",
    minQueryLength: Int = 2,
) {
    var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var expanded by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var searchError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(value) {
        searchError = null
        val query = value.trim()
        if (query.length < minQueryLength) {
            suggestions = emptyList()
            expanded = false
            return@LaunchedEffect
        }
        delay(350)
        loading = true
        val result = onSearch(query)
        loading = false
        result.fold(
            onSuccess = { list ->
                suggestions = list
                expanded = list.isNotEmpty()
            },
            onFailure = { err ->
                suggestions = emptyList()
                expanded = false
                searchError = err.message
            },
        )
    }

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                expanded = it.trim().length >= minQueryLength
            },
            label = { Text(label, fontSize = scaleSp(12f)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            supportingText = when {
                loading -> {
                    { Text("Поиск…", fontSize = scaleSp(11f)) }
                }
                searchError != null -> {
                    { Text(searchError!!, fontSize = scaleSp(11f), color = AppRedLight) }
                }
                else -> null
            },
        )

        DropdownMenu(
            expanded = expanded && suggestions.isNotEmpty(),
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = scaleDp(220f)),
        ) {
            suggestions.forEach { city ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = city,
                            fontSize = scaleSp(14f),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    onClick = {
                        onValueChange(city)
                        expanded = false
                        suggestions = emptyList()
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
