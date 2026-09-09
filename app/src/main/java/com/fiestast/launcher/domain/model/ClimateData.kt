package com.fiestast.launcher.domain.model

enum class AirflowDirection {
    UNKNOWN,
    FACE,
    FLOOR,
    DEFROST,
    FACE_FLOOR,
    FLOOR_DEFROST,
    FACE_DEFROST,
    ALL
}

data class ClimateState(
    val driverTempCelsius: Float? = null,
    val passengerTempCelsius: Float? = null,
    val fanSpeed: Int? = null,
    val maxFanSpeed: Int = 7,
    val isAcOn: Boolean? = null,
    val isAutoOn: Boolean? = null,
    val isFrontDefrostOn: Boolean? = null,
    val isRearDefrostOn: Boolean? = null,
    val isRecirculationOn: Boolean? = null,
    val airflowDirection: AirflowDirection = AirflowDirection.UNKNOWN,
    val isDualZone: Boolean = false
)

data class ClimateBackendInfo(
    val type: ClimateBackendType,
    val name: String,
    val packageName: String? = null,
    val exportedActivities: List<String> = emptyList(),
    val exportedServices: List<String> = emptyList(),
    val exportedReceivers: List<String> = emptyList(),
    val isControllable: Boolean = false,
    val controlMechanism: String = "None"
)

enum class ClimateBackendType {
    NONE,
    ANDROID_CAR_API,
    VENDOR_CANBUS,
    BROADCAST_RECEIVER
}
