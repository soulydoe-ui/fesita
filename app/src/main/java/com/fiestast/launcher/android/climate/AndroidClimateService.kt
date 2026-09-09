package com.fiestast.launcher.android.climate

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.fiestast.launcher.domain.model.AirflowDirection
import com.fiestast.launcher.domain.model.ClimateBackendInfo
import com.fiestast.launcher.domain.model.ClimateBackendType
import com.fiestast.launcher.domain.model.ClimateState
import com.fiestast.launcher.domain.model.ServiceStatus
import com.fiestast.launcher.domain.service.ClimateService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AndroidClimateService(
    private val context: Context
) : ClimateService {

    companion object {
        private const val TAG = "AndroidClimateService"
        const val UNAVAILABLE_REASON = "CLIMATE API UNAVAILABLE"

        // Candidate package names for aftermarket CAN bus / HVAC services.
        // NOTE: We do NOT assume any vendor; these are candidates to query via PackageManager.
        val CANDIDATE_CANBUS_PACKAGES = listOf(
            "com.syu.canbus",
            "com.ts.can",
            "com.microntek.controlinfo",
            "com.microntek.canbus",
            "com.microntek.hvac",
            "com.hiworld.canbus",
            "com.raise.canbus",
            "com.simplesoft.canbus",
            "com.szchoi.canbus",
            "com.android.car.hvac"
        )
    }

    private val _status = MutableStateFlow<ServiceStatus>(
        ServiceStatus.Unavailable(UNAVAILABLE_REASON)
    )
    override val status: StateFlow<ServiceStatus> = _status.asStateFlow()

    private val _climateState = MutableStateFlow(ClimateState())
    override val climateState: StateFlow<ClimateState> = _climateState.asStateFlow()

    private val _driverTempCelsius = MutableStateFlow<Float?>(null)
    override val driverTempCelsius: StateFlow<Float?> = _driverTempCelsius.asStateFlow()

    private val _passengerTempCelsius = MutableStateFlow<Float?>(null)
    override val passengerTempCelsius: StateFlow<Float?> = _passengerTempCelsius.asStateFlow()

    private val _fanSpeed = MutableStateFlow<Int?>(null)
    override val fanSpeed: StateFlow<Int?> = _fanSpeed.asStateFlow()

    private val _isAcOn = MutableStateFlow<Boolean?>(null)
    override val isAcOn: StateFlow<Boolean?> = _isAcOn.asStateFlow()

    private val _isAutoOn = MutableStateFlow<Boolean?>(null)
    override val isAutoOn: StateFlow<Boolean?> = _isAutoOn.asStateFlow()

    private val _isFrontDefrostOn = MutableStateFlow<Boolean?>(null)
    override val isFrontDefrostOn: StateFlow<Boolean?> = _isFrontDefrostOn.asStateFlow()

    private val _isRearDefrostOn = MutableStateFlow<Boolean?>(null)
    override val isRearDefrostOn: StateFlow<Boolean?> = _isRearDefrostOn.asStateFlow()

    private val _isRecirculationOn = MutableStateFlow<Boolean?>(null)
    override val isRecirculationOn: StateFlow<Boolean?> = _isRecirculationOn.asStateFlow()

    private val _airflowDirection = MutableStateFlow(AirflowDirection.UNKNOWN)
    override val airflowDirection: StateFlow<AirflowDirection> = _airflowDirection.asStateFlow()

    private val _detectedBackend = MutableStateFlow<ClimateBackendInfo?>(null)
    override val detectedBackend: StateFlow<ClimateBackendInfo?> = _detectedBackend.asStateFlow()

    private val _canControlHardware = MutableStateFlow(false)
    override val canControlHardware: StateFlow<Boolean> = _canControlHardware.asStateFlow()

    private var isReceiverRegistered = false

    private val climateBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return
            handleClimateBroadcast(intent)
        }
    }

    val broadcastReceiver: BroadcastReceiver get() = climateBroadcastReceiver

    init {
        registerClimateBroadcastReceiver()
        inspectAndConnectClimateBackend()
    }

    private fun registerClimateBroadcastReceiver() {
        try {
            val filter = IntentFilter().apply {
                addAction("com.microntek.canbus.report")
                addAction("com.microntek.hvac.report")
                addAction("com.syu.canbus.action.HVAC")
                addAction("com.syu.ms.action.HVAC")
                addAction("com.ts.can.hvac")
                addAction("android.car.action.HVAC_STATUS")
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(climateBroadcastReceiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                context.registerReceiver(climateBroadcastReceiver, filter)
            }
            isReceiverRegistered = true
        } catch (e: Exception) {
            Log.w(TAG, "Could not register climate broadcast receiver: ${e.message}")
        }
    }

    fun handleClimateBroadcast(intent: Intent) {
        val action = intent.action ?: return
        try {
            when (action) {
                "com.microntek.canbus.report", "com.microntek.hvac.report" -> {
                    parseMicrontekHvac(intent)
                }
                "com.syu.canbus.action.HVAC", "com.syu.ms.action.HVAC" -> {
                    parseSyuHvac(intent)
                }
                "com.ts.can.hvac" -> {
                    parseTopwayHvac(intent)
                }
                "android.car.action.HVAC_STATUS" -> {
                    parseStandardHvac(intent)
                }
                else -> {
                    parseStandardHvac(intent)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Malformed climate broadcast data ignored: ${e.message}")
        }
    }

    private fun parseMicrontekHvac(intent: Intent) {
        val driverTemp = if (intent.hasExtra("temp_left")) {
            val raw = intent.getFloatExtra("temp_left", -1f)
            if (raw > 0) raw else null
        } else null

        val passTemp = if (intent.hasExtra("temp_right")) {
            val raw = intent.getFloatExtra("temp_right", -1f)
            if (raw > 0) raw else null
        } else null

        val fan = if (intent.hasExtra("fan_speed")) {
            val raw = intent.getIntExtra("fan_speed", -1)
            if (raw in 0..10) raw else null
        } else null

        val ac = if (intent.hasExtra("ac_on")) intent.getBooleanExtra("ac_on", false) else null
        val auto = if (intent.hasExtra("auto_on")) intent.getBooleanExtra("auto_on", false) else null
        val frontDefrost = if (intent.hasExtra("front_defrost")) intent.getBooleanExtra("front_defrost", false) else null
        val rearDefrost = if (intent.hasExtra("rear_defrost")) intent.getBooleanExtra("rear_defrost", false) else null
        val recirc = if (intent.hasExtra("recirc")) intent.getBooleanExtra("recirc", false) else null

        val mode = if (intent.hasExtra("airflow_mode")) {
            mapAirflowIntToEnum(intent.getIntExtra("airflow_mode", 0))
        } else AirflowDirection.UNKNOWN

        updateClimateState(
            driverTemp = driverTemp,
            passengerTemp = passTemp,
            fan = fan,
            ac = ac,
            auto = auto,
            frontDefrost = frontDefrost,
            rearDefrost = rearDefrost,
            recirc = recirc,
            airflow = mode
        )
    }

    private fun parseSyuHvac(intent: Intent) {
        val driverTemp = if (intent.hasExtra("temp_left")) {
            val raw = intent.getIntExtra("temp_left", -1)
            if (raw > 0) raw / 10f else null
        } else null

        val passTemp = if (intent.hasExtra("temp_right")) {
            val raw = intent.getIntExtra("temp_right", -1)
            if (raw > 0) raw / 10f else null
        } else null

        val fan = if (intent.hasExtra("wind_speed")) {
            val raw = intent.getIntExtra("wind_speed", -1)
            if (raw in 0..10) raw else null
        } else null

        val ac = if (intent.hasExtra("ac")) intent.getIntExtra("ac", 0) == 1 else null
        val auto = if (intent.hasExtra("auto")) intent.getIntExtra("auto", 0) == 1 else null
        val frontDefrost = if (intent.hasExtra("wind_front")) intent.getIntExtra("wind_front", 0) == 1 else null
        val rearDefrost = if (intent.hasExtra("wind_rear")) intent.getIntExtra("wind_rear", 0) == 1 else null
        val recirc = if (intent.hasExtra("cycle")) intent.getIntExtra("cycle", 0) == 1 else null

        val mode = if (intent.hasExtra("wind_direction")) {
            mapAirflowIntToEnum(intent.getIntExtra("wind_direction", 0))
        } else AirflowDirection.UNKNOWN

        updateClimateState(
            driverTemp = driverTemp,
            passengerTemp = passTemp,
            fan = fan,
            ac = ac,
            auto = auto,
            frontDefrost = frontDefrost,
            rearDefrost = rearDefrost,
            recirc = recirc,
            airflow = mode
        )
    }

    private fun parseTopwayHvac(intent: Intent) {
        val driverTemp = if (intent.hasExtra("driver_temp")) intent.getFloatExtra("driver_temp", -1f).takeIf { it > 0 } else null
        val passTemp = if (intent.hasExtra("pass_temp")) intent.getFloatExtra("pass_temp", -1f).takeIf { it > 0 } else null
        val fan = if (intent.hasExtra("fan")) intent.getIntExtra("fan", -1).takeIf { it in 0..10 } else null
        val ac = if (intent.hasExtra("ac")) intent.getBooleanExtra("ac", false) else null
        val auto = if (intent.hasExtra("auto")) intent.getBooleanExtra("auto", false) else null
        val frontDefrost = if (intent.hasExtra("defrost_front")) intent.getBooleanExtra("defrost_front", false) else null
        val rearDefrost = if (intent.hasExtra("defrost_rear")) intent.getBooleanExtra("defrost_rear", false) else null
        val recirc = if (intent.hasExtra("recirc")) intent.getBooleanExtra("recirc", false) else null

        updateClimateState(
            driverTemp = driverTemp,
            passengerTemp = passTemp,
            fan = fan,
            ac = ac,
            auto = auto,
            frontDefrost = frontDefrost,
            rearDefrost = rearDefrost,
            recirc = recirc
        )
    }

    private fun parseStandardHvac(intent: Intent) {
        val driverTemp = if (intent.hasExtra("driver_temperature")) intent.getFloatExtra("driver_temperature", -1f).takeIf { it > 0 } else null
        val passTemp = if (intent.hasExtra("passenger_temperature")) intent.getFloatExtra("passenger_temperature", -1f).takeIf { it > 0 } else null
        val fan = if (intent.hasExtra("fan_speed")) intent.getIntExtra("fan_speed", -1).takeIf { it in 0..10 } else null
        val ac = if (intent.hasExtra("ac_on")) intent.getBooleanExtra("ac_on", false) else null
        val auto = if (intent.hasExtra("auto_on")) intent.getBooleanExtra("auto_on", false) else null
        val frontDefrost = if (intent.hasExtra("front_defrost")) intent.getBooleanExtra("front_defrost", false) else null
        val rearDefrost = if (intent.hasExtra("rear_defrost")) intent.getBooleanExtra("rear_defrost", false) else null
        val recirc = if (intent.hasExtra("recirculation")) intent.getBooleanExtra("recirculation", false) else null

        updateClimateState(
            driverTemp = driverTemp,
            passengerTemp = passTemp,
            fan = fan,
            ac = ac,
            auto = auto,
            frontDefrost = frontDefrost,
            rearDefrost = rearDefrost,
            recirc = recirc
        )
    }

    private fun mapAirflowIntToEnum(mode: Int): AirflowDirection {
        return when (mode) {
            1 -> AirflowDirection.FACE
            2 -> AirflowDirection.FLOOR
            3 -> AirflowDirection.DEFROST
            4 -> AirflowDirection.FACE_FLOOR
            5 -> AirflowDirection.FLOOR_DEFROST
            6 -> AirflowDirection.FACE_DEFROST
            7 -> AirflowDirection.ALL
            else -> AirflowDirection.UNKNOWN
        }
    }

    private fun updateClimateState(
        driverTemp: Float? = null,
        passengerTemp: Float? = null,
        fan: Int? = null,
        ac: Boolean? = null,
        auto: Boolean? = null,
        frontDefrost: Boolean? = null,
        rearDefrost: Boolean? = null,
        recirc: Boolean? = null,
        airflow: AirflowDirection = AirflowDirection.UNKNOWN
    ) {
        val current = _climateState.value
        val newState = current.copy(
            driverTempCelsius = driverTemp ?: current.driverTempCelsius,
            passengerTempCelsius = passengerTemp ?: current.passengerTempCelsius,
            fanSpeed = fan ?: current.fanSpeed,
            isAcOn = ac ?: current.isAcOn,
            isAutoOn = auto ?: current.isAutoOn,
            isFrontDefrostOn = frontDefrost ?: current.isFrontDefrostOn,
            isRearDefrostOn = rearDefrost ?: current.isRearDefrostOn,
            isRecirculationOn = recirc ?: current.isRecirculationOn,
            airflowDirection = if (airflow != AirflowDirection.UNKNOWN) airflow else current.airflowDirection
        )
        _climateState.value = newState
        _driverTempCelsius.value = newState.driverTempCelsius
        _passengerTempCelsius.value = newState.passengerTempCelsius
        _fanSpeed.value = newState.fanSpeed
        _isAcOn.value = newState.isAcOn
        _isAutoOn.value = newState.isAutoOn
        _isFrontDefrostOn.value = newState.isFrontDefrostOn
        _isRearDefrostOn.value = newState.isRearDefrostOn
        _isRecirculationOn.value = newState.isRecirculationOn
        _airflowDirection.value = newState.airflowDirection
    }

    /**
     * Inspects environment safely:
     * 1. android.car / Car API
     * 2. Vendor CAN bus / HVAC packages and components
     * Strictly avoids guessing or fabricating data.
     */
    @SuppressLint("QueryPermissionsNeeded")
    fun inspectAndConnectClimateBackend(): ClimateBackendInfo? {
        // 1. Safe Android Car API inspection
        val carApiBackend = inspectAndroidCarApi()
        if (carApiBackend != null) {
            _detectedBackend.value = carApiBackend
            _canControlHardware.value = carApiBackend.isControllable
            _status.value = if (carApiBackend.isControllable) {
                ServiceStatus.Connected
            } else {
                ServiceStatus.Unavailable(carApiBackend.controlMechanism)
            }
            return carApiBackend
        }

        // 2. Safe Vendor CAN bus package inspection
        val vendorBackend = inspectVendorCanbusPackages()
        if (vendorBackend != null) {
            _detectedBackend.value = vendorBackend
            _canControlHardware.value = vendorBackend.isControllable
            _status.value = if (vendorBackend.isControllable) {
                ServiceStatus.Connected
            } else {
                ServiceStatus.Available("Detected CAN bus: ${vendorBackend.name}")
            }
            return vendorBackend
        }

        // 3. Fallback: Honest Unavailable
        _detectedBackend.value = null
        _canControlHardware.value = false
        _status.value = ServiceStatus.Unavailable(UNAVAILABLE_REASON)
        _climateState.value = ClimateState()
        _driverTempCelsius.value = null
        _passengerTempCelsius.value = null
        _fanSpeed.value = null
        _isAcOn.value = null
        _isAutoOn.value = null
        _isFrontDefrostOn.value = null
        _isRearDefrostOn.value = null
        _isRecirculationOn.value = null
        _airflowDirection.value = AirflowDirection.UNKNOWN

        Log.i(TAG, "No verified climate/CAN bus backend found; reporting $UNAVAILABLE_REASON")
        return null
    }

    private fun inspectAndroidCarApi(): ClimateBackendInfo? {
        // Only inspect Car API if the system officially advertises the Automotive hardware feature
        if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)) {
            return null
        }
        return try {
            val carClass = Class.forName("android.car.Car")
            // Check if CONTROL_CAR_CLIMATE permission is held
            val hasClimatePermission = context.checkCallingOrSelfPermission(
                "android.car.permission.CONTROL_CAR_CLIMATE"
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasClimatePermission) {
                Log.w(TAG, "android.car is present, but CONTROL_CAR_CLIMATE privileged permission is missing")
                return ClimateBackendInfo(
                    type = ClimateBackendType.ANDROID_CAR_API,
                    name = "Android Car API",
                    packageName = "android.car",
                    isControllable = false,
                    controlMechanism = "Android Car API present but CONTROL_CAR_CLIMATE privileged permission required"
                )
            }

            ClimateBackendInfo(
                type = ClimateBackendType.ANDROID_CAR_API,
                name = "Android Car API (CarHvacManager)",
                packageName = "android.car",
                isControllable = true,
                controlMechanism = "CarHvacManager System Service"
            )
        } catch (_: ClassNotFoundException) {
            null
        } catch (e: SecurityException) {
            Log.w(TAG, "SecurityException inspecting Android Car API: ${e.message}")
            ClimateBackendInfo(
                type = ClimateBackendType.ANDROID_CAR_API,
                name = "Android Car API",
                packageName = "android.car",
                isControllable = false,
                controlMechanism = "Android Car API restricted by system security policy"
            )
        } catch (t: Throwable) {
            Log.w(TAG, "Car API inspection check failed: ${t.message}")
            null
        }
    }

    private fun inspectVendorCanbusPackages(): ClimateBackendInfo? {
        val pm = context.packageManager
        val candidatePackages = mutableSetOf<String>()

        // Query candidate list
        for (pkg in CANDIDATE_CANBUS_PACKAGES) {
            try {
                pm.getPackageInfo(pkg, 0)
                candidatePackages.add(pkg)
            } catch (_: PackageManager.NameNotFoundException) {
                // Not present
            }
        }

        // Query installed applications matching canbus or hvac in package name or label
        try {
            val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            for (app in installedApps) {
                val pkgName = app.packageName
                if (pkgName == context.packageName) continue

                val lower = pkgName.lowercase()
                if (lower.contains("canbus") || lower.contains(".can.") || lower.endsWith(".can") ||
                    lower.contains("hvac") || lower.contains("climate") || lower.contains("aircondition")) {
                    candidatePackages.add(pkgName)
                } else {
                    val label = try {
                        pm.getApplicationLabel(app).toString().lowercase()
                    } catch (_: Exception) { "" }
                    if (label.contains("canbus") || label.contains("hvac") || label.contains("climate") || label.contains("air conditioner")) {
                        candidatePackages.add(pkgName)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error inspecting installed apps for CAN bus: ${e.message}")
        }

        Log.d(TAG, "Found ${candidatePackages.size} candidate CAN/climate packages: $candidatePackages")

        for (pkg in candidatePackages) {
            val info = inspectPackageComponents(pm, pkg)
            if (info != null) {
                return info
            }
        }

        return null
    }

    private fun inspectPackageComponents(pm: PackageManager, packageName: String): ClimateBackendInfo? {
        return try {
            val flags = PackageManager.GET_ACTIVITIES or
                    PackageManager.GET_SERVICES or
                    PackageManager.GET_RECEIVERS
            val pkgInfo = pm.getPackageInfo(packageName, flags)
            val appInfo = pkgInfo.applicationInfo ?: pm.getApplicationInfo(packageName, 0)
            val appLabel = pm.getApplicationLabel(appInfo).toString()

            val exportedActivities = pkgInfo.activities?.filter { it.exported }?.map { it.name } ?: emptyList()
            val exportedServices = pkgInfo.services?.filter { it.exported }?.map { it.name } ?: emptyList()
            val exportedReceivers = pkgInfo.receivers?.filter { it.exported }?.map { it.name } ?: emptyList()

            val isMicrontek = packageName.contains("microntek")
            val isSyu = packageName.contains("syu")
            val isTopway = packageName.contains("ts.can")

            val isControllable = (isMicrontek && exportedReceivers.isNotEmpty()) ||
                    (isSyu && exportedServices.isNotEmpty()) ||
                    (isTopway && exportedReceivers.isNotEmpty())

            val mechanism = when {
                isMicrontek -> "Microntek CAN Bus Broadcast"
                isSyu -> "FYT/Syu CAN Bus Service"
                isTopway -> "Topway CAN Broadcast"
                exportedActivities.isNotEmpty() -> "CAN Settings Activity"
                else -> "None"
            }

            ClimateBackendInfo(
                type = ClimateBackendType.VENDOR_CANBUS,
                name = appLabel,
                packageName = packageName,
                exportedActivities = exportedActivities,
                exportedServices = exportedServices,
                exportedReceivers = exportedReceivers,
                isControllable = isControllable,
                controlMechanism = mechanism
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed inspecting package $packageName: ${e.message}")
            null
        }
    }

    // ==========================================
    // Control Commands (Only dispatched if backend is verified)
    // ==========================================

    override fun setDriverTemperature(tempCelsius: Float): Boolean {
        val backend = _detectedBackend.value ?: return false
        if (!_canControlHardware.value) return false
        return dispatchTemperatureCommand(backend, isDriver = true, tempCelsius = tempCelsius)
    }

    override fun adjustDriverTemperature(deltaCelsius: Float): Boolean {
        val current = _driverTempCelsius.value ?: return false
        return setDriverTemperature(current + deltaCelsius)
    }

    override fun setPassengerTemperature(tempCelsius: Float): Boolean {
        val backend = _detectedBackend.value ?: return false
        if (!_canControlHardware.value) return false
        return dispatchTemperatureCommand(backend, isDriver = false, tempCelsius = tempCelsius)
    }

    override fun adjustPassengerTemperature(deltaCelsius: Float): Boolean {
        val current = _passengerTempCelsius.value ?: return false
        return setPassengerTemperature(current + deltaCelsius)
    }

    private fun dispatchTemperatureCommand(backend: ClimateBackendInfo, isDriver: Boolean, tempCelsius: Float): Boolean {
        val pkg = backend.packageName ?: return false
        return try {
            when {
                pkg.contains("microntek") -> {
                    val intent = Intent("com.microntek.canbus.command").apply {
                        putExtra("command", if (isDriver) "set_temp_left" else "set_temp_right")
                        putExtra("value", tempCelsius)
                        setPackage(pkg)
                    }
                    context.sendBroadcast(intent)
                    true
                }
                pkg.contains("syu") -> {
                    val intent = Intent("com.syu.canbus.action.HVAC").apply {
                        putExtra("cmd", if (isDriver) 10 else 11)
                        putExtra("temp", (tempCelsius * 10).toInt())
                        setPackage(pkg)
                    }
                    context.sendBroadcast(intent)
                    true
                }
                else -> false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to dispatch temperature command: ${e.message}")
            false
        }
    }

    override fun setFanSpeed(speed: Int): Boolean {
        val backend = _detectedBackend.value ?: return false
        if (!_canControlHardware.value) return false
        val pkg = backend.packageName ?: return false
        return try {
            when {
                pkg.contains("microntek") -> {
                    val intent = Intent("com.microntek.canbus.command").apply {
                        putExtra("command", "set_fan_speed")
                        putExtra("value", speed)
                        setPackage(pkg)
                    }
                    context.sendBroadcast(intent)
                    true
                }
                pkg.contains("syu") -> {
                    val intent = Intent("com.syu.canbus.action.HVAC").apply {
                        putExtra("cmd", 20)
                        putExtra("fan", speed)
                        setPackage(pkg)
                    }
                    context.sendBroadcast(intent)
                    true
                }
                else -> false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set fan speed: ${e.message}")
            false
        }
    }

    override fun adjustFanSpeed(delta: Int): Boolean {
        val current = _fanSpeed.value ?: return false
        return setFanSpeed((current + delta).coerceIn(0, 7))
    }

    override fun toggleAc(): Boolean {
        val backend = _detectedBackend.value ?: return false
        if (!_canControlHardware.value) return false
        val pkg = backend.packageName ?: return false
        val target = !(_isAcOn.value ?: false)
        return try {
            when {
                pkg.contains("microntek") -> {
                    val intent = Intent("com.microntek.canbus.command").apply {
                        putExtra("command", "toggle_ac")
                        putExtra("value", target)
                        setPackage(pkg)
                    }
                    context.sendBroadcast(intent)
                    true
                }
                pkg.contains("syu") -> {
                    val intent = Intent("com.syu.canbus.action.HVAC").apply {
                        putExtra("cmd", 30)
                        putExtra("ac", if (target) 1 else 0)
                        setPackage(pkg)
                    }
                    context.sendBroadcast(intent)
                    true
                }
                else -> false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to toggle A/C: ${e.message}")
            false
        }
    }

    override fun toggleAuto(): Boolean {
        val backend = _detectedBackend.value ?: return false
        if (!_canControlHardware.value) return false
        val pkg = backend.packageName ?: return false
        val target = !(_isAutoOn.value ?: false)
        return try {
            when {
                pkg.contains("microntek") -> {
                    val intent = Intent("com.microntek.canbus.command").apply {
                        putExtra("command", "toggle_auto")
                        putExtra("value", target)
                        setPackage(pkg)
                    }
                    context.sendBroadcast(intent)
                    true
                }
                pkg.contains("syu") -> {
                    val intent = Intent("com.syu.canbus.action.HVAC").apply {
                        putExtra("cmd", 31)
                        putExtra("auto", if (target) 1 else 0)
                        setPackage(pkg)
                    }
                    context.sendBroadcast(intent)
                    true
                }
                else -> false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to toggle AUTO: ${e.message}")
            false
        }
    }

    override fun toggleFrontDefrost(): Boolean {
        val backend = _detectedBackend.value ?: return false
        if (!_canControlHardware.value) return false
        val pkg = backend.packageName ?: return false
        val target = !(_isFrontDefrostOn.value ?: false)
        return try {
            when {
                pkg.contains("microntek") -> {
                    val intent = Intent("com.microntek.canbus.command").apply {
                        putExtra("command", "toggle_front_defrost")
                        putExtra("value", target)
                        setPackage(pkg)
                    }
                    context.sendBroadcast(intent)
                    true
                }
                else -> false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to toggle front defrost: ${e.message}")
            false
        }
    }

    override fun toggleRearDefrost(): Boolean {
        val backend = _detectedBackend.value ?: return false
        if (!_canControlHardware.value) return false
        val pkg = backend.packageName ?: return false
        val target = !(_isRearDefrostOn.value ?: false)
        return try {
            when {
                pkg.contains("microntek") -> {
                    val intent = Intent("com.microntek.canbus.command").apply {
                        putExtra("command", "toggle_rear_defrost")
                        putExtra("value", target)
                        setPackage(pkg)
                    }
                    context.sendBroadcast(intent)
                    true
                }
                else -> false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to toggle rear defrost: ${e.message}")
            false
        }
    }

    override fun toggleRecirculation(): Boolean {
        val backend = _detectedBackend.value ?: return false
        if (!_canControlHardware.value) return false
        val pkg = backend.packageName ?: return false
        val target = !(_isRecirculationOn.value ?: false)
        return try {
            when {
                pkg.contains("microntek") -> {
                    val intent = Intent("com.microntek.canbus.command").apply {
                        putExtra("command", "toggle_recirc")
                        putExtra("value", target)
                        setPackage(pkg)
                    }
                    context.sendBroadcast(intent)
                    true
                }
                else -> false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to toggle recirculation: ${e.message}")
            false
        }
    }

    override fun cycleAirflow(): Boolean {
        val backend = _detectedBackend.value ?: return false
        if (!_canControlHardware.value) return false
        val pkg = backend.packageName ?: return false
        val nextMode = when (_airflowDirection.value) {
            AirflowDirection.FACE -> 2 // Floor
            AirflowDirection.FLOOR -> 3 // Defrost
            AirflowDirection.DEFROST -> 4 // Face & Floor
            AirflowDirection.FACE_FLOOR -> 1 // Face
            else -> 1
        }
        return try {
            when {
                pkg.contains("microntek") -> {
                    val intent = Intent("com.microntek.canbus.command").apply {
                        putExtra("command", "set_airflow")
                        putExtra("value", nextMode)
                        setPackage(pkg)
                    }
                    context.sendBroadcast(intent)
                    true
                }
                else -> false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cycle airflow: ${e.message}")
            false
        }
    }

    override fun refreshClimateState() {
        inspectAndConnectClimateBackend()
    }

    fun cleanUp() {
        if (isReceiverRegistered) {
            try {
                context.unregisterReceiver(climateBroadcastReceiver)
                isReceiverRegistered = false
            } catch (e: Exception) {
                Log.w(TAG, "Error unregistering climate receiver: ${e.message}")
            }
        }
    }
}
