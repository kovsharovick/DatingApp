package com.example.androiddatingapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.androiddatingapp.ui.components.BottomTabs
import com.example.androiddatingapp.ui.home.HomeScreen
import com.example.androiddatingapp.ui.messages.MessagesScreen
import com.example.androiddatingapp.ui.model.ScreenInfo
import com.example.androiddatingapp.ui.profile.ProfileScreen
import com.example.androiddatingapp.ui.util.rememberScreenScale

@Composable
fun AppRoot(
    screen: ScreenInfo,
    modifier: Modifier = Modifier
) {
    val scale = rememberScreenScale(screen)
    var selectedTab by remember { mutableIntStateOf(0) }
    var isLoggedIn by remember { mutableStateOf(true) }
    var isProfileActive by remember { mutableStateOf(true) }
    var openProfileSettings by remember { mutableStateOf(false) }

    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (!isLoggedIn) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(scale.dp(16f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Вы вышли из аккаунта",
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(scale.dp(12f)))
                    Button(onClick = { isLoggedIn = true }) {
                        Text(text = "Войти (заглушка)")
                    }
                }
            }
        } else {
            when (selectedTab) {
                0 -> HomeScreen(
                    isProfileActive = isProfileActive,
                    onOpenSettings = {
                        selectedTab = 2
                        openProfileSettings = true
                    },
                    scaleDp = scale.dp,
                    scaleSp = scale.sp,
                    modifier = Modifier.weight(1f)
                )
                1 -> MessagesScreen(
                    isProfileActive = isProfileActive,
                    onOpenSettings = {
                        selectedTab = 2
                        openProfileSettings = true
                    },
                    scaleDp = scale.dp,
                    scaleSp = scale.sp,
                    modifier = Modifier.weight(1f)
                )
                else -> ProfileScreen(
                    isProfileActive = isProfileActive,
                    openSettings = openProfileSettings,
                    onOpenSettingsConsumed = { openProfileSettings = false },
                    onToggleProfileActive = { isProfileActive = it },
                    onLogout = {
                        isLoggedIn = false
                        selectedTab = 0
                        openProfileSettings = false
                    },
                    scaleDp = scale.dp,
                    scaleSp = scale.sp,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(scale.dp(8f)))

            BottomTabs(
                selectedTab = selectedTab,
                onSelect = { selectedTab = it },
                scaleDp = scale.dp,
                scaleSp = scale.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = scale.dp(12f), vertical = scale.dp(10f))
            )
        }
    }
}

