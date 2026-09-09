package com.fiestast.launcher.android.clock

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ClockData(
    val timeFormatted: String,
    val amPm: String,
    val dateFormatted: String
)

class SystemClockProvider {

    fun observeClock(is24Hour: Boolean): Flow<ClockData> = flow {
        while (currentCoroutineContext().isActive) {
            val now = Date()
            val timePattern = if (is24Hour) "HH:mm" else "hh:mm"
            val amPmPattern = if (is24Hour) "" else "a"
            val datePattern = "EEEE, MMM d"

            val timeStr = SimpleDateFormat(timePattern, Locale.getDefault()).format(now)
            val amPmStr = if (is24Hour) "" else SimpleDateFormat(amPmPattern, Locale.getDefault()).format(now).uppercase(Locale.getDefault())
            val dateStr = SimpleDateFormat(datePattern, Locale.getDefault()).format(now)

            emit(ClockData(timeFormatted = timeStr, amPm = amPmStr, dateFormatted = dateStr))
            delay(1000L)
        }
    }
}
