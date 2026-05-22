package com.example.androiddatingapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.androiddatingapp.data.AuthRepository
import com.example.androiddatingapp.data.SessionStore
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
import kotlinx.coroutines.launch

private enum class AuthMode { Login, Register }

@Composable
fun AppRoot(
    screen: ScreenInfo,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scale = rememberScreenScale(screen)
    val scope = rememberCoroutineScope()
    val authRepository = remember {
        AuthRepository(sessionStore = SessionStore(context.applicationContext))
    }

    var session by remember { mutableStateOf<UserAccount?>(null) }
    var authMode by remember { mutableStateOf(AuthMode.Login) }
    var authError by remember { mutableStateOf<String?>(null) }
    var authLoading by remember { mutableStateOf(false) }
    var restoringSession by remember { mutableStateOf(true) }

    var selectedTab by remember { mutableIntStateOf(0) }
    var openProfileSettings by remember { mutableStateOf(false) }
    var openProfileSubscription by remember { mutableStateOf(false) }

    val searchCities: suspend (String) -> Result<List<String>> = remember(authRepository) {
        { prefix -> authRepository.searchCities(prefix) }
    }

    fun openProfileTab(openSettings: Boolean = false, openSubscription: Boolean = false) {
        selectedTab = 2
        if (openSettings) openProfileSettings = true
        if (openSubscription) openProfileSubscription = true
    }

    LaunchedEffect(authRepository) {
        restoringSession = true
        authRepository.restoreSession()
            .onSuccess { session = it }
        restoringSession = false
    }

    if (restoringSession) {
        Box(
            modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        when {
            session == null -> {
                when (authMode) {
                    AuthMode.Login -> LoginScreen(
                        onLogin = { email, password ->
                            authError = null
                            authLoading = true
                            scope.launch {
                                authRepository.login(email, password)
                                    .onSuccess { account ->
                                        session = account.copy(onboardingCompleted = account.hasVideo)
                                        selectedTab = 0
                                    }
                                    .onFailure { authError = it.message }
                                authLoading = false
                            }
                        },
                        onGoToRegister = {
                            authMode = AuthMode.Register
                            authError = null
                        },
                        errorMessage = authError,
                        isLoading = authLoading,
                        scaleDp = scale.dp,
                        scaleSp = scale.sp,
                        modifier = Modifier.fillMaxSize(),
                    )
                    AuthMode.Register -> RegisterScreen(
                        onRegister = { email, password, name, dateOfBirth, gender, city ->
                            authError = null
                            authLoading = true
                            scope.launch {
                                authRepository.register(email, password, name, dateOfBirth, gender, city)
                                    .onSuccess { account ->
                                        session = account.copy(onboardingCompleted = account.hasVideo)
                                        selectedTab = 0
                                        authMode = AuthMode.Login
                                    }
                                    .onFailure { authError = it.message }
                                authLoading = false
                            }
                        },
                        onBackToLogin = {
                            authMode = AuthMode.Login
                            authError = null
                        },
                        onSearchCities = searchCities,
                        errorMessage = authError,
                        isLoading = authLoading,
                        scaleDp = scale.dp,
                        scaleSp = scale.sp,
                        modifier = Modifier.fillMaxSize(),
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
                        )
                    },
                    onSkip = {
                        session = user.copy(onboardingCompleted = true)
                    },
                    scaleDp = scale.dp,
                    scaleSp = scale.sp,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            else -> {
                val user = session!!
                val inboxHasUnread = rememberInboxHasUnread()

                when (selectedTab) {
                    0 -> HomeScreen(
                        hasVideo = user.hasVideo,
                        isProfileActive = user.isProfileActive,
                        canLike = user.canLike(),
                        remainingLikes = user.remainingLikes(),
                        onLikeConsumed = { session = user.withLikeConsumed() },
                        onOpenSubscription = { openProfileTab(openSubscription = true) },
                        onOpenProfile = { openProfileTab() },
                        onOpenSettings = { openProfileTab(openSettings = true) },
                        scaleDp = scale.dp,
                        scaleSp = scale.sp,
                        modifier = Modifier.weight(1f),
                    )
                    1 -> MessagesScreen(
                        scaleDp = scale.dp,
                        scaleSp = scale.sp,
                        modifier = Modifier.weight(1f),
                    )
                    else -> ProfileScreen(
                        account = user,
                        onAccountUpdate = { session = it },
                        onSearchCities = searchCities,
                        openSettings = openProfileSettings,
                        onOpenSettingsConsumed = { openProfileSettings = false },
                        openSubscription = openProfileSubscription,
                        onOpenSubscriptionConsumed = { openProfileSubscription = false },
                        onLogout = {
                            scope.launch { authRepository.logout() }
                            session = null
                            authMode = AuthMode.Login
                            authError = null
                            selectedTab = 0
                            openProfileSettings = false
                            openProfileSubscription = false
                        },
                        scaleDp = scale.dp,
                        scaleSp = scale.sp,
                        modifier = Modifier.weight(1f),
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
                        .padding(horizontal = scale.dp(12f), vertical = scale.dp(10f)),
                )
            }
        }
    }
}
