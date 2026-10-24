package com.xiannn.ledsync.system.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
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
import com.xiannn.ledsync.system.ui.theme.TileShapeHero
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
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .padding(bottom = 74.dp)
    ) {
        // Top Header
        Column(modifier = Modifier.padding(bottom = 8.dp)) {
            Text(
                text = "LED Sync",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp,
                color = TextPrimary
            )
            Text(
                text = "Device overview",
                fontSize = 13.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 1.dp)
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
                        .height(135.dp)
                        .clipToBounds()
                ) {
                    DeviceGraphic(
                        modifier = Modifier.fillMaxSize(),
                        isActive = state.masterEnabled
                    )

                    // Auto · Normal Mode Chip
                    Box(
                        modifier = Modifier
                            .padding(start = 16.dp, top = 14.dp)
                            .clip(PillShape)
                            .background(PrimaryContainer)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Auto · Normal",
                            color = OnPrimaryContainer,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Master Power Button
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 14.dp, top = 12.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (state.masterEnabled) Primary else Surface3)
                            .clickable { viewModel.toggleMasterPower() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PowerSettingsNew,
                            contentDescription = "Power",
                            tint = if (state.masterEnabled) OnPrimary else TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Device Specs & Status
                Column(modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 14.dp)) {
                    Text(
                        text = state.deviceModel,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        letterSpacing = (-0.4).sp
                    )
                    Text(
                        text = state.androidVersion,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 1.dp)
                    )
                    Text(
                        text = state.kernelRelease,
                        fontSize = 11.sp,
                        color = TextSecondary.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 1.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .clip(PillShape)
                            .background(if (state.masterEnabled) Primary else Surface3)
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (state.masterEnabled) OnPrimary else TextSecondary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (state.masterEnabled) "LED service: ACTIVE" else "LED service: PAUSED",
                            color = if (state.masterEnabled) OnPrimary else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
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
                height = 105.dp,
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
                height = 105.dp,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Section: Controls Header
        Text(
            text = "Controls",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        // Row 2 (Figma 2-Column Controls layout): Left = Audio Reactive, Right = Stacked App Sync & Battery Studio
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(176.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Left Column: Audio Reactive LED Tile
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(TileShapeHero)
                    .background(Primary)
                    .clickable { onNavigateToAudio() }
                    .padding(14.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(OnPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.GraphicEq,
                                contentDescription = "Audio Reactive",
                                tint = Primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        if (state.audioLedActive) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF7EE787))
                            )
                        }
                    }

                    // Dynamic 8-bar Equalizer visualization
                    EqualizerBars(
                        isActive = state.audioLedActive,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                    )

                    Column {
                        Text(
                            text = "Audio Reactive LED",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnPrimary
                        )
                        Text(
                            text = if (state.audioLedActive) "Reactive mode active" else "Spectrum or static",
                            fontSize = 11.sp,
                            color = OnPrimary.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Right Column: App LED Sync & Battery LED Studio Tiles stacked vertically
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // App LED Sync Tile
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(TileShapeAsym1)
                        .background(SecondaryContainer)
                        .clickable { onNavigateToAppSync() }
                        .padding(12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Apps,
                                    contentDescription = "App Sync",
                                    tint = OnPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Icon(
                                imageVector = Icons.Rounded.ChevronRight,
                                contentDescription = null,
                                tint = OnSecondaryContainer.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "App LED Sync",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnSecondaryContainer
                            )
                            Text(
                                text = "Per-app notifications",
                                fontSize = 11.sp,
                                color = OnSecondaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // Battery LED Studio Tile
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(TileShapeAsym2)
                        .background(WarningContainer)
                        .clickable { onNavigateToBattery() }
                        .padding(12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Bolt,
                                    contentDescription = "Battery Studio",
                                    tint = OnPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Icon(
                                imageVector = Icons.Rounded.ChevronRight,
                                contentDescription = null,
                                tint = OnWarningContainer.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Battery Studio",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnWarningContainer
                            )
                            Text(
                                text = "Power thresholds",
                                fontSize = 11.sp,
                                color = OnWarningContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Section: System Performance Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "System performance",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(PillShape)
                    .background(SecondaryContainer)
                    .clickable { onNavigateToPerf() }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Live Monitor",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSecondaryContainer
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = "Navigate",
                    tint = OnSecondaryContainer,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // Section: System Performance Live Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CardShapeLarge)
                .background(Surface2)
                .clickable { onNavigateToPerf() }
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // CPU Load Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CPU load",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = "${state.cpuUsagePct}%",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }

            // CPU Load Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 10.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Surface3)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = (state.cpuUsagePct / 100f).coerceIn(0.05f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(Primary)
                )
            }

            // RAM Allocation Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RAM allocation",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = "${state.ramUsedGb} / ${state.ramTotalGb} GB",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }

            // RAM Progress Bar
            val ramFraction = if (state.ramTotalGb > 0f) {
                (state.ramUsedGb / state.ramTotalGb).coerceIn(0.05f, 1f)
            } else {
                0.5f
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Surface3)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = ramFraction)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(Primary.copy(alpha = 0.7f))
                )
            }
        }
    }
}

@Composable
fun EqualizerBars(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "eqAnim")
    val baseFractions = listOf(0.35f, 0.75f, 0.40f, 0.85f, 0.50f, 0.90f, 0.45f, 0.80f)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        baseFractions.forEachIndexed { index, base ->
            val animatedFraction = if (isActive) {
                val anim by infiniteTransition.animateFloat(
                    initialValue = 0.2f,
                    targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 350 + (index * 60),
                            easing = FastOutSlowInEasing
                        ),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "bar_$index"
                )
                (anim * base).coerceIn(0.15f, 1.0f)
            } else {
                base * 0.4f
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(fraction = animatedFraction)
                    .clip(RoundedCornerShape(2.dp))
                    .background(OnPrimary.copy(alpha = 0.85f))
            )
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
    shape: Shape,
    height: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(height)
            .clip(shape)
            .background(bgColor)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = OnPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = textColor.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = value,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textColor,
                    letterSpacing = (-0.4).sp
                )
            }
        }
    }
}
