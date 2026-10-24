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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Battery5Bar
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiannn.ledsync.system.ui.components.DeviceGraphic
import com.xiannn.ledsync.system.ui.theme.AccentContainer
import com.xiannn.ledsync.system.ui.theme.CardShapeLarge
import com.xiannn.ledsync.system.ui.theme.HeroCardShape
import com.xiannn.ledsync.system.ui.theme.OnAccentContainer
import com.xiannn.ledsync.system.ui.theme.OnPrimary
import com.xiannn.ledsync.system.ui.theme.OnPrimaryContainer
import com.xiannn.ledsync.system.ui.theme.OnSecondaryContainer
import com.xiannn.ledsync.system.ui.theme.OnTertiaryContainer
import com.xiannn.ledsync.system.ui.theme.OnWarningContainer
import com.xiannn.ledsync.system.ui.theme.PillShape
import com.xiannn.ledsync.system.ui.theme.Primary
import com.xiannn.ledsync.system.ui.theme.PrimaryContainer
import com.xiannn.ledsync.system.ui.theme.SecondaryContainer
import com.xiannn.ledsync.system.ui.theme.Surface2
import com.xiannn.ledsync.system.ui.theme.Surface3
import com.xiannn.ledsync.system.ui.theme.TertiaryContainer
import com.xiannn.ledsync.system.ui.theme.TextPrimary
import com.xiannn.ledsync.system.ui.theme.TextSecondary
import com.xiannn.ledsync.system.ui.theme.TileShapeAsym1
import com.xiannn.ledsync.system.ui.theme.TileShapeAsym2
import com.xiannn.ledsync.system.ui.theme.WarningContainer

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToAudio: () -> Unit,
    onNavigateToAppSync: () -> Unit,
    onNavigateToBattery: () -> Unit,
    onNavigateToPerf: () -> Unit,
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
                text = "LED Sync",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-1).sp,
                color = TextPrimary
            )
            Text(
                text = "Device overview",
                fontSize = 15.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Hero Device Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(HeroCardShape)
                .background(Surface2)
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(177.dp)
                ) {
                    DeviceGraphic(
                        modifier = Modifier.fillMaxSize(),
                        isActive = state.masterEnabled
                    )

                    // Auto · Normal Chip
                    Box(
                        modifier = Modifier
                            .padding(start = 24.dp, top = 22.dp)
                            .clip(PillShape)
                            .background(PrimaryContainer)
                            .padding(horizontal = 13.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Auto · Normal",
                            color = OnPrimaryContainer,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Master Power Button
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 18.dp, top = 18.dp)
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (state.masterEnabled) Primary else Surface3)
                            .clickable { viewModel.toggleMasterPower() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PowerSettingsNew,
                            contentDescription = "Power",
                            tint = if (state.masterEnabled) OnPrimary else TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Device Specs & Status
                Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 24.dp)) {
                    Text(
                        text = state.deviceModel,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = state.androidVersion,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = state.kernelRelease,
                        fontSize = 12.sp,
                        color = TextSecondary.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(top = 14.dp)
                            .clip(PillShape)
                            .background(if (state.masterEnabled) Primary else Surface3)
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(if (state.masterEnabled) OnPrimary else TextSecondary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (state.masterEnabled) "LED service: ACTIVE" else "LED service: PAUSED",
                            color = if (state.masterEnabled) OnPrimary else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Row 1: Battery & Sysfs Tiles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ExpressiveActionTile(
                icon = Icons.Rounded.Battery5Bar,
                title = "Battery",
                value = "${state.batteryPct}%",
                bgColor = AccentContainer,
                textColor = OnAccentContainer,
                shape = TileShapeAsym1,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigateToBattery() }
            )

            ExpressiveActionTile(
                icon = Icons.Rounded.Lightbulb,
                title = "Sysfs",
                value = if (state.isSysfsReady) "Active" else "Standby",
                bgColor = TertiaryContainer,
                textColor = OnTertiaryContainer,
                shape = TileShapeAsym2,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Row 2: Audio Reactive Full Width Tile
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(TileShapeAsym2)
                .background(Primary)
                .clickable { onNavigateToAudio() }
                .padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(OnPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.GraphicEq,
                        contentDescription = "Audio",
                        tint = Primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Audio Reactive LED",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnPrimary
                    )
                    Text(
                        text = if (state.audioLedActive) "Reactive mode active" else "Spectrum or static white",
                        fontSize = 13.sp,
                        color = OnPrimary.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Row 3: App LED Sync & Battery LED Studio Tiles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ExpressiveActionTile(
                icon = Icons.Rounded.Apps,
                title = "App Sync",
                value = "Per-app",
                bgColor = SecondaryContainer,
                textColor = OnSecondaryContainer,
                shape = TileShapeAsym1,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigateToAppSync() }
            )

            ExpressiveActionTile(
                icon = Icons.Rounded.Bolt,
                title = "Battery Studio",
                value = "Thresholds",
                bgColor = WarningContainer,
                textColor = OnWarningContainer,
                shape = TileShapeAsym2,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigateToBattery() }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Row 4: CPU / RAM Load Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CardShapeLarge)
                .background(Surface2)
                .clickable { onNavigateToPerf() }
                .padding(20.dp)
        ) {
            Row(
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
                    Icon(
                        imageVector = Icons.Rounded.Speed,
                        contentDescription = "Perf",
                        tint = Primary
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "CPU load",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${state.cpuUsagePct}%",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                    Text(
                        text = "RAM: ${state.ramUsedGb} / ${state.ramTotalGb} GB",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ExpressiveActionTile(
    icon: ImageVector,
    title: String,
    value: String,
    bgColor: Color,
    textColor: Color,
    shape: androidx.compose.ui.graphics.Shape,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(bgColor)
            .padding(18.dp)
            .height(104.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = OnPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    color = textColor.copy(alpha = 0.8f)
                )
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textColor
                )
            }
        }
    }
}
