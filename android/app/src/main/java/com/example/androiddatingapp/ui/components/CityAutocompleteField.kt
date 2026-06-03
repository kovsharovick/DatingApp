package com.example.androiddatingapp.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityAutocompleteField(
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: suspend (String) -> Result<List<String>>,
    onCitySelected: ((String) -> Unit)? = null,
    scaleSp: (Float) -> TextUnit,
    scaleDp: (Float) -> Dp,
    modifier: Modifier = Modifier,
    label: String = "Город",
    minQueryLength: Int = 1,
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
        delay(300)
        if (value.trim() != query) return@LaunchedEffect
        loading = true
        val result = onSearch(query)
        loading = false
        if (value.trim() != query) return@LaunchedEffect
        result.fold(
            onSuccess = { list ->
                suggestions = list
                expanded = list.isNotEmpty()
                if (list.isEmpty()) {
                    searchError = "Ничего не найдено. Попробуйте: Вор, Мос, СПб"
                    expanded = false
                }
            },
            onFailure = { err ->
                suggestions = emptyList()
                expanded = false
                searchError = err.message ?: "Ошибка поиска"
            },
        )
    }

    ExposedDropdownMenuBox(
        expanded = expanded && suggestions.isNotEmpty(),
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                expanded = it.trim().length >= minQueryLength
                searchError = null
            },
            label = { Text(label, fontSize = scaleSp(12f)) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            trailingIcon = {
                if (loading) {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = true)
                } else {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && suggestions.isNotEmpty())
                }
            },
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

        ExposedDropdownMenu(
            expanded = expanded && suggestions.isNotEmpty(),
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = scaleDp(260f)),
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
                        onCitySelected?.invoke(city)
                        expanded = false
                        suggestions = emptyList()
                        searchError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
