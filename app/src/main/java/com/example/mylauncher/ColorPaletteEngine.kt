package com.example.mylauncher

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import androidx.palette.graphics.Palette
import java.util.concurrent.Executors

object ColorPaletteEngine {

    data class ThemeColors(
        val dominantColor: Int,
        val vibrantAccent: Int,
        val mutedColor: Int,
        val darkVibrantColor: Int,
        val lightVibrantColor: Int
    )

    fun interface OnThemeColorGeneratedListener {
        fun onColorsReady(colors: ThemeColors)
    }

    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    fun extractColorsFromWallpaper(
        wallpaperBitmap: Bitmap,
        listener: OnThemeColorGeneratedListener
    ) {
        executor.execute {
            val colors = try {
                val palette = Palette.from(wallpaperBitmap)
                    .maximumColorCount(16)
                    .generate()

                ThemeColors(
                    palette.getDominantColor(0xFF202020.toInt()),
                    palette.getVibrantColor(0xFF4F8CFF.toInt()),
                    palette.getMutedColor(0xFF808080.toInt()),
                    palette.getDarkVibrantColor(0xFF303F9F.toInt()),
                    palette.getLightVibrantColor(0xFF90CAF9.toInt())
                )
            } catch (_: Exception) {
                ThemeColors(
                    0xFF202020.toInt(),
                    0xFF4F8CFF.toInt(),
                    0xFF808080.toInt(),
                    0xFF303F9F.toInt(),
                    0xFF90CAF9.toInt()
                )
            } finally {
                if (!wallpaperBitmap.isRecycled) wallpaperBitmap.recycle()
            }

            mainHandler.post { listener.onColorsReady(colors) }
        }
    }
}
