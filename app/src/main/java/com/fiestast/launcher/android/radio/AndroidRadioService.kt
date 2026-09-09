package com.fiestast.launcher.android.radio

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.fiestast.launcher.domain.model.RadioBackendInfo
import com.fiestast.launcher.domain.model.ServiceStatus
import com.fiestast.launcher.domain.service.RadioService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AndroidRadioService(
    private val context: Context
) : RadioService {

    companion object {
        private const val TAG = "AndroidRadioService"
        const val UNAVAILABLE_REASON = "FM HARDWARE API UNAVAILABLE"

        // Known candidate package prefixes/names across common Android head-unit platforms
        // NOTE: We do NOT assume any vendor; these are purely candidate patterns to inspect via PackageManager
        val KNOWN_CANDIDATE_PACKAGES = listOf(
            "com.android.fmradio",
            "com.caf.fmradio",
            "com.mediatek.fmradio",
            "com.syu.radio",
            "com.syu.fm",
            "com.microntek.radio",
            "com.ts.radio",
            "com.navinfo.radio",
            "com.hct.radio",
            "com.sds.radio"
        )
    }

    private val _status = MutableStateFlow<ServiceStatus>(
        ServiceStatus.Unavailable(UNAVAILABLE_REASON)
    )
    override val status: StateFlow<ServiceStatus> = _status.asStateFlow()

    private val _currentFrequency = MutableStateFlow<Double?>(null)
    override val currentFrequency: StateFlow<Double?> = _currentFrequency.asStateFlow()

    private val _detectedRadioPackage = MutableStateFlow<String?>(null)
    override val detectedRadioPackage: StateFlow<String?> = _detectedRadioPackage.asStateFlow()

    private val _detectedBackendInfo = MutableStateFlow<RadioBackendInfo?>(null)
    override val detectedBackendInfo: StateFlow<RadioBackendInfo?> = _detectedBackendInfo.asStateFlow()

    private val _canControlHardware = MutableStateFlow(false)
    override val canControlHardware: StateFlow<Boolean> = _canControlHardware.asStateFlow()

    private var isReceiverRegistered = false

    private val radioFeedbackReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return
            handleRadioBroadcast(intent)
        }
    }

    val broadcastReceiver: BroadcastReceiver get() = radioFeedbackReceiver

    init {
        registerFeedbackReceiver()
        inspectAndConnectRadioBackend()
    }

    private fun registerFeedbackReceiver() {
        try {
            val filter = IntentFilter().apply {
                addAction("com.microntek.radio.report")
                addAction("com.syu.ms.action.RADIO")
                addAction("android.intent.action.FM")
                addAction("com.caf.fmradio.FMRADIO_ACTIVITY")
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(radioFeedbackReceiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                context.registerReceiver(radioFeedbackReceiver, filter)
            }
            isReceiverRegistered = true
        } catch (e: Exception) {
            Log.w(TAG, "Could not register radio feedback receiver: ${e.message}")
        }
    }

    private fun handleRadioBroadcast(intent: Intent) {
        val action = intent.action ?: return
        try {
            when (action) {
                "com.microntek.radio.report" -> {
                    val freqInt = intent.getIntExtra("freq", -1)
                    if (freqInt > 0) {
                        _currentFrequency.value = freqInt / 100.0
                    }
                }
                "com.syu.ms.action.RADIO" -> {
                    val freqInt = intent.getIntExtra("freq", -1)
                    if (freqInt > 0) {
                        _currentFrequency.value = if (freqInt > 2000) freqInt / 100.0 else freqInt / 10.0
                    }
                }
                else -> {
                    // Extract frequency if standard extra is present
                    val freqDouble = intent.getDoubleExtra("frequency", -1.0)
                    if (freqDouble > 0) {
                        _currentFrequency.value = freqDouble
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error handling radio broadcast: ${e.message}")
        }
    }

    /**
     * Inspects head-unit environment for real radio packages and hardware APIs.
     * Does NOT assume any vendor and does NOT fabricate fake frequencies or stations.
     */
    @SuppressLint("QueryPermissionsNeeded")
    fun inspectAndConnectRadioBackend(): RadioBackendInfo? {
        val pm = context.packageManager

        // 1. First, inspect all installed packages matching candidate signatures or containing "radio"/"fm"
        val candidatePackages = mutableSetOf<String>()

        // Check explicit candidate packages
        for (pkg in KNOWN_CANDIDATE_PACKAGES) {
            try {
                pm.getPackageInfo(pkg, 0)
                candidatePackages.add(pkg)
            } catch (_: PackageManager.NameNotFoundException) {
                // Not installed
            }
        }

        // Query installed applications and check package names / labels
        try {
            val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            for (app in installedApps) {
                val pkgName = app.packageName
                if (pkgName == context.packageName) continue

                val lowerPkg = pkgName.lowercase()
                if (lowerPkg.contains("radio") || lowerPkg.contains("fmradio") || lowerPkg.endsWith(".fm")) {
                    candidatePackages.add(pkgName)
                } else {
                    val label = try {
                        pm.getApplicationLabel(app).toString().lowercase()
                    } catch (_: Exception) {
                        ""
                    }
                    if (label.contains("radio") || label == "fm" || label.contains("fm tuner") || label.contains("am/fm")) {
                        candidatePackages.add(pkgName)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error querying installed applications: ${e.message}")
        }

        // Query intent matches for music/radio actions
        try {
            val radioIntents = listOf(
                Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_MUSIC),
                Intent("android.intent.action.RADIO_SEARCH")
            )
            for (intent in radioIntents) {
                val resolveInfos = pm.queryIntentActivities(intent, 0)
                for (info in resolveInfos) {
                    val pkg = info.activityInfo?.packageName
                    if (pkg != null && pkg != context.packageName) {
                        val lower = pkg.lowercase()
                        if (lower.contains("radio") || lower.contains("fm") || lower.contains("tuner")) {
                            candidatePackages.add(pkg)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error querying radio intent activities: ${e.message}")
        }

        Log.d(TAG, "Found ${candidatePackages.size} candidate radio packages on system: $candidatePackages")

        // 2. Inspect components of detected candidate packages
        val detectedBackends = mutableListOf<RadioBackendInfo>()
        for (pkg in candidatePackages) {
            val backendInfo = inspectPackageComponents(pm, pkg)
            if (backendInfo != null) {
                detectedBackends.add(backendInfo)
            }
        }

        // 3. Check for standard Android Automotive RadioManager hardware API
        val isRadioManagerPresent = inspectSystemRadioManager()

        // 4. Determine connection state honestly
        if (detectedBackends.isNotEmpty()) {
            val primary = detectedBackends.first()
            _detectedRadioPackage.value = primary.packageName
            _detectedBackendInfo.value = primary
            _canControlHardware.value = primary.isControllable || isRadioManagerPresent
            _status.value = ServiceStatus.Available("Radio backend connected: ${primary.appName}")
            Log.i(TAG, "Connected to verified radio backend: ${primary.packageName} (controllable=${_canControlHardware.value})")
            return primary
        } else if (isRadioManagerPresent) {
            val systemBackend = RadioBackendInfo(
                packageName = "android.hardware.radio",
                appName = "System Radio Hardware",
                isControllable = true,
                controlMechanism = "Android Hardware RadioManager"
            )
            _detectedRadioPackage.value = systemBackend.packageName
            _detectedBackendInfo.value = systemBackend
            _canControlHardware.value = true
            _status.value = ServiceStatus.Available("Radio backend connected: System Hardware Radio")
            Log.i(TAG, "Connected to Android Hardware RadioManager")
            return systemBackend
        } else {
            // Honest state: No verified hardware or radio package
            _detectedRadioPackage.value = null
            _detectedBackendInfo.value = null
            _canControlHardware.value = false
            _status.value = ServiceStatus.Unavailable(UNAVAILABLE_REASON)
            _currentFrequency.value = null
            Log.i(TAG, "No verified radio backend found; reporting $UNAVAILABLE_REASON")
            return null
        }
    }

    private fun inspectPackageComponents(pm: PackageManager, packageName: String): RadioBackendInfo? {
        return try {
            val flags = PackageManager.GET_ACTIVITIES or
                    PackageManager.GET_SERVICES or
                    PackageManager.GET_RECEIVERS
            val pkgInfo = pm.getPackageInfo(packageName, flags)
            val appLabel = pm.getApplicationLabel(pkgInfo.applicationInfo ?: return null).toString()

            val exportedActivities = pkgInfo.activities
                ?.filter { it.exported }
                ?.map { it.name }
                ?: emptyList()

            val exportedServices = pkgInfo.services
                ?.filter { it.exported }
                ?.map { it.name }
                ?: emptyList()

            val exportedReceivers = pkgInfo.receivers
                ?.filter { it.exported }
                ?.map { it.name }
                ?: emptyList()

            // Verify if there are usable components (activity to launch, or receiver/service to tune)
            val launchIntent = pm.getLaunchIntentForPackage(packageName)
            val hasLaunchableActivity = launchIntent != null || exportedActivities.isNotEmpty()

            // Check if vendor has controllable intent receiver
            val isMicrontek = packageName.contains("microntek")
            val isSyu = packageName.contains("syu")
            val isTopway = packageName.contains("ts.radio")
            val isControllable = (isMicrontek && exportedReceivers.isNotEmpty()) ||
                    (isSyu && exportedServices.isNotEmpty()) ||
                    (isTopway && exportedReceivers.isNotEmpty())

            val mechanism = when {
                isMicrontek -> "Microntek Intent Broadcast"
                isSyu -> "FYT/Syu IPC Service"
                isTopway -> "Topway Broadcast"
                hasLaunchableActivity -> "Activity Launch Intent"
                else -> "None"
            }

            RadioBackendInfo(
                packageName = packageName,
                appName = appLabel,
                exportedActivities = exportedActivities,
                exportedServices = exportedServices,
                exportedReceivers = exportedReceivers,
                isControllable = isControllable,
                controlMechanism = mechanism
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed to inspect package $packageName: ${e.message}")
            null
        }
    }

    private fun inspectSystemRadioManager(): Boolean {
        return try {
            val radioManager = context.getSystemService("radio") ?: return false
            val listModules = radioManager.javaClass.getMethod("listModules", List::class.java)
            val modules = mutableListOf<Any>()
            val result = listModules.invoke(radioManager, modules) as? Int
            result == 0 && modules.isNotEmpty()
        } catch (_: Throwable) {
            false
        }
    }

    override fun tune(frequency: Double) {
        val backend = _detectedBackendInfo.value ?: return
        if (!_canControlHardware.value) {
            Log.w(TAG, "Hardware control not supported for backend: ${backend.packageName}")
            return
        }

        try {
            when {
                backend.packageName.contains("microntek") -> {
                    val intent = Intent("com.microntek.radio.tune").apply {
                        putExtra("freq", (frequency * 100).toInt())
                        setPackage(backend.packageName)
                    }
                    context.sendBroadcast(intent)
                }
                backend.packageName.contains("syu") -> {
                    val intent = Intent("com.syu.ms.action.RADIO").apply {
                        putExtra("cmd", 1) // Tune command
                        putExtra("freq", (frequency * 100).toInt())
                        setPackage(backend.packageName)
                    }
                    context.sendBroadcast(intent)
                }
                else -> {
                    val intent = Intent("android.intent.action.RADIO_SEARCH").apply {
                        putExtra("frequency", frequency)
                        setPackage(backend.packageName)
                    }
                    context.sendBroadcast(intent)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to tune radio to $frequency MHz: ${e.message}")
        }
    }

    override fun seekNext() {
        val backend = _detectedBackendInfo.value ?: return
        if (!_canControlHardware.value) {
            Log.w(TAG, "Seek next not supported without hardware control")
            return
        }
        try {
            when {
                backend.packageName.contains("microntek") -> {
                    val intent = Intent("com.microntek.radio.command").apply {
                        putExtra("command", "seek_up")
                        setPackage(backend.packageName)
                    }
                    context.sendBroadcast(intent)
                }
                backend.packageName.contains("syu") -> {
                    val intent = Intent("com.syu.ms.action.RADIO").apply {
                        putExtra("cmd", 3) // Seek up
                        setPackage(backend.packageName)
                    }
                    context.sendBroadcast(intent)
                }
                else -> {
                    val intent = Intent("android.intent.action.RADIO_SEARCH").apply {
                        putExtra("search_mode", "up")
                        setPackage(backend.packageName)
                    }
                    context.sendBroadcast(intent)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to seek next: ${e.message}")
        }
    }

    override fun seekPrevious() {
        val backend = _detectedBackendInfo.value ?: return
        if (!_canControlHardware.value) {
            Log.w(TAG, "Seek previous not supported without hardware control")
            return
        }
        try {
            when {
                backend.packageName.contains("microntek") -> {
                    val intent = Intent("com.microntek.radio.command").apply {
                        putExtra("command", "seek_down")
                        setPackage(backend.packageName)
                    }
                    context.sendBroadcast(intent)
                }
                backend.packageName.contains("syu") -> {
                    val intent = Intent("com.syu.ms.action.RADIO").apply {
                        putExtra("cmd", 4) // Seek down
                        setPackage(backend.packageName)
                    }
                    context.sendBroadcast(intent)
                }
                else -> {
                    val intent = Intent("android.intent.action.RADIO_SEARCH").apply {
                        putExtra("search_mode", "down")
                        setPackage(backend.packageName)
                    }
                    context.sendBroadcast(intent)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to seek previous: ${e.message}")
        }
    }

    override fun launchRadioApp(): Boolean {
        val backend = _detectedBackendInfo.value ?: return false
        return try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(backend.packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                true
            } else if (backend.exportedActivities.isNotEmpty()) {
                val actName = backend.exportedActivities.first()
                val intent = Intent().apply {
                    setClassName(backend.packageName, actName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch radio app ${backend.packageName}: ${e.message}")
            false
        }
    }

    override fun refreshRadioState() {
        inspectAndConnectRadioBackend()
    }

    fun cleanUp() {
        if (isReceiverRegistered) {
            try {
                context.unregisterReceiver(radioFeedbackReceiver)
                isReceiverRegistered = false
            } catch (e: Exception) {
                Log.w(TAG, "Error unregistering receiver: ${e.message}")
            }
        }
    }
}
