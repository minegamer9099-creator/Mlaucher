package com.example.mylauncher

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce

class FluidCloudService : Service() {

    private lateinit var windowManager: WindowManager
    private var cloudView: TextView? = null
    private var expanded = false

    companion object {
        private const val CHANNEL_ID = "fluid_cloud_channel"
        private const val NOTIFICATION_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return START_NOT_STICKY
        }

        createNotificationChannel()
        startCloudForeground()

        if (cloudView == null) showFluidCloud()
        return START_STICKY
    }

    private fun showFluidCloud() {
        val density = resources.displayMetrics.density

        val pill = TextView(this).apply {
            text = "●  MyLauncher"
            textSize = 14f
            setTextColor(0xFFFFFFFF.toInt())
            gravity = Gravity.CENTER
            setPadding(
                (22 * density).toInt(),
                (12 * density).toInt(),
                (22 * density).toInt(),
                (12 * density).toInt()
            )
            background = GradientDrawable().apply {
                setColor(0xFF202124.toInt())
                cornerRadius = 40 * density
                setStroke((1 * density).toInt(), 0x55FFFFFF)
            }
            elevation = 12 * density
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = (30 * density).toInt()
        }

        pill.setOnClickListener { toggleCloud(pill) }
        pill.setOnLongClickListener {
            toggleCloud(pill)
            true
        }

        try {
            windowManager.addView(pill, params)
            cloudView = pill
        } catch (_: Exception) {
            stopSelf()
        }
    }

    private fun toggleCloud(view: TextView) {
        expanded = !expanded
        view.text = if (expanded) "●  MyLauncher\n\nFluid Cloud Active"
                    else "●  MyLauncher"
        animateScale(view)
    }

    private fun animateScale(view: TextView) {
        fun spring(property: DynamicAnimation.ViewProperty, target: Float) {
            val animation = SpringAnimation(view, property)
            animation.spring = SpringForce(target).apply {
                dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
                stiffness = SpringForce.STIFFNESS_MEDIUM
            }
            animation.start()
        }

        spring(DynamicAnimation.SCALE_X, 1.06f)
        spring(DynamicAnimation.SCALE_Y, 1.06f)
        view.postDelayed({
            if (view.isAttachedToWindow) {
                spring(DynamicAnimation.SCALE_X, 1f)
                spring(DynamicAnimation.SCALE_Y, 1f)
            }
        }, 150)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Fluid Cloud",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    private fun startCloudForeground() {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }.setSmallIcon(android.R.drawable.sym_def_app_icon)
            .setContentTitle("Fluid Cloud is active")
            .setContentText("Floating launcher interface is running")
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        cloudView?.let {
            try {
                windowManager.removeView(it)
            } catch (_: Exception) {
            }
        }
        cloudView = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
