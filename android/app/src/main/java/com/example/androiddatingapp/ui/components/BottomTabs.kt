package com.example.androiddatingapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit

@Composable
fun BottomTabs(
    selectedTab: Int,
    onSelect: (Int) -> Unit,
    inboxHasUnread: Boolean,
    scaleDp: (Float) -> Dp,
    scaleSp: (Float) -> TextUnit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(scaleDp(64f))
            .clip(RoundedCornerShape(scaleDp(18f)))
            .background(MaterialTheme.colorScheme.surface),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        TabButton(
            title = "Лента",
            selected = selectedTab == 0,
            onClick = { onSelect(0) },
            scaleDp = scaleDp,
            scaleSp = scaleSp,
            modifier = Modifier.weight(1f)
        )
        TabButton(
            title = "Входящие",
            selected = selectedTab == 1,
            showBadge = inboxHasUnread,
            onClick = { onSelect(1) },
            scaleDp = scaleDp,
            scaleSp = scaleSp,
            modifier = Modifier.weight(1f)
        )
        TabButton(
            title = "Профиль",
            selected = selectedTab == 2,
            onClick = { onSelect(2) },
            scaleDp = scaleDp,
            scaleSp = scaleSp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TabButton(
    title: String,
    selected: Boolean,
    showBadge: Boolean = false,
    onClick: () -> Unit,
    scaleDp: (Float) -> Dp,
    scaleSp: (Float) -> TextUnit,
    modifier: Modifier = Modifier
) {
    val container = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val content = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier.padding(scaleDp(6f)),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(scaleDp(14f))),
            colors = ButtonDefaults.buttonColors(
                containerColor = container,
                contentColor = content
            ),
            contentPadding = PaddingValues(horizontal = scaleDp(8f), vertical = scaleDp(6f))
        ) {
            Text(
                text = title,
                fontSize = scaleSp(13f),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Clip
            )
        }
        if (showBadge) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = scaleDp(-4f), y = scaleDp(2f))
                    .size(scaleDp(8f))
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary)
            )
        }
    }
}

