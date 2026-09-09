package com.fiestast.launcher.android.clock

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge
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

    private val forceRefreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    fun notifyTimeOrTimezoneChanged() {
        forceRefreshTrigger.tryEmit(Unit)
    }

    private fun formatCurrentClock(is24Hour: Boolean): ClockData {
        val now = Date()
        val timePattern = if (is24Hour) "HH:mm" else "hh:mm"
        val amPmPattern = if (is24Hour) "" else "a"
        val datePattern = "EEEE, MMM d"

        val timeStr = SimpleDateFormat(timePattern, Locale.getDefault()).format(now)
        val amPmStr = if (is24Hour) "" else SimpleDateFormat(amPmPattern, Locale.getDefault()).format(now).uppercase(Locale.getDefault())
        val dateStr = SimpleDateFormat(datePattern, Locale.getDefault()).format(now)

        return ClockData(timeFormatted = timeStr, amPm = amPmStr, dateFormatted = dateStr)
    }

    fun observeClock(is24Hour: Boolean): Flow<ClockData> {
        val intervalFlow = flow {
            while (currentCoroutineContext().isActive) {
                emit(formatCurrentClock(is24Hour))
                delay(1000L)
            }
        }

        val eventFlow = flow {
            forceRefreshTrigger.collect {
                emit(formatCurrentClock(is24Hour))
            }
        }

        return merge(intervalFlow, eventFlow)
    }
}
