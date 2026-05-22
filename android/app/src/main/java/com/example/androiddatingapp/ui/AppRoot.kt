package com.example.androiddatingapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.androiddatingapp.ui.auth.LoginScreen
import com.example.androiddatingapp.ui.auth.OnboardingScreen
import com.example.androiddatingapp.ui.auth.RegisterScreen
import com.example.androiddatingapp.ui.components.BottomTabs
import com.example.androiddatingapp.ui.home.HomeScreen
import com.example.androiddatingapp.ui.messages.MessagesScreen
import com.example.androiddatingapp.ui.messages.rememberInboxHasUnread
import com.example.androiddatingapp.ui.model.ScreenInfo
import com.example.androiddatingapp.ui.model.UserAccount
import com.example.androiddatingapp.ui.profile.ProfileScreen
import com.example.androiddatingapp.ui.util.rememberScreenScale

private enum class AuthMode { Login, Register }

@Composable
fun AppRoot(
    screen: ScreenInfo,
    modifier: Modifier = Modifier
) {
    val scale = rememberScreenScale(screen)
    val accounts = remember { mutableStateMapOf<String, UserAccount>() }

    var session by remember { mutableStateOf<UserAccount?>(null) }
    var authMode by remember { mutableStateOf(AuthMode.Login) }
    var authError by remember { mutableStateOf<String?>(null) }

    var selectedTab by remember { mutableIntStateOf(0) }
    var openProfileSettings by remember { mutableStateOf(false) }
    var openProfileSubscription by remember { mutableStateOf(false) }

    fun openProfileTab(openSettings: Boolean = false, openSubscription: Boolean = false) {
        selectedTab = 2
        if (openSettings) openProfileSettings = true
        if (openSubscription) openProfileSubscription = true
    }

    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        when {
            session == null -> {
                when (authMode) {
                    AuthMode.Login -> LoginScreen(
                        onLogin = { email, password ->
                            authError = null
                            val account = accounts[email.trim().lowercase()]
                            when {
                                account == null -> authError = "Аккаунт не найден"
                                account.password != password -> authError = "Неверный пароль"
                                else -> {
                                    session = account
                                    selectedTab = 0
                                }
                            }
                        },
                        onGoToRegister = {
                            authMode = AuthMode.Register
                            authError = null
                        },
                        errorMessage = authError,
                        scaleDp = scale.dp,
                        scaleSp = scale.sp,
                        modifier = Modifier.fillMaxSize()
                    )
                    AuthMode.Register -> RegisterScreen(
                        onRegister = { email, password, name, dateOfBirth, gender, city ->
                            authError = null
                            val key = email.trim().lowercase()
                            if (accounts.containsKey(key)) {
                                authError = "Email уже зарегистрирован"
                            } else {
                            val account = UserAccount(
                                email = key,
                                password = password,
                                name = name,
                                dateOfBirth = dateOfBirth,
                                gender = gender,
                                city = city,
                            )
                            accounts[key] = account
                            session = account
                            selectedTab = 0
                            authMode = AuthMode.Login
                            }
                        },
                        onBackToLogin = {
                            authMode = AuthMode.Login
                            authError = null
                        },
                        errorMessage = authError,
                        scaleDp = scale.dp,
                        scaleSp = scale.sp,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            session != null && !session!!.onboardingCompleted -> {
                val user = session!!
                OnboardingScreen(
                    userName = user.name,
                    onComplete = { description, videoUploaded ->
                        session = user.copy(
                            description = description,
                            hasVideo = videoUploaded,
                            videoTitle = if (videoUploaded) "video_profile_v1.mp4" else "",
                            onboardingCompleted = true,
                        ).also { accounts[user.email] = it }
                    },
                    onSkip = {
                        session = user.copy(onboardingCompleted = true).also { accounts[user.email] = it }
                    },
                    scaleDp = scale.dp,
                    scaleSp = scale.sp,
                    modifier = Modifier.fillMaxSize()
                )
            }

            else -> {
                val user = session!!
                val inboxHasUnread = rememberInboxHasUnread()

                when (selectedTab) {
                    0 -> HomeScreen(
                        hasVideo = user.hasVideo,
                        isProfileActive = user.isProfileActive,
                        canSwipe = user.canSwipe(),
                        remainingSwipes = user.remainingSwipes(),
                        onSwipeConsumed = {
                            val updated = user.withSwipeConsumed()
                            session = updated
                            accounts[updated.email] = updated
                        },
                        onOpenSubscription = { openProfileTab(openSubscription = true) },
                        onOpenProfile = { openProfileTab() },
                        onOpenSettings = { openProfileTab(openSettings = true) },
                        scaleDp = scale.dp,
                        scaleSp = scale.sp,
                        modifier = Modifier.weight(1f)
                    )
                    1 -> MessagesScreen(
                        scaleDp = scale.dp,
                        scaleSp = scale.sp,
                        modifier = Modifier.weight(1f)
                    )
                    else -> ProfileScreen(
                        account = user,
                        onAccountUpdate = { updated ->
                            session = updated
                            accounts[updated.email] = updated
                        },
                        openSettings = openProfileSettings,
                        onOpenSettingsConsumed = { openProfileSettings = false },
                        openSubscription = openProfileSubscription,
                        onOpenSubscriptionConsumed = { openProfileSubscription = false },
                        onLogout = {
                            session = null
                            authMode = AuthMode.Login
                            authError = null
                            selectedTab = 0
                            openProfileSettings = false
                            openProfileSubscription = false
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
                    inboxHasUnread = inboxHasUnread && selectedTab != 1,
                    scaleDp = scale.dp,
                    scaleSp = scale.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = scale.dp(12f), vertical = scale.dp(10f))
                )
            }
        }
    }
}
