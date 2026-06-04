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
import com.example.androiddatingapp.data.DatingRepository
import com.example.androiddatingapp.data.MediaUrlResolver
import com.example.androiddatingapp.data.api.stomp.StompChatClient
import com.example.androiddatingapp.data.DatingRepository.Companion.mergeProfile
import com.example.androiddatingapp.data.SessionStore
import com.example.androiddatingapp.data.api.dto.MessageDto
import com.example.androiddatingapp.ui.auth.LoginScreen
import com.example.androiddatingapp.ui.auth.OnboardingScreen
import com.example.androiddatingapp.ui.auth.RegisterScreen
import com.example.androiddatingapp.ui.components.AppSplashScreen
import com.example.androiddatingapp.ui.components.BottomTabs
import com.example.androiddatingapp.ui.home.HomeScreen
import com.example.androiddatingapp.ui.messages.ChatUi
import com.example.androiddatingapp.ui.messages.MessageUi
import com.example.androiddatingapp.ui.messages.MessagesScreen
import com.example.androiddatingapp.ui.messages.rememberInboxHasUnread
import com.example.androiddatingapp.data.SubscriptionStatus
import com.example.androiddatingapp.ui.model.ScreenInfo
import com.example.androiddatingapp.ui.model.UserAccount
import com.example.androiddatingapp.ui.model.UserPreferences
import com.example.androiddatingapp.ui.profile.ProfileScreen
import com.example.androiddatingapp.ui.profile.UserProfileUi
import com.example.androiddatingapp.ui.util.rememberScreenScale
import kotlinx.coroutines.delay
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
    val datingRepository = remember { DatingRepository() }
    val stompChatClient = remember { StompChatClient() }
    val sessionStore = remember { SessionStore(context.applicationContext) }
    val authRepository = remember {
        AuthRepository(
            sessionStore = sessionStore,
            datingRepository = datingRepository,
        )
    }

    var session by remember { mutableStateOf<UserAccount?>(null) }
    var authMode by remember { mutableStateOf(AuthMode.Login) }
    var authError by remember { mutableStateOf<String?>(null) }
    var authLoading by remember { mutableStateOf(false) }
    var restoringSession by remember { mutableStateOf(true) }
    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(1200)
        showSplash = false
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    var feedRefreshToken by remember { mutableIntStateOf(0) }
    var openProfileSettings by remember { mutableStateOf(false) }
    var openProfileSubscription by remember { mutableStateOf(false) }
    var openProfilePreferences by remember { mutableStateOf(false) }
    var swipedUserIds by remember { mutableStateOf(setOf<Long>()) }

    val searchCities: suspend (String) -> Result<List<String>> = remember(datingRepository, context) {
        { query -> datingRepository.searchCities(context, query) }
    }
    suspend fun syncSubscription(account: UserAccount): UserAccount {
        return datingRepository.getSubscription()
            .map { sub -> DatingRepository.accountWithSubscription(account, sub) }
            .getOrElse { account }
    }

    fun openProfileTab(
        openSettings: Boolean = false,
        openSubscription: Boolean = false,
        openPreferences: Boolean = false,
    ) {
        selectedTab = 2
        if (openSettings) openProfileSettings = true
        if (openSubscription && session?.hasActivePremium() != true) {
            openProfileSubscription = true
        }
        if (openPreferences) openProfilePreferences = true
    }

    LaunchedEffect(authRepository) {
        restoringSession = true
        authRepository.restoreSession()
            .onSuccess { account -> session = syncSubscription(account) }
        restoringSession = false
    }

    LaunchedEffect(session?.authToken) {
        val token = session?.authToken
        if (token.isNullOrBlank()) {
            stompChatClient.disconnect()
        } else {
            stompChatClient.connect(token)
        }
    }

    if (showSplash) {
        AppSplashScreen(scaleDp = scale.dp, modifier = modifier.fillMaxSize())
        return
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
                                        session = syncSubscription(account)
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
                        onRegister = { email, password, name, dateOfBirth, gender, city, description ->
                            authError = null
                            authLoading = true
                            scope.launch {
                                authRepository.register(
                                    email, password, name, dateOfBirth, gender, city, description,
                                )
                                    .onSuccess { account ->
                                        session = syncSubscription(account)
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
                    account = user,
                    hasVideoAlready = user.hasVideo,
                    onUploadVideo = { uri ->
                        datingRepository.uploadVideo(context, uri).map { profile ->
                            session = user.mergeProfile(profile, user.email)
                                .copy(onboardingCompleted = false)
                            Unit
                        }
                    },
                    onSavePreferences = { prefs ->
                        val current = session ?: user
                        datingRepository.updatePreferences(current.email, current, prefs).map {
                            session = it.copy(onboardingCompleted = false)
                            Unit
                        }
                    },
                    onSkipPreferences = {
                        val current = session ?: user
                        val defaults = UserPreferences.skipDefaults(
                            current.gender,
                            current.ageYears(),
                        )
                        datingRepository.updatePreferences(current.email, current, defaults).map {
                            session = it.copy(onboardingCompleted = false)
                            Unit
                        }
                    },
                    onFinished = {
                        scope.launch {
                            val current = session ?: user
                            session = datingRepository.refreshAccount(current.email, current)
                                .map { refreshed -> refreshed.copy(onboardingCompleted = true) }
                                .getOrElse { current.copy(onboardingCompleted = true) }
                            feedRefreshToken++
                        }
                    },
                    scaleDp = scale.dp,
                    scaleSp = scale.sp,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            else -> {
                val user = session!!

                val inboxHasUnread = rememberInboxHasUnread(
                    loadMatches = {
                        datingRepository.getMatches().map { list ->
                            list.map { it.toChatUi() }
                        }
                    },
                )

                LaunchedEffect(selectedTab, session?.userId) {
                    val current = session ?: return@LaunchedEffect
                    when (selectedTab) {
                        0 -> feedRefreshToken++
                        2 -> datingRepository.refreshAccount(current.email, current)
                            .onSuccess { refreshed -> session = syncSubscription(refreshed) }
                    }
                }

                LaunchedEffect(user.userId) {
                    val uid = user.userId ?: return@LaunchedEffect
                    swipedUserIds = sessionStore.getSwipedUserIds(uid)
                }

                LaunchedEffect(openProfileSubscription, user.hasActivePremium()) {
                    if (openProfileSubscription && user.hasActivePremium()) {
                        openProfileSubscription = false
                    }
                }

                when (selectedTab) {
                    0 -> HomeScreen(
                        hasVideo = user.hasVideo,
                        isProfileActive = user.isProfileActive,
                        canLike = user.canLike(),
                        remainingLikes = user.remainingLikes(),
                        onLikeConsumed = {
                            scope.launch {
                                session?.let { current ->
                                    session = syncSubscription(current.withLikeConsumed())
                                }
                            }
                        },
                        onOpenSubscription = {
                            if (!user.hasActivePremium()) openProfileTab(openSubscription = true)
                        },
                        hasActivePremium = user.hasActivePremium(),
                        swipedUserIds = swipedUserIds,
                        onUserSwiped = { targetId ->
                            user.userId?.let { uid ->
                                swipedUserIds = swipedUserIds + targetId
                                scope.launch { sessionStore.addSwipedUserId(uid, targetId) }
                            }
                        },
                        onUserUnswiped = { targetId ->
                            user.userId?.let { uid ->
                                swipedUserIds = swipedUserIds - targetId
                                scope.launch { sessionStore.removeSwipedUserId(uid, targetId) }
                            }
                        },
                        onOpenProfile = { openProfileTab() },
                        onOpenPreferences = { openProfileTab(openPreferences = true) },
                        onOpenSettings = { openProfileTab(openSettings = true) },
                        loadFeed = { datingRepository.getFeed() },
                        feedRefreshToken = feedRefreshToken,
                        onSwipe = { userId, like -> datingRepository.swipe(userId, like) },
                        scaleDp = scale.dp,
                        scaleSp = scale.sp,
                        modifier = Modifier.weight(1f),
                    )
                    1 -> MessagesScreen(
                        currentUserId = user.userId,
                        loadMatches = {
                            datingRepository.getMatches().map { list ->
                                list.map { it.toChatUi() }
                            }
                        },
                        loadMessages = { matchId, myUserId ->
                            datingRepository.getMessageHistory(matchId).map { messages ->
                                messages.map { it.toMessageUi(myUserId) }
                            }
                        },
                        stompChatClient = stompChatClient,
                        onIncomingMessage = { _, dto -> dto.toMessageUi(user.userId) },
                        scaleDp = scale.dp,
                        scaleSp = scale.sp,
                        modifier = Modifier.weight(1f),
                    )
                    else -> ProfileScreen(
                        account = user,
                        onAccountUpdate = { session = it },
                        onSearchCities = searchCities,
                        onSaveProfile = { profile ->
                            val updated = user.copy(
                                name = profile.name,
                                dateOfBirth = profile.dateOfBirth,
                                city = profile.city,
                                description = profile.description,
                                gender = profile.gender,
                            )
                            datingRepository.updateProfileFromAccount(
                                email = user.email,
                                account = updated,
                                dateOfBirth = profile.dateOfBirth,
                                gender = profile.gender,
                            )
                        },
                        onToggleProfileActive = { active ->
                            val current = session ?: user
                            datingRepository.setProfileHidden(hidden = !active).map { profile ->
                                current.mergeProfile(profile, current.email)
                            }
                        },
                        isPremiumActive = user.hasActivePremium(),
                        premiumExpiresLabel = SubscriptionStatus.formatExpiresLabel(user.premiumExpiresAt),
                        onActivatePremium = {
                            val current = session ?: user
                            datingRepository.activatePremium(UserAccount.PRO_DURATION_DAYS).map {
                                val sub = datingRepository.getSubscription().getOrThrow()
                                DatingRepository.accountWithSubscription(current, sub)
                            }
                        },
                        onUploadVideo = { uri ->
                            val current = session ?: user
                            datingRepository.uploadVideo(context, uri).map { profile ->
                                current.mergeProfile(profile, current.email)
                            }
                        },
                        onUploadAvatar = { uri ->
                            val current = session ?: user
                            datingRepository.uploadAvatar(context, uri).map { profile ->
                                current.mergeProfile(profile, current.email)
                            }
                        },
                        openSettings = openProfileSettings,
                        onOpenSettingsConsumed = { openProfileSettings = false },
                        openSubscription = openProfileSubscription && !user.hasActivePremium(),
                        openPreferences = openProfilePreferences,
                        onOpenPreferencesConsumed = { openProfilePreferences = false },
                        onSavePreferences = { prefs ->
                            val current = session ?: user
                            datingRepository.updatePreferences(current.email, current, prefs)
                                .map { updated ->
                                    session = updated
                                    feedRefreshToken++
                                    updated
                                }
                        },
                        onOpenSubscriptionConsumed = { openProfileSubscription = false },
                        onLogout = {
                            stompChatClient.disconnect()
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

private fun com.example.androiddatingapp.data.api.dto.MatchDto.toChatUi(): ChatUi = ChatUi(
    matchId = matchId,
    name = partnerName,
    avatarUrl = MediaUrlResolver.resolve(partnerAvatarUrl),
    lastMessage = lastMessagePreview.orEmpty(),
    time = DatingRepository.formatDateTime(matchedAt),
    unreadCount = 0,
    messages = emptyList(),
)

private fun MessageDto.toMessageUi(currentUserId: Long?): MessageUi = MessageUi(
    fromMe = currentUserId != null && senderId == currentUserId,
    text = content,
    time = DatingRepository.formatDateTime(sentAt),
)
