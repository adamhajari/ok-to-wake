package com.oktowake.clock

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.Calendar
import java.util.Locale
import java.util.Timer
import java.util.TimerTask

class MainActivity : AppCompatActivity() {

    private lateinit var rootLayout: FrameLayout
    private lateinit var timeText: TextView
    private lateinit var testButtonsContainer: LinearLayout
    private var updateTimer: Timer? = null
    private var testWakeActive = false
    private var testPreWakeActive = false
    private var openingInternalActivity = false
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rootLayout = findViewById(R.id.main_root)
        timeText = findViewById(R.id.time_text)
        testButtonsContainer = findViewById(R.id.test_buttons_container)

        updateTestButtonsVisibility()

        // Keep screen on while app is in foreground
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        hideSystemUi()

        updateTimeAndBackground()

        findViewById<android.widget.ImageButton>(R.id.settings_button).setOnClickListener {
            openingInternalActivity = true
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<android.widget.Button>(R.id.test_wake_button).setOnClickListener {
            testPreWakeActive = false
            testWakeActive = true
            updateTimeAndBackground()
            mainHandler.postDelayed({
                testWakeActive = false
                updateTimeAndBackground()
            }, TEST_DURATION_MS)
        }
        findViewById<android.widget.Button>(R.id.test_pre_wake_button).setOnClickListener {
            testWakeActive = false
            testPreWakeActive = true
            updateTimeAndBackground()
            mainHandler.postDelayed({
                testPreWakeActive = false
                updateTimeAndBackground()
            }, TEST_DURATION_MS)
        }

        // Update clock and background every second
        updateTimer = Timer().apply {
            scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    runOnUiThread { updateTimeAndBackground() }
                }
            }, 0, 1000)
        }
    }

    override fun onResume() {
        super.onResume()
        openingInternalActivity = false
        hideSystemUi()
        updateTestButtonsVisibility()
        updateTimeAndBackground()
    }

    override fun onPause() {
        super.onPause()
        if (openingInternalActivity) {
            openingInternalActivity = false
            return
        }
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_LOCK_ON_LEAVE, false)) return
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager ?: return
        val admin = ComponentName(this, OkToWakeDeviceAdminReceiver::class.java)
        if (dpm.isAdminActive(admin)) {
            dpm.lockNow()
        }
    }

    private fun updateTestButtonsVisibility() {
        val show = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getBoolean(KEY_SHOW_TEST_BUTTONS, true)
        testButtonsContainer.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUi()
    }

    private fun hideSystemUi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(android.view.WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        updateTimer?.cancel()
    }

    private fun applyBrightness(backgroundColor: Int) {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val isWakeOrPreWake = backgroundColor == ContextCompat.getColor(this, R.color.green) ||
            backgroundColor == ContextCompat.getColor(this, R.color.orange)
        val brightness = if (isWakeOrPreWake) {
            prefs.getInt(KEY_WAKE_BRIGHTNESS, 5).coerceIn(0, 10)
        } else {
            prefs.getInt(KEY_BRIGHTNESS, 5).coerceIn(0, 10)
        }
        // Map 0-10 to 0.01f-1.0f (avoid 0 so screen stays visible)
        val windowBrightness = 0.01f + (brightness / 10f) * 0.99f
        window.attributes = window.attributes.apply { this.screenBrightness = windowBrightness }
    }

    private fun updateTimeAndBackground() {
        val now = Calendar.getInstance()
        val hour = now.get(Calendar.HOUR_OF_DAY)
        val minute = now.get(Calendar.MINUTE)
        val currentMinutes = hour * 60 + minute

        val backgroundColor = getBackgroundColor(currentMinutes)
        applyBrightness(backgroundColor)
        // Display in 12-hour format without AM/PM (wake time in settings stays 24-hour)
        val hour12 = now.get(Calendar.HOUR).let { if (it == 0) 12 else it }
        timeText.text = String.format(Locale.getDefault(), "%d:%02d", hour12, minute)
        rootLayout.setBackgroundColor(backgroundColor)
    }

    private fun getBackgroundColor(currentMinutesOfDay: Int): Int {
        if (testWakeActive) return ContextCompat.getColor(this, R.color.green)
        if (testPreWakeActive) return ContextCompat.getColor(this, R.color.orange)

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val wakeTimeStr = prefs.getString(KEY_WAKE_TIME, "07:00") ?: "07:00"
        val wakeDuration = prefs.getInt(KEY_WAKE_DURATION, 60).coerceAtLeast(0)
        val preWakeDuration = prefs.getInt(KEY_PRE_WAKE_DURATION, 0).coerceAtLeast(0)

        val (wakeHour, wakeMinute) = parseTime(wakeTimeStr)
        val wakeStartMinutes = wakeHour * 60 + wakeMinute
        val wakeEndMinutes = wakeStartMinutes + wakeDuration
        val preWakeStartMinutes = (wakeStartMinutes - preWakeDuration).coerceAtLeast(0)
        val minutesPerDay = 24 * 60

        return when {
            // During green "wake" period (handles overnight: e.g. 23:00 + 120 min)
            wakeEndMinutes <= minutesPerDay && currentMinutesOfDay >= wakeStartMinutes && currentMinutesOfDay < wakeEndMinutes ->
                ContextCompat.getColor(this, R.color.green)
            wakeEndMinutes > minutesPerDay && (currentMinutesOfDay >= wakeStartMinutes || currentMinutesOfDay < wakeEndMinutes - minutesPerDay) ->
                ContextCompat.getColor(this, R.color.green)
            // During orange "pre-wake" period (only if pre-wake > 0)
            preWakeDuration > 0 && currentMinutesOfDay >= preWakeStartMinutes && currentMinutesOfDay < wakeStartMinutes ->
                ContextCompat.getColor(this, R.color.orange)
            else ->
                ContextCompat.getColor(this, R.color.black)
        }
    }

    private fun parseTime(timeStr: String): Pair<Int, Int> {
        return try {
            val parts = timeStr.trim().split(":", " ")
            val hour = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: 7
            val minute = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0
            Pair(hour, minute)
        } catch (_: Exception) {
            Pair(7, 0)
        }
    }

    companion object {
        private const val TEST_DURATION_MS = 10_000L // 10 seconds

        const val PREFS_NAME = "OkToWakePrefs"
        const val KEY_WAKE_TIME = "wake_time"
        const val KEY_WAKE_DURATION = "wake_duration"
        const val KEY_BRIGHTNESS = "brightness"
        const val KEY_WAKE_BRIGHTNESS = "wake_brightness"
        const val KEY_PRE_WAKE_DURATION = "pre_wake_duration"
        const val KEY_SHOW_TEST_BUTTONS = "show_test_buttons"
        const val KEY_LOCK_ON_LEAVE = "lock_on_leave"
    }
}
