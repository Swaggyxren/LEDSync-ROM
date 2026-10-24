package com.xiannn.ledsync.system.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiannn.ledsync.system.ui.theme.DockShape
import com.xiannn.ledsync.system.ui.theme.OnPrimary
import com.xiannn.ledsync.system.ui.theme.Primary
import com.xiannn.ledsync.system.ui.theme.Surface3
import com.xiannn.ledsync.system.ui.theme.TextSecondary

enum class NavTab(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Rounded.Home),
    LEDS("LEDs", Icons.Rounded.Lightbulb),
    TWEAKS("Tweaks", Icons.Rounded.Tune)
}

@Composable
fun FigmaDock(
    currentTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 36.dp, vertical = 14.dp)
            .height(70.dp)
            .clip(DockShape)
            .background(Surface3)
            .padding(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavTab.entries.forEach { tab ->
                val selected = tab == currentTab
                val weight by animateFloatAsState(targetValue = if (selected) 1.5f else 1.0f, label = "dockWeight")
                val bgColor by animateColorAsState(targetValue = if (selected) Primary else Surface3, label = "dockBg")
                val contentColor by animateColorAsState(targetValue = if (selected) OnPrimary else TextSecondary, label = "dockColor")

                Box(
                    modifier = Modifier
                        .weight(weight)
                        .height(58.dp)
                        .clip(RoundedCornerShape(29.dp))
                        .background(bgColor)
                        .clickable { onTabSelected(tab) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title,
                            tint = contentColor
                        )
                        Text(
                            text = tab.title,
                            color = contentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
