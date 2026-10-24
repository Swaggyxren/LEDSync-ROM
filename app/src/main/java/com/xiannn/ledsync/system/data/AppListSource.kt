package com.xiannn.ledsync.system.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class AppItem(
    val name: String,
    val packageName: String,
    val icon: Drawable?,
    val isSystem: Boolean
)

@Singleton
class AppListSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun getInstalledApps(includeSystem: Boolean = false): List<AppItem> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        packages.mapNotNull { appInfo ->
            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            if (!includeSystem && isSystem) return@mapNotNull null

            val name = pm.getApplicationLabel(appInfo).toString()
            val icon = try { pm.getApplicationIcon(appInfo) } catch (_: Exception) { null }
            AppItem(name = name, packageName = appInfo.packageName, icon = icon, isSystem = isSystem)
        }.sortedBy { it.name.lowercase() }
    }
}
