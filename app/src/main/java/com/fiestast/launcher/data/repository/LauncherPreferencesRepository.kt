package com.fiestast.launcher.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.fiestast.launcher.domain.model.DriverMode
import com.fiestast.launcher.domain.model.LauncherPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface LauncherPreferencesRepository {
    val preferences: StateFlow<LauncherPreferences>
    fun set24HourFormat(enabled: Boolean)
    fun setDriverMode(mode: DriverMode)
    fun setMetricUnits(enabled: Boolean)
    fun setPreferredNavigationPackage(packageName: String?)
}

class SharedPreferencesLauncherRepository(
    context: Context
) : LauncherPreferencesRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _preferences = MutableStateFlow(loadPreferences())
    override val preferences: StateFlow<LauncherPreferences> = _preferences.asStateFlow()

    private fun loadPreferences(): LauncherPreferences {
        val is24h = prefs.getBoolean(KEY_24_HOUR, false)
        val modeName = prefs.getString(KEY_DRIVER_MODE, DriverMode.NORMAL.name)
        val mode = try {
            DriverMode.valueOf(modeName ?: DriverMode.NORMAL.name)
        } catch (_: IllegalArgumentException) {
            DriverMode.NORMAL
        }
        val metric = prefs.getBoolean(KEY_METRIC, true)
        val preferredNav = prefs.getString(KEY_PREFERRED_NAV, null)
        return LauncherPreferences(
            is24HourFormat = is24h,
            selectedDriverMode = mode,
            isMetricUnits = metric,
            preferredNavigationPackage = preferredNav
        )
    }

    override fun set24HourFormat(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_24_HOUR, enabled).apply()
        _preferences.value = _preferences.value.copy(is24HourFormat = enabled)
    }

    override fun setDriverMode(mode: DriverMode) {
        prefs.edit().putString(KEY_DRIVER_MODE, mode.name).apply()
        _preferences.value = _preferences.value.copy(selectedDriverMode = mode)
    }

    override fun setMetricUnits(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_METRIC, enabled).apply()
        _preferences.value = _preferences.value.copy(isMetricUnits = enabled)
    }

    override fun setPreferredNavigationPackage(packageName: String?) {
        if (packageName == null) {
            prefs.edit().remove(KEY_PREFERRED_NAV).apply()
        } else {
            prefs.edit().putString(KEY_PREFERRED_NAV, packageName).apply()
        }
        _preferences.value = _preferences.value.copy(preferredNavigationPackage = packageName)
    }

    companion object {
        private const val PREFS_NAME = "fiesta_st_launcher_prefs"
        private const val KEY_24_HOUR = "key_24_hour_format"
        private const val KEY_DRIVER_MODE = "key_driver_mode"
        private const val KEY_METRIC = "key_metric_units"
        private const val KEY_PREFERRED_NAV = "key_preferred_navigation_package"
    }
}
