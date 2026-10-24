package com.xiannn.ledsync.system.data

import android.util.Log
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SysfsWriter @Inject constructor() {

    private val TAG = "SysfsWriter"

    fun writeNode(path: String, content: String): Boolean {
        return try {
            val file = File(path)
            if (!file.exists()) {
                Log.w(TAG, "Sysfs node does not exist: $path")
                return false
            }
            FileOutputStream(file).use { fos ->
                fos.write(content.toByteArray())
                fos.flush()
            }
            true
        } catch (e: Exception) {
            Log.d(TAG, "Direct sysfs write failed for $path ($content): ${e.message}. Trying su fallback...")
            writeWithSuFallback(path, content)
        }
    }

    private fun writeWithSuFallback(path: String, content: String): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "echo -n '$content' > $path"))
            process.waitFor() == 0
        } catch (e: Exception) {
            Log.e(TAG, "SU fallback also failed: ${e.message}")
            false
        }
    }
}
