package com.xiannn.ledsync.system.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiannn.ledsync.system.data.LogLevel
import com.xiannn.ledsync.system.ui.theme.OnPrimary
import com.xiannn.ledsync.system.ui.theme.PillShape
import com.xiannn.ledsync.system.ui.theme.Primary
import com.xiannn.ledsync.system.ui.theme.Surface2
import com.xiannn.ledsync.system.ui.theme.TextPrimary
import com.xiannn.ledsync.system.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HardwareLabScreen(
    viewModel: HardwareLabViewModel,
    modifier: Modifier = Modifier
) {
    val logs by viewModel.actionLogs.collectAsState()
    var selectedEffect by remember { mutableStateOf<String?>("Soft") }
    val listState = rememberLazyListState()

    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .padding(bottom = 72.dp)
    ) {
        // Header
        Column(modifier = Modifier.padding(bottom = 8.dp)) {
            Text(
                text = "Hardware Lab",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp,
                color = TextPrimary
            )
            Text(
                text = "Effect presets and live console",
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 1.dp)
            )
        }

        // Terminal Mockup Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 12.dp, bottomEnd = 32.dp))
                .background(Color(0xFF0D1117))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Window Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF2A2F36))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFF28B82)))
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFFDD663)))
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF81C995)))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "led-console ~ sysfs",
                        color = Color(0xFF8B949E),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Console Content
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                ) {
                    if (logs.isEmpty()) {
                        item {
                            Text(
                                text = "[00:00:00] System ready. Awaiting effect selection.",
                                color = Color(0xFF7EE787),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    } else {
                        items(logs) { entry ->
                            val color = when (entry.level) {
                                LogLevel.SUCCESS -> Color(0xFF7EE787)
                                LogLevel.ERROR -> Color(0xFFF85149)
                                LogLevel.WARNING -> Color(0xFFFDD663)
                                LogLevel.INFO -> Color(0xFF79C0FF)
                            }
                            Text(
                                text = "${entry.timestamp} ${entry.message}",
                                color = color,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Presets Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Effect presets",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )

            Text(
                text = "Turn off",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Primary,
                modifier = Modifier
                    .clip(PillShape)
                    .clickable {
                        selectedEffect = null
                        viewModel.turnOff()
                    }
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Preset Chips FlowRow
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            viewModel.effectList.forEach { effect ->
                val isSelected = selectedEffect == effect
                Box(
                    modifier = Modifier
                        .height(34.dp)
                        .clip(if (isSelected) RoundedCornerShape(10.dp) else RoundedCornerShape(18.dp))
                        .background(if (isSelected) Primary else Surface2)
                        .clickable {
                            selectedEffect = effect
                            viewModel.testEffect(effect)
                        }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = effect,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) OnPrimary else TextPrimary
                    )
                }
            }
        }
    }
}
