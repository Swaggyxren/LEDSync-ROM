package com.xiannn.ledsync.system.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.xiannn.ledsync.system.data.PreferencesRepository
import com.xiannn.ledsync.system.ui.theme.LEDSyncTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var preferencesRepository: PreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by preferencesRepository.themeMode.collectAsState(initial = "SYSTEM")
            LEDSyncTheme(themeSetting = themeMode) {
                MainShell()
            }
        }
    }
}
