package com.xiannn.ledsync.system.ui.screens

import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.xiannn.ledsync.system.ui.theme.Surface1
import com.xiannn.ledsync.system.ui.theme.Surface2
import com.xiannn.ledsync.system.ui.theme.Surface3
import com.xiannn.ledsync.system.ui.theme.TextPrimary
import com.xiannn.ledsync.system.ui.theme.TextSecondary

@Composable
fun AudioReactiveScreen(
    viewModel: AudioReactiveViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    val infiniteTransition = rememberInfiniteTransition(label = "eqAnim")

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .navigationBarsPadding()
    ) {
        // Back Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Surface3)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "Back", tint = TextPrimary, modifier = Modifier.size(18.dp))
            }
            Text(
                text = "Audio Reactive LED",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp,
                color = TextPrimary
            )
        }

        // Hero Card with Animated Equalizer Bars
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 12.dp, bottomStart = 28.dp, bottomEnd = 12.dp))
                .background(if (state.isEnabled) Primary else Surface2)
                .padding(14.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (state.isEnabled) OnPrimary else Primary)
                            .clickable { viewModel.toggleEnabled(!state.isEnabled) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PowerSettingsNew,
                            contentDescription = "Power",
                            tint = if (state.isEnabled) Primary else OnPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (state.isEnabled) "Audio LED Active" else "Enable Audio LED",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (state.isEnabled) OnPrimary else TextPrimary
                        )
                        Text(
                            text = if (state.isEnabled) {
                                if (state.isDynamicRgb) "Dynamic spectrum active" else "Static white mode active"
                            } else "Tap power to activate",
                            fontSize = 13.sp,
                            color = if (state.isEnabled) OnPrimary.copy(alpha = 0.8f) else TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Equalizer Bar Animation (Figma replica)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    val barFractions = listOf(0.3f, 0.67f, 0.34f, 0.71f, 0.38f, 0.75f, 0.42f, 0.79f, 0.46f, 0.83f, 0.5f, 0.87f, 0.54f, 0.91f, 0.58f, 0.95f)
                    barFractions.forEachIndexed { i, baseFrac ->
                        val animFrac by infiniteTransition.animateFloat(
                            initialValue = baseFrac * 0.4f,
                            targetValue = baseFrac,
                            animationSpec = infiniteRepeatable(
                                animation = tween(durationMillis = 350 + (i * 45), easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "eq_$i"
                        )
                        val barHeight = if (state.isEnabled) animFrac else 0.15f
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(100.dp * barHeight)
                                .clip(RoundedCornerShape(5.dp))
                                .background(if (state.isEnabled) OnPrimary.copy(alpha = 0.85f) else Surface3)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Options List Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CardShapeLarge)
                .background(Surface2)
        ) {
            AudioOptionRow(
                icon = Icons.Rounded.GraphicEq,
                title = "Dynamic RGB mode",
                subtitle = "Reactive spectrum RGB show (Party)",
                checked = state.isDynamicRgb,
                onCheckedChange = { viewModel.toggleDynamicRgb(it) }
            )

            AudioOptionRow(
                icon = Icons.Rounded.VolumeUp,
                title = "Live Volume Follower",
                subtitle = "Auto-track media volume (Stock 1-15 parity)",
                checked = state.dynamicVolumeTracking,
                onCheckedChange = { viewModel.toggleDynamicVolumeTracking(it) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Reactivity Slider Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CardShapeLarge)
                .background(Surface2)
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Reactivity", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text(text = "${state.reactivity}%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Primary)
                }

                Slider(
                    value = state.reactivity.toFloat(),
                    onValueChange = { viewModel.updateReactivity(it.toInt()) },
                    valueRange = 10f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = Primary,
                        activeTrackColor = Primary,
                        inactiveTrackColor = SecondaryContainer
                    ),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Info Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 36.dp, bottomEnd = 36.dp))
                .background(Surface1)
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "Hardware integration & behavior",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
                Text(
                    text = "Dynamic RGB follows the audio spectrum via hardware I2C firmware schemes. Live volume follower dynamically adjusts the AW22xxx analog gain (0x32 down to 0x08) on volume slider changes, exactly matching stock ROM behavior.",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

@Composable
fun AudioOptionRow(
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
                .background(SecondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = Primary)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(text = subtitle, fontSize = 13.sp, color = TextSecondary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = OnPrimary, checkedTrackColor = Primary)
        )
    }
}
