package com.example.mylauncher

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas

object WallpaperColorHelper {

    fun getWallpaperBitmap(context: Context): Bitmap? {
        return try {
            val drawable = WallpaperManager.getInstance(context).drawable ?: return null
            val size = 256
            Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).also { bitmap ->
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, size, size)
                drawable.draw(canvas)
            }
        } catch (_: Exception) {
            null
        }
    }
}
