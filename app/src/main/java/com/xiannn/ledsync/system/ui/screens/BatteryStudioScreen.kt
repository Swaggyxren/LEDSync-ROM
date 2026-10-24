package com.xiannn.ledsync.system.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.PriorityHigh
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiannn.ledsync.system.ui.theme.CardShapeLarge
import com.xiannn.ledsync.system.ui.theme.ErrorContainer
import com.xiannn.ledsync.system.ui.theme.OnErrorContainer
import com.xiannn.ledsync.system.ui.theme.OnPrimary
import com.xiannn.ledsync.system.ui.theme.OnWarningContainer
import com.xiannn.ledsync.system.ui.theme.PillShape
import com.xiannn.ledsync.system.ui.theme.Primary
import com.xiannn.ledsync.system.ui.theme.SecondaryContainer
import com.xiannn.ledsync.system.ui.theme.Surface2
import com.xiannn.ledsync.system.ui.theme.Surface3
import com.xiannn.ledsync.system.ui.theme.TextPrimary
import com.xiannn.ledsync.system.ui.theme.TextSecondary
import com.xiannn.ledsync.system.ui.theme.WarningColor
import com.xiannn.ledsync.system.ui.theme.WarningContainer

@Composable
fun BatteryStudioScreen(
    viewModel: BatteryStudioViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .navigationBarsPadding()
    ) {
        // Top Back Header
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
                text = "Battery LED Studio",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp,
                color = TextPrimary
            )
        }

        // Circular Battery Ring Hero Tile
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CardShapeLarge)
                .background(Surface2)
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(100.dp), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Surface3,
                            radius = 42.dp.toPx(),
                            style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = WarningColor,
                            startAngle = -90f,
                            sweepAngle = (state.currentLevel / 100f) * 360f,
                            useCenter = false,
                            style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Text(
                        text = "${state.currentLevel}%",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (state.isCharging) "CHARGING" else "BATTERY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Trigger thresholds",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Low Battery Threshold Card
        BatteryThresholdCard(
            icon = Icons.Rounded.TrendingUp,
            title = "Low Battery",
            subtitle = "Pattern when battery hits threshold",
            currentEffect = state.lowEffect,
            threshold = state.lowThreshold,
            patterns = viewModel.availablePatterns,
            bgColor = WarningContainer,
            contentColor = OnWarningContainer,
            onEffectSelected = { viewModel.setLowEffect(it) },
            onThresholdChanged = { viewModel.updateLowThreshold(it) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Critical Battery Threshold Card
        BatteryThresholdCard(
            icon = Icons.Rounded.PriorityHigh,
            title = "Critical Battery",
            subtitle = "Emergency pattern on critical battery",
            currentEffect = state.critEffect,
            threshold = state.critThreshold,
            patterns = viewModel.availablePatterns,
            bgColor = ErrorContainer,
            contentColor = OnErrorContainer,
            onEffectSelected = { viewModel.setCritEffect(it) },
            onThresholdChanged = { viewModel.updateCritThreshold(it) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Full Charge Threshold Card
        BatteryThresholdCard(
            icon = Icons.Rounded.CheckCircle,
            title = "Full Charge",
            subtitle = "Pattern when fully charged",
            currentEffect = state.fullEffect,
            threshold = state.fullThreshold,
            patterns = viewModel.availablePatterns,
            bgColor = SecondaryContainer,
            contentColor = TextPrimary,
            onEffectSelected = { viewModel.setFullEffect(it) },
            onThresholdChanged = { viewModel.updateFullThreshold(it) }
        )
    }
}

@Composable
fun BatteryThresholdCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    currentEffect: String,
    threshold: Int,
    patterns: List<String>,
    bgColor: Color,
    contentColor: Color,
    onEffectSelected: (String) -> Unit,
    onThresholdChanged: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 14.dp, bottomStart = 32.dp, bottomEnd = 14.dp))
            .background(bgColor)
            .padding(18.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                    Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = contentColor)
                    Text(text = subtitle, fontSize = 12.sp, color = contentColor.copy(alpha = 0.8f))
                }

                // Effect Picker Pill
                Box {
                    Row(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(Primary)
                            .clickable { expanded = true }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = currentEffect, color = OnPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.Rounded.ExpandMore, contentDescription = "Select", tint = OnPrimary, modifier = Modifier.size(16.dp))
                    }

                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        patterns.forEach { pattern ->
                            DropdownMenuItem(
                                text = { Text(pattern) },
                                onClick = {
                                    onEffectSelected(pattern)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Threshold", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = contentColor)
                Text(text = "$threshold%", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = contentColor)
            }

            Slider(
                value = threshold.toFloat(),
                onValueChange = { onThresholdChanged(it.toInt()) },
                valueRange = 5f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = Primary,
                    activeTrackColor = Primary,
                    inactiveTrackColor = Surface3
                )
            )
        }
    }
}
