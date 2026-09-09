package com.fiestast.launcher.android.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.fiestast.launcher.domain.model.ServiceStatus
import com.fiestast.launcher.domain.service.NavigationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AndroidNavigationService(
    private val context: Context
) : NavigationService {

    private val _status = MutableStateFlow<ServiceStatus>(
        ServiceStatus.Available("Navigation service ready")
    )
    override val status: StateFlow<ServiceStatus> = _status.asStateFlow()

    private val _defaultNavigationApp = MutableStateFlow<String?>(null)
    override val defaultNavigationApp: StateFlow<String?> = _defaultNavigationApp.asStateFlow()

    init {
        detectNavigationApp()
    }

    private fun detectNavigationApp() {
        val geoIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q="))
        val resolved = context.packageManager.resolveActivity(geoIntent, 0)
        _defaultNavigationApp.value = resolved?.activityInfo?.packageName
    }

    override fun launchNavigationApp(): Boolean {
        return try {
            val navIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (navIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(navIntent)
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }
}
