package com.xiannn.ledsync.system.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.BatterySaver
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DoNotDisturb
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.ScreenRotation
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiannn.ledsync.system.ui.theme.CardShapeLarge
import com.xiannn.ledsync.system.ui.theme.OnPrimary
import com.xiannn.ledsync.system.ui.theme.Primary
import com.xiannn.ledsync.system.ui.theme.SecondaryContainer
import com.xiannn.ledsync.system.ui.theme.Surface2
import com.xiannn.ledsync.system.ui.theme.Surface3
import com.xiannn.ledsync.system.ui.theme.TextPrimary
import com.xiannn.ledsync.system.ui.theme.TextSecondary
import com.xiannn.ledsync.system.ui.theme.WarningContainer

@Composable
fun TweaksScreen(
    viewModel: TweaksViewModel,
    onNavigateToAudio: () -> Unit,
    onNavigateToAppSync: () -> Unit,
    onNavigateToBattery: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 18.dp, vertical = 20.dp)
            .padding(bottom = 80.dp)
    ) {
        // Header
        Column(modifier = Modifier.padding(bottom = 20.dp)) {
            Text(
                text = "Tweaks",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-1).sp,
                color = TextPrimary
            )
            Text(
                text = "Modes, triggers and preferences",
                fontSize = 15.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Section: Modes
        Text(
            text = "Modes & Quick Access",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CardShapeLarge)
                .background(Surface2)
        ) {
            TweakNavigationRow(
                icon = Icons.Rounded.GraphicEq,
                title = "Audio Reactive LED",
                subtitle = "Spectrum and static modes",
                onClick = onNavigateToAudio
            )
            TweakNavigationRow(
                icon = Icons.Rounded.Apps,
                title = "App LED Sync",
                subtitle = "Per-app notification triggers",
                onClick = onNavigateToAppSync
            )
            TweakNavigationRow(
                icon = Icons.Rounded.Bolt,
                title = "Battery LED Studio",
                subtitle = "Charging and power thresholds",
                onClick = onNavigateToBattery
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: Theme Segmented Buttons
        Text(
            text = "Theme",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Surface3)
                .padding(4.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("SYSTEM" to "System", "LIGHT" to "Light", "DARK" to "Dark").forEach { (key, label) ->
                    val selected = state.themeMode == key
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (selected) Primary else Surface3)
                            .clickable { viewModel.setThemeMode(key) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (selected) OnPrimary else TextSecondary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: Sensors & System Invariants (Stock Parity)
        Text(
            text = "Sensors & System Invariants",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CardShapeLarge)
                .background(Surface2)
        ) {
            TweakSwitchRow(
                icon = Icons.Rounded.ScreenRotation,
                title = "Flip-to-Mute",
                subtitle = "Suppress LEDs when phone is face-down",
                checked = state.faceDownMute,
                onCheckedChange = { viewModel.toggleFaceDownMute(it) }
            )

            TweakSwitchRow(
                icon = Icons.Rounded.CameraAlt,
                title = "Rear Camera Suppression",
                subtitle = "Turn off rear LEDs while shooting photos/videos",
                checked = state.cameraSuppression,
                onCheckedChange = { viewModel.toggleCameraSuppression(it) }
            )

            TweakSwitchRow(
                icon = Icons.Rounded.DoNotDisturb,
                title = "Respect Do Not Disturb",
                subtitle = "Silence lighting during Zen Mode / DND",
                checked = state.respectDnd,
                onCheckedChange = { viewModel.toggleRespectDnd(it) }
            )

            TweakSwitchRow(
                icon = Icons.Rounded.BatterySaver,
                title = "Respect Battery Saver",
                subtitle = "Disable ambient LEDs on Low Power mode",
                checked = state.respectBatterySaver,
                onCheckedChange = { viewModel.toggleRespectBatterySaver(it) }
            )
        }
    }
}

@Composable
fun TweakNavigationRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = OnPrimary)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(text = subtitle, fontSize = 13.sp, color = TextSecondary)
        }
        Icon(imageVector = Icons.Rounded.ChevronRight, contentDescription = "Open", tint = TextSecondary)
    }
}

@Composable
fun TweakSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Surface3),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = Primary)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(text = subtitle, fontSize = 13.sp, color = TextSecondary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = OnPrimary,
                checkedTrackColor = Primary
            )
        )
    }
}
