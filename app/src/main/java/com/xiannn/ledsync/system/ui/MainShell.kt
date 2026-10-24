package com.xiannn.ledsync.system.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.xiannn.ledsync.system.ui.components.FigmaDock
import com.xiannn.ledsync.system.ui.components.NavTab
import com.xiannn.ledsync.system.ui.screens.AppSyncScreen
import com.xiannn.ledsync.system.ui.screens.AudioReactiveScreen
import com.xiannn.ledsync.system.ui.screens.BatteryStudioScreen
import com.xiannn.ledsync.system.ui.screens.HardwareLabScreen
import com.xiannn.ledsync.system.ui.screens.HomeScreen
import com.xiannn.ledsync.system.ui.screens.PerformanceScreen
import com.xiannn.ledsync.system.ui.screens.TweaksScreen
import com.xiannn.ledsync.system.ui.theme.BgLight

enum class ScreenRoute {
    HOME,
    HARDWARE_LAB,
    TWEAKS,
    PERFORMANCE,
    APP_SYNC,
    BATTERY_STUDIO,
    AUDIO_REACTIVE
}

@Composable
fun MainShell() {
    val backStack = remember { mutableStateListOf(ScreenRoute.HOME) }
    val currentRoute = backStack.lastOrNull() ?: ScreenRoute.HOME

    fun navigateTo(route: ScreenRoute) {
        if (currentRoute != route) {
            backStack.add(route)
        }
    }

    fun handleBack() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    // Intercept system back navigation: pop to previous screen when not at root HOME
    BackHandler(enabled = backStack.size > 1) {
        handleBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgLight)
            .statusBarsPadding()
    ) {
        when (currentRoute) {
            ScreenRoute.HOME -> {
                HomeScreen(
                    viewModel = hiltViewModel(),
                    onNavigateToAudio = { navigateTo(ScreenRoute.AUDIO_REACTIVE) },
                    onNavigateToAppSync = { navigateTo(ScreenRoute.APP_SYNC) },
                    onNavigateToBattery = { navigateTo(ScreenRoute.BATTERY_STUDIO) },
                    onNavigateToPerf = { navigateTo(ScreenRoute.PERFORMANCE) }
                )
            }
            ScreenRoute.HARDWARE_LAB -> {
                HardwareLabScreen(viewModel = hiltViewModel())
            }
            ScreenRoute.TWEAKS -> {
                TweaksScreen(
                    viewModel = hiltViewModel(),
                    onNavigateToAudio = { navigateTo(ScreenRoute.AUDIO_REACTIVE) },
                    onNavigateToAppSync = { navigateTo(ScreenRoute.APP_SYNC) },
                    onNavigateToBattery = { navigateTo(ScreenRoute.BATTERY_STUDIO) }
                )
            }
            ScreenRoute.PERFORMANCE -> {
                PerformanceScreen(
                    viewModel = hiltViewModel(),
                    onBack = { handleBack() }
                )
            }
            ScreenRoute.APP_SYNC -> {
                AppSyncScreen(
                    viewModel = hiltViewModel(),
                    onBack = { handleBack() }
                )
            }
            ScreenRoute.BATTERY_STUDIO -> {
                BatteryStudioScreen(
                    viewModel = hiltViewModel(),
                    onBack = { handleBack() }
                )
            }
            ScreenRoute.AUDIO_REACTIVE -> {
                AudioReactiveScreen(
                    viewModel = hiltViewModel(),
                    onBack = { handleBack() }
                )
            }
        }

        // Show dock only when on one of the 3 primary tabs
        val showDock = currentRoute in listOf(ScreenRoute.HOME, ScreenRoute.HARDWARE_LAB, ScreenRoute.TWEAKS)
        AnimatedVisibility(
            visible = showDock,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        ) {
            FigmaDock(
                currentTab = when (currentRoute) {
                    ScreenRoute.HARDWARE_LAB -> NavTab.LEDS
                    ScreenRoute.TWEAKS -> NavTab.TWEAKS
                    else -> NavTab.HOME
                },
                onTabSelected = { tab ->
                    val targetRoute = when (tab) {
                        NavTab.HOME -> ScreenRoute.HOME
                        NavTab.LEDS -> ScreenRoute.HARDWARE_LAB
                        NavTab.TWEAKS -> ScreenRoute.TWEAKS
                    }
                    if (targetRoute == ScreenRoute.HOME) {
                        backStack.clear()
                        backStack.add(ScreenRoute.HOME)
                    } else if (currentRoute != targetRoute) {
                        if (backStack.size > 1 && backStack.last() in listOf(ScreenRoute.HARDWARE_LAB, ScreenRoute.TWEAKS)) {
                            backStack.removeAt(backStack.lastIndex)
                        }
                        backStack.add(targetRoute)
                    }
                }
            )
        }
    }
}
