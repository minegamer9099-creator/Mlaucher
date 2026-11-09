package com.example.mylauncher

import android.content.Context
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

class ColorOSPageTransformer(context: Context) : ViewPager2.PageTransformer {

    private val cameraDistance =
        context.resources.displayMetrics.density * 12000f

    override fun transformPage(page: View, position: Float) {
        val distance = abs(position)
        val pageWidth = page.width.toFloat()
        val pageHeight = page.height.toFloat()

        if (distance >= 1f) {
            page.alpha = 0f
            page.rotationY = 0f
            page.translationX = 0f
            page.translationZ = 0f
            page.scaleX = 0.88f
            page.scaleY = 0.88f
            return
        }

        val scale = 1f - distance * 0.12f
        val verticalMargin = pageHeight * (1f - scale) / 2f
        val horizontalMargin = pageWidth * (1f - scale) / 2f

        page.apply {
            alpha = 1f - distance * 0.12f
            scaleX = scale
            scaleY = scale
            pivotX = if (position < 0f) pageWidth else 0f
            pivotY = pageHeight / 2f
            this.cameraDistance = this@ColorOSPageTransformer.cameraDistance
            rotationY = -position * 12f
            translationX = if (position < 0f) {
                horizontalMargin - verticalMargin / 2f
            } else {
                -horizontalMargin + verticalMargin / 2f
            }
            translationZ = (1f - distance) * 8f
        }
    }
}
