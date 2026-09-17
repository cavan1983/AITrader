package com.example.aitrader

import java.util.Calendar
import java.util.TimeZone
import java.util.Locale

object MarketUtils {
    private val marketTimeZone = TimeZone.getTimeZone("America/New_York")

    fun isMarketOpen(): Boolean {
        val now = Calendar.getInstance(marketTimeZone)
        val dayOfWeek = now.get(Calendar.DAY_OF_WEEK)
        
        // Check if weekend
        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            return false
        }

        val hour = now.get(Calendar.HOUR_OF_DAY)
        val minute = now.get(Calendar.MINUTE)
        val currentTimeInMinutes = hour * 60 + minute
        
        // 9:30 AM is 570 minutes
        // 4:00 PM is 960 minutes
        return currentTimeInMinutes >= 570 && currentTimeInMinutes < 960
    }

    fun getTimeToNextMarketOpen(): Long {
        val now = Calendar.getInstance(marketTimeZone)
        val nextOpen = Calendar.getInstance(marketTimeZone)
        nextOpen.timeInMillis = now.timeInMillis
        nextOpen.set(Calendar.HOUR_OF_DAY, 9)
        nextOpen.set(Calendar.MINUTE, 30)
        nextOpen.set(Calendar.SECOND, 0)
        nextOpen.set(Calendar.MILLISECOND, 0)

        // If it's already past 9:30 AM today, move to tomorrow
        if (now.after(nextOpen)) {
            nextOpen.add(Calendar.DAY_OF_MONTH, 1)
        }

        // Keep adding days until it's a weekday (Mon-Fri)
        while (nextOpen.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || 
               nextOpen.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            nextOpen.add(Calendar.DAY_OF_MONTH, 1)
        }

        return nextOpen.timeInMillis - now.timeInMillis
    }

    fun formatMilliseconds(ms: Long): String {
        val totalSeconds = ms / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    }

    /**
     * Institutional Polling:
     * - 30s during US Market Open (9:30 - 10:30 ET) and Close (15:00 - 16:00 ET)
     * - 120s during Midday (10:30 - 15:00 ET)
     * - 60s Default
     */
    fun getDynamicPollingInterval(): Int {
        val now = Calendar.getInstance(marketTimeZone)
        val hour = now.get(Calendar.HOUR_OF_DAY)
        val minute = now.get(Calendar.MINUTE)
        val currentTimeInMinutes = hour * 60 + minute

        return when {
            // Market Open: 9:30 - 10:30 (570 - 630 mins)
            currentTimeInMinutes in 570..630 -> 30
            // Market Close: 15:00 - 16:00 (900 - 960 mins)
            currentTimeInMinutes in 900..960 -> 30
            // Midday: 10:30 - 15:00 (630 - 900 mins)
            currentTimeInMinutes in 630..900 -> 120
            else -> 60
        }
    }
}
