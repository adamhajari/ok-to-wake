package com.oktowake.clock

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.content.Context
import android.os.Bundle
import android.widget.CheckBox
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class SettingsActivity : AppCompatActivity() {

    private val wakeDurationValues = intArrayOf(15, 30, 60, 90, 120)
    private val preWakeDurationValues = intArrayOf(0, 5, 15, 30, 45, 60, 90, 120)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.settings)

        val prefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE)

        val wakeTimeInput = findViewById<TextInputEditText>(R.id.wake_time_input)
        val wakeDurationSpinner = findViewById<Spinner>(R.id.wake_duration_spinner)
        val brightnessSlider = findViewById<SeekBar>(R.id.brightness_slider)
        val wakeBrightnessSlider = findViewById<SeekBar>(R.id.wake_brightness_slider)
        val preWakeSpinner = findViewById<Spinner>(R.id.pre_wake_spinner)
        val showTestButtonsCheckbox = findViewById<CheckBox>(R.id.show_test_buttons_checkbox)

        wakeTimeInput.setText(prefs.getString(MainActivity.KEY_WAKE_TIME, "07:00"))
        val savedWakeDuration = prefs.getInt(MainActivity.KEY_WAKE_DURATION, 60)
        wakeDurationSpinner.setSelection(wakeDurationValues.indexOf(savedWakeDuration).coerceAtLeast(0))
        brightnessSlider.progress = prefs.getInt(MainActivity.KEY_BRIGHTNESS, 5).coerceIn(0, 10)
        wakeBrightnessSlider.progress = prefs.getInt(MainActivity.KEY_WAKE_BRIGHTNESS, 5).coerceIn(0, 10)
        val savedPreWake = prefs.getInt(MainActivity.KEY_PRE_WAKE_DURATION, 0)
        preWakeSpinner.setSelection(preWakeDurationValues.indexOf(savedPreWake).coerceAtLeast(0))
        showTestButtonsCheckbox.isChecked = prefs.getBoolean(MainActivity.KEY_SHOW_TEST_BUTTONS, true)

        val lockOnLeaveCheckbox = findViewById<CheckBox>(R.id.lock_on_leave_checkbox)
        lockOnLeaveCheckbox.isChecked = prefs.getBoolean(MainActivity.KEY_LOCK_ON_LEAVE, false)

        val enableDeviceAdminButton = findViewById<MaterialButton>(R.id.enable_device_admin_button)
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
        val admin = ComponentName(this, OkToWakeDeviceAdminReceiver::class.java)
        enableDeviceAdminButton.isEnabled = dpm != null && !dpm.isAdminActive(admin)
        enableDeviceAdminButton.setOnClickListener {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin)
                putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getString(R.string.lock_on_leave_summary))
            }
            startActivity(intent)
        }

        findViewById<MaterialButton>(R.id.back_button).setOnClickListener {
            saveAndFinish(
                wakeTimeInput,
                wakeDurationSpinner,
                brightnessSlider,
                wakeBrightnessSlider,
                preWakeSpinner,
                showTestButtonsCheckbox,
                lockOnLeaveCheckbox,
                prefs
            )
        }
    }

    override fun onResume() {
        super.onResume()
        val enableDeviceAdminButton = findViewById<MaterialButton>(R.id.enable_device_admin_button)
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
        val admin = ComponentName(this, OkToWakeDeviceAdminReceiver::class.java)
        enableDeviceAdminButton.isEnabled = dpm != null && !dpm.isAdminActive(admin)
    }

    override fun onSupportNavigateUp(): Boolean {
        val wakeTimeInput = findViewById<TextInputEditText>(R.id.wake_time_input)
        val wakeDurationSpinner = findViewById<Spinner>(R.id.wake_duration_spinner)
        val brightnessSlider = findViewById<SeekBar>(R.id.brightness_slider)
        val wakeBrightnessSlider = findViewById<SeekBar>(R.id.wake_brightness_slider)
        val preWakeSpinner = findViewById<Spinner>(R.id.pre_wake_spinner)
        val showTestButtonsCheckbox = findViewById<CheckBox>(R.id.show_test_buttons_checkbox)
        val lockOnLeaveCheckbox = findViewById<CheckBox>(R.id.lock_on_leave_checkbox)
        val prefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE)
        saveAndFinish(wakeTimeInput, wakeDurationSpinner, brightnessSlider, wakeBrightnessSlider, preWakeSpinner, showTestButtonsCheckbox, lockOnLeaveCheckbox, prefs)
        return true
    }

    private fun saveAndFinish(
        wakeTimeInput: TextInputEditText,
        wakeDurationSpinner: Spinner,
        brightnessSlider: SeekBar,
        wakeBrightnessSlider: SeekBar,
        preWakeSpinner: Spinner,
        showTestButtonsCheckbox: CheckBox,
        lockOnLeaveCheckbox: CheckBox,
        prefs: android.content.SharedPreferences
    ) {
        val wakeTime = wakeTimeInput.text?.toString()?.trim() ?: "07:00"
        val wakeDuration = wakeDurationValues[wakeDurationSpinner.selectedItemPosition.coerceIn(0, wakeDurationValues.lastIndex)]
        val brightness = brightnessSlider.progress.coerceIn(0, 10)
        val wakeBrightness = wakeBrightnessSlider.progress.coerceIn(0, 10)
        val preWake = preWakeDurationValues[preWakeSpinner.selectedItemPosition.coerceIn(0, preWakeDurationValues.lastIndex)]
        val showTestButtons = showTestButtonsCheckbox.isChecked
        val lockOnLeave = lockOnLeaveCheckbox.isChecked

        prefs.edit()
            .putString(MainActivity.KEY_WAKE_TIME, wakeTime)
            .putInt(MainActivity.KEY_WAKE_DURATION, wakeDuration)
            .putInt(MainActivity.KEY_BRIGHTNESS, brightness)
            .putInt(MainActivity.KEY_WAKE_BRIGHTNESS, wakeBrightness)
            .putInt(MainActivity.KEY_PRE_WAKE_DURATION, preWake)
            .putBoolean(MainActivity.KEY_SHOW_TEST_BUTTONS, showTestButtons)
            .putBoolean(MainActivity.KEY_LOCK_ON_LEAVE, lockOnLeave)
            .apply()

        Toast.makeText(this, getString(R.string.settings) + " saved", Toast.LENGTH_SHORT).show()
        finish()
    }
}
