package com.example.ui

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.domain.Player
import com.example.ui.components.ReplayStudioModal
import com.example.ui.components.TactixAmbientBackdrop
import com.example.ui.theme.Ceramic
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.semantics.Role
import com.example.ui.screens.ArchiveScreen
import com.example.ui.screens.DailyScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PlayScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.viewmodel.ArchiveViewModel
import com.example.ui.viewmodel.DailyViewModel
import com.example.ui.viewmodel.GameViewModel

private enum class AppFlow {
    SPLASH,
    ONBOARDING,
    MAIN,
}

enum class TactixTab(
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String,
) {
    HOME(R.string.nav_home, Icons.Rounded.GridView, Icons.Outlined.GridView, "nav_home"),
    DAILY(R.string.nav_daily, Icons.Rounded.CalendarToday, Icons.Outlined.CalendarToday, "nav_daily"),
    ARCHIVE(R.string.nav_archive, Icons.Rounded.Person, Icons.Outlined.Person, "nav_archive"),
}

@Composable
fun TactixApp(
    gameViewModel: GameViewModel,
    dailyViewModel: DailyViewModel,
    archiveViewModel: ArchiveViewModel,
) {
    var appFlow by rememberSaveable { mutableStateOf(AppFlow.SPLASH) }
    var activeTab by rememberSaveable { mutableStateOf(TactixTab.HOME) }
    var isInArena by rememberSaveable { mutableStateOf(false) }

    val gameState by gameViewModel.uiState.collectAsStateWithLifecycle()
    val dailyState by dailyViewModel.uiState.collectAsStateWithLifecycle()
    val archiveState by archiveViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // System back button hierarchy:
    // 1. Replay Studio (handled inside ReplayStudioModal)
    // 2. Onboarding opened as refresher from Home -> back to Main
    BackHandler(
        enabled = appFlow == AppFlow.ONBOARDING && gameState.hasCompletedOnboarding == true,
    ) {
        appFlow = AppFlow.MAIN
    }
    // 3. Active Arena -> pause & return to Mode Hub
    BackHandler(
        enabled = appFlow == AppFlow.MAIN && isInArena && archiveState.replayStudio == null,
    ) {
        gameViewModel.pauseArena()
        isInArena = false
    }
    // 4. Secondary tabs -> return to Home Mode Hub
    BackHandler(
        enabled = appFlow == AppFlow.MAIN && !isInArena && activeTab != TactixTab.HOME && archiveState.replayStudio == null,
    ) {
        activeTab = TactixTab.HOME
    }

    // Dynamic atmospheric ambient hues reacting to the active screen & turn
    val primaryAccent = when {
        appFlow != AppFlow.MAIN -> MaterialTheme.colorScheme.primary
        isInArena -> {
            if (gameState.snapshot.winner == Player.O ||
                (!gameState.snapshot.isGameOver && gameState.snapshot.currentPlayer == Player.O)
            ) {
                MaterialTheme.colorScheme.tertiary
            } else {
                MaterialTheme.colorScheme.primary
            }
        }
        activeTab == TactixTab.DAILY && dailyState.isRushMode -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }
    val secondaryAccent = if (primaryAccent == MaterialTheme.colorScheme.primary) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.primary
    }

    TactixAmbientBackdrop(
        accentPrimary = primaryAccent,
        accentSecondary = secondaryAccent,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = appFlow,
                transitionSpec = {
                    (fadeIn(tween(320)) + scaleIn(initialScale = 0.97f, animationSpec = tween(320)))
                        .togetherWith(fadeOut(tween(220)))
                },
                label = "app_flow_transition",
                modifier = Modifier.fillMaxSize(),
            ) { flow ->
                when (flow) {
                    AppFlow.SPLASH -> {
                        SplashScreen(
                            isReady = gameState.hasCompletedOnboarding != null,
                            onFinished = {
                                appFlow = if (gameState.hasCompletedOnboarding == true) {
                                    AppFlow.MAIN
                                } else {
                                    AppFlow.ONBOARDING
                                }
                            },
                        )
                    }

                    AppFlow.ONBOARDING -> {
                        OnboardingScreen(
                            onComplete = {
                                gameViewModel.completeOnboarding()
                                appFlow = AppFlow.MAIN
                                activeTab = TactixTab.HOME
                                isInArena = false
                            },
                            onTapHaptic = gameViewModel::performTapHaptic,
                            onCelebrationHaptic = gameViewModel::performCelebrationHaptic,
                        )
                    }

                    AppFlow.MAIN -> {
                        AnimatedContent(
                            targetState = isInArena,
                            transitionSpec = {
                                (fadeIn(tween(260)) + scaleIn(initialScale = 0.98f, animationSpec = tween(260)))
                                    .togetherWith(fadeOut(tween(180)))
                            },
                            label = "hub_arena_transition",
                            modifier = Modifier.fillMaxSize(),
                        ) { inArena ->
                            if (inArena) {
                                PlayScreen(
                                    uiState = gameState,
                                    onBackToHome = {
                                        gameViewModel.pauseArena()
                                        isInArena = false
                                    },
                                    onCellTap = gameViewModel::onCellTap,
                                    onSelectMode = gameViewModel::onSelectMode,
                                    onSelectOpponent = gameViewModel::onSelectOpponent,
                                    onSelectDifficulty = gameViewModel::onSelectDifficulty,
                                    onToggleBlitz = gameViewModel::onToggleBlitz,
                                    onSwapHumanSide = gameViewModel::onSwapHumanSide,
                                    onRequestHint = gameViewModel::onRequestHint,
                                    onDismissHint = gameViewModel::onDismissHint,
                                    onNewRound = gameViewModel::startNewRound,
                                    onToggleHaptics = gameViewModel::onToggleHaptics,
                                    onOpenReplay = { match -> archiveViewModel.openReplay(match, autoPlay = true) },
                                    onShareResult = {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, gameViewModel.buildShareSummary())
                                        }
                                        context.startActivity(
                                            Intent.createChooser(shareIntent, "Share Tactix Match"),
                                        )
                                    },
                                    contentBottomPadding = PaddingValues(bottom = 16.dp),
                                    modifier = Modifier.fillMaxSize(),
                                )
                            } else {
                                MainHubScaffold(
                                    activeTab = activeTab,
                                    onSelectTab = { activeTab = it },
                                    gameViewModel = gameViewModel,
                                    dailyViewModel = dailyViewModel,
                                    archiveViewModel = archiveViewModel,
                                    gameState = gameState,
                                    dailyState = dailyState,
                                    archiveState = archiveState,
                                    onLaunchMode = { mode ->
                                        gameViewModel.launchModeFromHub(mode)
                                        isInArena = true
                                    },
                                    onResumeActiveMatch = {
                                        gameViewModel.resumeArena()
                                        isInArena = true
                                    },
                                    onOpenTutorial = {
                                        gameViewModel.performTapHaptic()
                                        appFlow = AppFlow.ONBOARDING
                                    },
                                )
                            }
                        }
                    }
                }
            }

            // Full-screen Replay Studio overlay accessible from both Arena and Archive
            archiveState.replayStudio?.let { studio ->
                ReplayStudioModal(
                    studio = studio,
                    skin = archiveState.selectedSkin,
                    onClose = archiveViewModel::closeReplay,
                    onTogglePlay = archiveViewModel::toggleReplayPlayback,
                    onStepBack = { archiveViewModel.stepReplay(-1) },
                    onStepForward = { archiveViewModel.stepReplay(1) },
                    onSeek = archiveViewModel::seekReplay,
                    modifier = Modifier.statusBarsPadding(),
                )
            }
        }
    }
}

@Composable
private fun MainHubScaffold(
    activeTab: TactixTab,
    onSelectTab: (TactixTab) -> Unit,
    gameViewModel: GameViewModel,
    dailyViewModel: DailyViewModel,
    archiveViewModel: ArchiveViewModel,
    gameState: com.example.ui.viewmodel.GameUiState,
    dailyState: com.example.ui.viewmodel.DailyUiState,
    archiveState: com.example.ui.viewmodel.ArchiveUiState,
    onLaunchMode: (com.example.domain.GameMode) -> Unit,
    onResumeActiveMatch: () -> Unit,
    onOpenTutorial: () -> Unit,
) {
    val dailySolvedCount = Integer.bitCount(dailyState.solvedMask and 0b111)

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavRail = maxWidth >= 680.dp

        if (useNavRail) {
            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets.safeDrawing,
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                ) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.84f),
                    ) {
                        TactixTab.entries.forEach { tab ->
                            val selected = activeTab == tab
                            NavigationRailItem(
                                selected = selected,
                                onClick = { onSelectTab(tab) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = stringResource(tab.labelRes),
                                    )
                                },
                                label = { Text(stringResource(tab.labelRes)) },
                                modifier = Modifier.testTag(tab.testTag),
                            )
                        }
                    }

                    HubTabContent(
                        activeTab = activeTab,
                        gameViewModel = gameViewModel,
                        dailyViewModel = dailyViewModel,
                        archiveViewModel = archiveViewModel,
                        gameState = gameState,
                        dailyState = dailyState,
                        archiveState = archiveState,
                        dailySolvedCount = dailySolvedCount,
                        onLaunchMode = onLaunchMode,
                        onResumeActiveMatch = onResumeActiveMatch,
                        onOpenDaily = { onSelectTab(TactixTab.DAILY) },
                        onOpenArchive = { onSelectTab(TactixTab.ARCHIVE) },
                        onOpenTutorial = onOpenTutorial,
                        contentBottomPadding = PaddingValues(bottom = 16.dp),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                HubTabContent(
                    activeTab = activeTab,
                    gameViewModel = gameViewModel,
                    dailyViewModel = dailyViewModel,
                    archiveViewModel = archiveViewModel,
                    gameState = gameState,
                    dailyState = dailyState,
                    archiveState = archiveState,
                    dailySolvedCount = dailySolvedCount,
                    onLaunchMode = onLaunchMode,
                    onResumeActiveMatch = onResumeActiveMatch,
                    onOpenDaily = { onSelectTab(TactixTab.DAILY) },
                    onOpenArchive = { onSelectTab(TactixTab.ARCHIVE) },
                    onOpenTutorial = onOpenTutorial,
                    contentBottomPadding = PaddingValues(bottom = 104.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding(),
                )

                FloatingPillNavBar(
                    activeTab = activeTab,
                    onSelectTab = onSelectTab,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun HubTabContent(
    activeTab: TactixTab,
    gameViewModel: GameViewModel,
    dailyViewModel: DailyViewModel,
    archiveViewModel: ArchiveViewModel,
    gameState: com.example.ui.viewmodel.GameUiState,
    dailyState: com.example.ui.viewmodel.DailyUiState,
    archiveState: com.example.ui.viewmodel.ArchiveUiState,
    dailySolvedCount: Int,
    onLaunchMode: (com.example.domain.GameMode) -> Unit,
    onResumeActiveMatch: () -> Unit,
    onOpenDaily: () -> Unit,
    onOpenArchive: () -> Unit,
    onOpenTutorial: () -> Unit,
    contentBottomPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Crossfade(
        targetState = activeTab,
        animationSpec = tween(durationMillis = 220),
        label = "tactix_hub_tab_crossfade",
        modifier = modifier,
    ) { tab ->
        when (tab) {
            TactixTab.HOME -> HomeScreen(
                gameState = gameState,
                dailySolvedCount = dailySolvedCount,
                dailyStreak = dailyState.dailyStreak,
                unlockedMedalCount = archiveState.unlockedMedalCount,
                onLaunchMode = onLaunchMode,
                onResumeActiveMatch = onResumeActiveMatch,
                onOpenDaily = onOpenDaily,
                onOpenArchive = onOpenArchive,
                onOpenTutorial = onOpenTutorial,
                onConfigureOpponent = gameViewModel::configureOpponentFromHub,
                onConfigureDifficulty = gameViewModel::configureDifficultyFromHub,
                onToggleBlitz = gameViewModel::configureBlitzFromHub,
                onToggleHumanSide = gameViewModel::configureHumanSideFromHub,
                onToggleHaptics = gameViewModel::onToggleHaptics,
                contentBottomPadding = contentBottomPadding,
            )

            TactixTab.DAILY -> DailyScreen(
                uiState = dailyState,
                onSelectDailyPuzzle = dailyViewModel::onSelectDailyPuzzle,
                onStartStreakRush = dailyViewModel::onStartStreakRush,
                onReturnToDailySet = dailyViewModel::onReturnToDailySet,
                onCellTap = dailyViewModel::onCellTap,
                onRequestHint = dailyViewModel::onRequestPuzzleHint,
                onRetry = dailyViewModel::onRetryCurrentPuzzle,
                onNextPuzzle = dailyViewModel::onAdvanceNextPuzzle,
                contentBottomPadding = contentBottomPadding,
            )

            TactixTab.ARCHIVE -> ArchiveScreen(
                uiState = archiveState,
                onSelectFilter = archiveViewModel::onSelectFilter,
                onSelectPieceSkin = archiveViewModel::onSelectPieceSkin,
                onOpenReplay = { match -> archiveViewModel.openReplay(match, autoPlay = true) },
                onDeleteMatch = archiveViewModel::onDeleteMatch,
                contentBottomPadding = contentBottomPadding,
            )
        }
    }
}

@Composable
private fun FloatingPillNavBar(
    activeTab: TactixTab,
    onSelectTab: (TactixTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Ceramic bottom bar: white, rounded on top, the active tab sits on an obsidian pill.
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = Ceramic.Card,
        shadowElevation = 16.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 14.dp)
                .padding(top = 10.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TactixTab.entries.forEach { tab ->
                val selected = activeTab == tab
                val pillColor by animateColorAsState(
                    targetValue = if (selected) Ceramic.Obsidian else Color.Transparent,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "nav_pill_bg_${tab.name}",
                )
                val iconColor by animateColorAsState(
                    targetValue = if (selected) Color.White else Ceramic.Muted,
                    animationSpec = tween(200),
                    label = "nav_pill_fg_${tab.name}",
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .testTag(tab.testTag)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Tab,
                            onClick = { onSelectTab(tab) },
                        )
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 60.dp, height = 32.dp)
                            .clip(CircleShape)
                            .background(pillColor),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Text(
                        text = stringResource(tab.labelRes),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (selected) Ceramic.Obsidian else Ceramic.Muted,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
