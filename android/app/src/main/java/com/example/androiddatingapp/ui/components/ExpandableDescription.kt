package com.example.androiddatingapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

private const val COLLAPSED_MAX_LINES = 3

@Composable
fun ExpandableDescription(
    text: String,
    fontSize: TextUnit,
    textColor: Color,
    linkColor: Color,
    modifier: Modifier = Modifier,
    collapsedMaxLines: Int = COLLAPSED_MAX_LINES,
) {
    if (text.isBlank()) return

    var expanded by remember(text) { mutableStateOf(false) }
    var canExpand by remember(text) { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = text,
            fontSize = fontSize,
            color = textColor,
            maxLines = if (expanded) Int.MAX_VALUE else collapsedMaxLines,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { result ->
                if (!expanded) {
                    canExpand = result.hasVisualOverflow
                }
            }
        )

        if (canExpand || expanded) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (expanded) "Свернуть" else "Развернуть",
                fontSize = fontSize,
                color = linkColor,
                modifier = Modifier.clickable { expanded = !expanded }
            )
        }
    }
}
