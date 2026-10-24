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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SettingsSuggest
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiannn.ledsync.system.ui.theme.CardShapeLarge
import com.xiannn.ledsync.system.ui.theme.OnPrimary
import com.xiannn.ledsync.system.ui.theme.PillShape
import com.xiannn.ledsync.system.ui.theme.Primary
import com.xiannn.ledsync.system.ui.theme.SecondaryContainer
import com.xiannn.ledsync.system.ui.theme.Surface2
import com.xiannn.ledsync.system.ui.theme.Surface3
import com.xiannn.ledsync.system.ui.theme.TextPrimary
import com.xiannn.ledsync.system.ui.theme.TextSecondary

@Composable
fun AppSyncScreen(
    viewModel: AppSyncViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 20.dp)
            .navigationBarsPadding()
    ) {
        // Top Back Navigation Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp),
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
                text = "App LED Sync",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp,
                color = TextPrimary
            )
        }

        // Search Input Bar
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            placeholder = { Text("Search installed apps…", color = TextSecondary) },
            leadingIcon = { Icon(imageVector = Icons.Rounded.Search, contentDescription = "Search", tint = TextSecondary) },
            shape = RoundedCornerShape(28.dp),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Surface2,
                unfocusedContainerColor = Surface2,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // System App Sync Toggle Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CardShapeLarge)
                .background(Surface2)
                .padding(16.dp)
        ) {
            Row(
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
                    Icon(imageVector = Icons.Rounded.SettingsSuggest, contentDescription = "System", tint = Primary)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "System App Sync", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(text = "Include system apps in LED effects", fontSize = 13.sp, color = TextSecondary)
                }
                Switch(
                    checked = state.includeSystemApps,
                    onCheckedChange = { viewModel.toggleIncludeSystem(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = OnPrimary, checkedTrackColor = Primary)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // App List
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CardShapeLarge)
                    .background(Surface2)
            ) {
                items(state.apps, key = { it.packageName }) { app ->
                    val currentHex = state.hexMap[app.packageName] ?: ""
                    val currentPatternLabel = viewModel.patternList.firstOrNull { it.second == currentHex }?.first ?: "Default"

                    var dropdownOpen by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Letter avatar or icon
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Surface3),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = app.name.take(1).uppercase(),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Primary
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = app.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, maxLines = 1)
                            Text(text = app.packageName, fontSize = 12.sp, color = TextSecondary, maxLines = 1)
                        }

                        // Pattern Selector Chip Dropdown
                        Box {
                            Row(
                                modifier = Modifier
                                    .clip(PillShape)
                                    .background(if (currentHex.isNotBlank()) Primary else Surface3)
                                    .clickable { dropdownOpen = true }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = currentPatternLabel,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentHex.isNotBlank()) OnPrimary else TextPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Rounded.ExpandMore,
                                    contentDescription = "Select",
                                    tint = if (currentHex.isNotBlank()) OnPrimary else TextPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = dropdownOpen,
                                onDismissRequest = { dropdownOpen = false }
                            ) {
                                viewModel.patternList.forEach { (label, hex) ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                            viewModel.setAppPattern(app.packageName, hex)
                                            dropdownOpen = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
