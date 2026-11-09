package com.example.mylauncher

import android.app.Activity
import android.app.WallpaperManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var drawer: LinearLayout
    private lateinit var drawerAdapter: AppGridAdapter
    private lateinit var dock: LinearLayout
    private lateinit var pageDots: LinearLayout
    private var allApps: List<AppInfo> = emptyList()
    private var receiverRegistered = false
    private var currentAccent: Int = 0xFFB9D7FF.toInt()

    private val wallpaperReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_WALLPAPER_CHANGED) applyWallpaperBackground()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        setContentView(R.layout.activity_main)

        viewPager = findViewById(R.id.homeViewPager)
        drawer = findViewById(R.id.appDrawerContainer)
        dock = findViewById(R.id.dockContainer)
        pageDots = findViewById(R.id.pageDots)

        updateDateAndClock()
        applyWallpaperBackground()
        loadApps()
        setupWorkspace()
        setupDrawer()
        setupDock()
        setupButtons()
    }

    private fun updateDateAndClock() {
        val now = Date()
        findViewById<TextView>(R.id.clockText).text = SimpleDateFormat("h:mm", Locale.getDefault()).format(now)
        findViewById<TextView>(R.id.dateText).text = SimpleDateFormat("MMM d EEE", Locale.getDefault()).format(now).uppercase(Locale.getDefault())
    }

    private fun applyWallpaperBackground() {
        try {
            val wallpaper = WallpaperManager.getInstance(this).drawable
            if (wallpaper != null) findViewById<View>(R.id.rootContainer)?.background = wallpaper
        } catch (_: Exception) {
            findViewById<View>(R.id.rootContainer)?.setBackgroundColor(0xFF263449.toInt())
        }
    }

    private fun loadApps() {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        allApps = packageManager.queryIntentActivities(intent, 0)
            .mapNotNull { resolve ->
                val info = resolve.activityInfo ?: return@mapNotNull null
                AppInfo(resolve.loadLabel(packageManager).toString(), info.packageName, resolve.loadIcon(packageManager))
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }

    private fun launchApp(app: AppInfo) {
        val launchIntent = packageManager.getLaunchIntentForPackage(app.packageName)
        if (launchIntent != null) {
            try { startActivity(launchIntent) }
            catch (_: Exception) { Toast.makeText(this, "Unable to open app", Toast.LENGTH_SHORT).show() }
        }
    }

    private fun setupWorkspace() {
        val pages = if (allApps.isEmpty()) listOf(emptyList()) else allApps.chunked(8)
        viewPager.adapter = WorkspaceAdapter(pages, ::launchApp)
        viewPager.setPageTransformer(ColorOSPageTransformer(this))
        viewPager.offscreenPageLimit = 1
        viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        viewPager.getChildAt(0).overScrollMode = View.OVER_SCROLL_NEVER
        renderPageDots(pages.size)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) = updatePageDots(position)
        })
    }

    private fun renderPageDots(count: Int) {
        pageDots.removeAllViews()
        repeat(count.coerceAtLeast(1)) { index ->
            val dot = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(if (index == 0) dp(18) else dp(6), dp(6)).apply {
                    marginStart = dp(3); marginEnd = dp(3)
                }
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = dp(6).toFloat()
                    setColor(if (index == 0) Color.WHITE else 0x77FFFFFF)
                }
            }
            pageDots.addView(dot)
        }
    }

    private fun updatePageDots(selected: Int) {
        for (i in 0 until pageDots.childCount) {
            val dot = pageDots.getChildAt(i)
            val params = dot.layoutParams as LinearLayout.LayoutParams
            params.width = if (i == selected) dp(18) else dp(6)
            dot.layoutParams = params
            (dot.background as? GradientDrawable)?.setColor(if (i == selected) Color.WHITE else 0x77FFFFFF)
        }
    }

    private fun setupDock() {
        dock.removeAllViews()
        val preferred = listOf("com.google.android.dialer", "com.android.phone", "com.google.android.apps.messaging", "com.android.messaging", "com.android.chrome", "com.google.android.apps.photos")
        val chosen = preferred.mapNotNull { pkg -> allApps.firstOrNull { it.packageName == pkg } }.distinctBy { it.packageName }.take(5)
        val fallback = allApps.filter { app -> chosen.none { it.packageName == app.packageName } }.take((5 - chosen.size).coerceAtLeast(0))
        (chosen + fallback).forEach { app ->
            val icon = ImageView(this).apply {
                setImageDrawable(app.icon)
                scaleType = ImageView.ScaleType.FIT_CENTER
                contentDescription = app.label
                setPadding(dp(9), dp(9), dp(9), dp(9))
                layoutParams = LinearLayout.LayoutParams(0, dp(58), 1f)
                setOnClickListener { launchApp(app) }
            }
            dock.addView(icon)
        }
    }

    private fun setupDrawer() {
        val recycler = findViewById<RecyclerView>(R.id.appsRecyclerView)
        recycler.layoutManager = GridLayoutManager(this, 4)
        drawerAdapter = AppGridAdapter(::launchApp)
        recycler.adapter = drawerAdapter
        drawerAdapter.submitList(allApps)
        findViewById<EditText>(R.id.searchBar).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.trim().orEmpty()
                drawerAdapter.submitList(if (query.isEmpty()) allApps else allApps.filter { it.label.contains(query, true) })
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })
    }

    private fun openDrawer() {
        drawer.visibility = View.VISIBLE
        findViewById<EditText>(R.id.searchBar).setText("")
    }

    private fun setupButtons() {
        findViewById<View>(R.id.drawerHandle).setOnClickListener { openDrawer() }
        findViewById<TextView>(R.id.closeDrawerButton).setOnClickListener { drawer.visibility = View.GONE }
    }

    private fun refreshWallpaperTheme() {
        val bitmap = WallpaperColorHelper.getWallpaperBitmap(this) ?: return
        ColorPaletteEngine.extractColorsFromWallpaper(bitmap) { colors ->
            if (isFinishing || isDestroyed) return@extractColorsFromWallpaper
            currentAccent = colors.vibrantAccent
            drawerAdapter.setLabelColor(Color.WHITE)
            val search = findViewById<EditText>(R.id.searchBar)
            search.background = GradientDrawable().apply {
                setColor(0x35FFFFFF)
                cornerRadius = dp(28).toFloat()
                setStroke(dp(1), currentAccent)
            }
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    override fun onResume() {
        super.onResume()
        updateDateAndClock()
        applyWallpaperBackground()
        if (!receiverRegistered) {
            val filter = IntentFilter(Intent.ACTION_WALLPAPER_CHANGED)
            if (Build.VERSION.SDK_INT >= 33) registerReceiver(wallpaperReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
            else {
                @Suppress("DEPRECATION")
                registerReceiver(wallpaperReceiver, filter)
            }
            receiverRegistered = true
        }
        refreshWallpaperTheme()
    }

    override fun onPause() {
        if (receiverRegistered) { unregisterReceiver(wallpaperReceiver); receiverRegistered = false }
        super.onPause()
    }

    @Deprecated("Use OnBackPressedDispatcher in a future UI migration")
    override fun onBackPressed() {
        if (drawer.visibility == View.VISIBLE) drawer.visibility = View.GONE else super.onBackPressed()
    }
}
