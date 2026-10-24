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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiannn.ledsync.system.ui.theme.AccentContainer
import com.xiannn.ledsync.system.ui.theme.CardShapeLarge
import com.xiannn.ledsync.system.ui.theme.OnAccentContainer
import com.xiannn.ledsync.system.ui.theme.OnPrimary
import com.xiannn.ledsync.system.ui.theme.OnSecondaryContainer
import com.xiannn.ledsync.system.ui.theme.OnTertiaryContainer
import com.xiannn.ledsync.system.ui.theme.Primary
import com.xiannn.ledsync.system.ui.theme.SecondaryContainer
import com.xiannn.ledsync.system.ui.theme.Surface2
import com.xiannn.ledsync.system.ui.theme.Surface3
import com.xiannn.ledsync.system.ui.theme.TertiaryContainer
import com.xiannn.ledsync.system.ui.theme.TextPrimary
import com.xiannn.ledsync.system.ui.theme.TextSecondary

@Composable
fun PerformanceScreen(
    viewModel: PerformanceViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.perfState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 20.dp)
    ) {
        // Top Back Navigation Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Surface3)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Text(
                text = "Performance Monitor",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp,
                color = TextPrimary
            )
        }

        // CPU Load Card
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Rounded.Speed, contentDescription = "CPU", tint = OnPrimary)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "CPU load", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(text = "Realtime usage", fontSize = 13.sp, color = TextSecondary)
                    }
                    Text(
                        text = "${state.cpuPercent}%",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Primary
                    )
                }

                // Dynamic CPU Bar Graph
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(top = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    repeat(20) { index ->
                        val heightFraction = (((index * 7 + state.cpuPercent) % 100) / 100f).coerceIn(0.2f, 1f)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp * heightFraction)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Primary.copy(alpha = 0.8f))
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // RAM Allocation Breakdown Card
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(SecondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Rounded.Memory, contentDescription = "RAM", tint = OnSecondaryContainer)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "RAM breakdown", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(text = "Physical memory allocation", fontSize = 13.sp, color = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PerfStatTile(title = "Used", value = "${state.ramUsedGb} GB", bgColor = AccentContainer, textColor = OnAccentContainer, modifier = Modifier.weight(1f))
                    PerfStatTile(title = "Free", value = "${state.ramFreeGb} GB", bgColor = TertiaryContainer, textColor = OnTertiaryContainer, modifier = Modifier.weight(1f))
                    PerfStatTile(title = "Total", value = "${state.ramTotalGb} GB", bgColor = Surface3, textColor = TextPrimary, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun PerfStatTile(
    title: String,
    value: String,
    bgColor: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .padding(14.dp)
    ) {
        Column {
            Text(text = title, fontSize = 12.sp, color = textColor.copy(alpha = 0.8f))
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = textColor)
        }
    }
}
