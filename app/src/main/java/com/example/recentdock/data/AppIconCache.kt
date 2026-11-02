package com.example.recentdock.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import java.io.File
import java.security.MessageDigest

/** Memory + disk icon cache. Disk files live in cacheDir and may be evicted by Android. */
class AppIconCache(context: Context) {
    private val appContext = context.applicationContext
    private val repository by lazy { RecentAppsRepository(appContext, useIconCache = false) }
    private val directory = File(appContext.cacheDir, "recentdock_icons").apply { mkdirs() }

    suspend fun load(app: RecentApp, sizePx: Int): Bitmap {
        val key = cacheKey(app, sizePx)
        MemoryIcons.cache.get(key)?.let { return it }

        val file = File(directory, "$key.png")
        if (file.isFile) {
            BitmapFactory.decodeFile(file.absolutePath)?.let { bitmap ->
                MemoryIcons.cache.put(key, bitmap)
                return bitmap
            }
        }

        val bitmap = repository.loadIconBitmapDirect(app, sizePx)
        MemoryIcons.cache.put(key, bitmap)
        runCatching {
            file.outputStream().buffered().use { output ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
            }
            pruneDiskCacheIfNeeded()
        }
        return bitmap
    }

    private fun pruneDiskCacheIfNeeded() {
        val files = directory.listFiles().orEmpty()
        if (files.size <= MAX_DISK_FILES) return

        files.sortedByDescending { it.lastModified() }
            .drop(KEEP_DISK_FILES)
            .forEach { runCatching { it.delete() } }
    }

    private fun cacheKey(app: RecentApp, sizePx: Int): String {
        val raw = "${app.packageName}|${app.lastUpdateTime}|$sizePx"
        val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    private object MemoryIcons {
        val cache = object : LruCache<String, Bitmap>(12 * 1024) {
            override fun sizeOf(key: String, value: Bitmap): Int =
                (value.byteCount / 1024).coerceAtLeast(1)
        }
    }

    companion object {
        private const val MAX_DISK_FILES = 220
        private const val KEEP_DISK_FILES = 180
    }

}
