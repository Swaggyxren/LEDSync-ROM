package com.xiannn.ledsync.system.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xiannn.ledsync.system.data.AppItem
import com.xiannn.ledsync.system.data.AppListSource
import com.xiannn.ledsync.system.data.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppSyncUiState(
    val apps: List<AppItem> = emptyList(),
    val searchQuery: String = "",
    val includeSystemApps: Boolean = false,
    val hexMap: Map<String, String> = emptyMap(),
    val isLoading: Boolean = true
)

@HiltViewModel
class AppSyncViewModel @Inject constructor(
    private val appListSource: AppListSource,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _rawApps = MutableStateFlow<List<AppItem>>(emptyList())
    private val _loading = MutableStateFlow(true)

    val patternList = listOf(
        "Default" to "",
        "Pure" to "00 05 01 00 00 00",
        "Starburst" to "00 05 01 01 00 00",
        "Halo" to "00 05 01 02 00 00",
        "Lightning" to "00 05 01 03 00 00",
        "Sunrise" to "00 05 01 04 00 00",
        "Breath" to "00 20 02 00 00 00",
        "Party" to "00 20 03 00 00 00"
    )

    val uiState: StateFlow<AppSyncUiState> = combine(
        _rawApps,
        _searchQuery,
        preferencesRepository.includeSystemApps,
        preferencesRepository.notifHexMap,
        _loading
    ) { apps, query, incSystem, hexMap, loading ->
        val filtered = apps.filter { app ->
            if (!incSystem && app.isSystem) false
            else if (query.isBlank()) true
            else app.name.contains(query, ignoreCase = true) || app.packageName.contains(query, ignoreCase = true)
        }
        AppSyncUiState(
            apps = filtered,
            searchQuery = query,
            includeSystemApps = incSystem,
            hexMap = hexMap,
            isLoading = loading
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSyncUiState())

    init {
        loadApps()
    }

    fun loadApps() {
        viewModelScope.launch {
            _loading.value = true
            val apps = appListSource.getInstalledApps(includeSystem = true)
            _rawApps.value = apps
            _loading.value = false
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleIncludeSystem(include: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setIncludeSystemApps(include)
        }
    }

    fun setAppPattern(packageName: String, hex: String) {
        viewModelScope.launch {
            preferencesRepository.setAppHex(packageName, hex)
        }
    }
}
