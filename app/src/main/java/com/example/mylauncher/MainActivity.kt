package com.example.mylauncher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import android.app.Activity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

class MainActivity : Activity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var drawer: LinearLayout
    private lateinit var drawerAdapter: AppGridAdapter
    private var allApps: List<AppInfo> = emptyList()
    private var receiverRegistered = false
    private var currentAccent: Int = 0xFF4F8CFF.toInt()

    private val wallpaperReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_WALLPAPER_CHANGED) {
                refreshWallpaperTheme()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewPager = findViewById(R.id.homeViewPager)
        drawer = findViewById(R.id.appDrawerContainer)

        loadApps()
        setupWorkspace()
        setupDrawer()
        setupButtons()
    }

    private fun loadApps() {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        allApps = packageManager.queryIntentActivities(intent, 0)
            .mapNotNull { resolve ->
                val activityInfo = resolve.activityInfo ?: return@mapNotNull null
                AppInfo(
                    label = resolve.loadLabel(packageManager).toString(),
                    packageName = activityInfo.packageName,
                    icon = resolve.loadIcon(packageManager)
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }

    private fun launchApp(app: AppInfo) {
        val launchIntent = packageManager.getLaunchIntentForPackage(app.packageName)
        if (launchIntent != null) {
            try {
                startActivity(launchIntent)
            } catch (_: Exception) {
                Toast.makeText(this, "Unable to open app", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupWorkspace() {
        val pageSize = 16
        val pages = if (allApps.isEmpty()) {
            listOf(emptyList())
        } else {
            allApps.chunked(pageSize)
        }

        viewPager.adapter = WorkspaceAdapter(pages, ::launchApp)
        viewPager.setPageTransformer(ColorOSPageTransformer(this))
        viewPager.offscreenPageLimit = 1
        viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        viewPager.getChildAt(0).overScrollMode = View.OVER_SCROLL_NEVER
    }

    private fun setupDrawer() {
        val recycler = findViewById<RecyclerView>(R.id.appsRecyclerView)
        recycler.layoutManager = GridLayoutManager(this, 4)
        drawerAdapter = AppGridAdapter(::launchApp)
        recycler.adapter = drawerAdapter
        drawerAdapter.submitList(allApps)

        findViewById<EditText>(R.id.searchBar).addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val query = s?.toString()?.trim().orEmpty()
                    drawerAdapter.submitList(
                        if (query.isEmpty()) allApps
                        else allApps.filter { it.label.contains(query, ignoreCase = true) }
                    )
                }
                override fun afterTextChanged(s: Editable?) = Unit
            }
        )
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.drawerButton).setOnClickListener {
            drawer.visibility = View.VISIBLE
        }

        findViewById<Button>(R.id.closeDrawerButton).setOnClickListener {
            drawer.visibility = View.GONE
        }

        findViewById<Button>(R.id.fluidCloudButton).setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                )
            } else {
                ContextCompat.startForegroundService(
                    this,
                    Intent(this, FluidCloudService::class.java)
                )
            }
        }
    }

    private fun refreshWallpaperTheme() {
        val bitmap = WallpaperColorHelper.getWallpaperBitmap(this) ?: return

        ColorPaletteEngine.extractColorsFromWallpaper(bitmap) { colors ->
            if (isFinishing || isDestroyed) return@extractColorsFromWallpaper

            currentAccent = colors.vibrantAccent
            findViewById<Button>(R.id.drawerButton)
                .backgroundTintList = android.content.res.ColorStateList.valueOf(currentAccent)
            findViewById<Button>(R.id.fluidCloudButton)
                .backgroundTintList = android.content.res.ColorStateList.valueOf(currentAccent)

            drawerAdapter.setLabelColor(Color.WHITE)

            val search = findViewById<EditText>(R.id.searchBar)
            search.background = GradientDrawable().apply {
                setColor(0x55FFFFFF)
                cornerRadius = 26f * resources.displayMetrics.density
                setStroke(
                    (1 * resources.displayMetrics.density).toInt(),
                    currentAccent
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!receiverRegistered) {
            val filter = IntentFilter(Intent.ACTION_WALLPAPER_CHANGED)
            if (Build.VERSION.SDK_INT >= 33) {
                registerReceiver(wallpaperReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                @Suppress("DEPRECATION")
                registerReceiver(wallpaperReceiver, filter)
            }
            receiverRegistered = true
        }
        refreshWallpaperTheme()
    }

    override fun onPause() {
        if (receiverRegistered) {
            unregisterReceiver(wallpaperReceiver)
            receiverRegistered = false
        }
        super.onPause()
    }

    @Deprecated("Use OnBackPressedDispatcher in a future UI migration")
    override fun onBackPressed() {
        if (drawer.visibility == View.VISIBLE) {
            drawer.visibility = View.GONE
        } else {
            super.onBackPressed()
        }
    }
}
