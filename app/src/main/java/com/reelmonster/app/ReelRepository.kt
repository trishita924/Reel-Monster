package com.reelmonster.app

import android.content.Context
import android.content.SharedPreferences
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

data class DayCount(val date: LocalDate, val count: Int) {
    val label: String
        get() = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
}

/** Stores one counter per calendar day in SharedPreferences. Nothing leaves the phone. */
class ReelRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("reel_monster", Context.MODE_PRIVATE)

    private fun key(date: LocalDate) = "count_$date"

    // Counts are keyed by date, so a new day starts at 0 automatically.
    fun getCount(date: LocalDate = LocalDate.now()): Int = prefs.getInt(key(date), 0)

    @Synchronized
    fun increment(): Int {
        val next = getCount() + 1
        prefs.edit().putInt(key(LocalDate.now()), next).apply()
        return next
    }

    @Synchronized
    fun decrement(): Int {
        val next = (getCount() - 1).coerceAtLeast(0)
        prefs.edit().putInt(key(LocalDate.now()), next).apply()
        return next
    }

    fun resetToday() {
        prefs.edit().putInt(key(LocalDate.now()), 0).apply()
    }

    /** Oldest day first, today last. */
    fun last7Days(): List<DayCount> {
        val today = LocalDate.now()
        return (6 downTo 0).map { offset ->
            val d = today.minusDays(offset.toLong())
            DayCount(d, getCount(d))
        }
    }

    var overlayEnabled: Boolean
        get() = prefs.getBoolean("overlay_enabled", false)
        set(value) {
            prefs.edit().putBoolean("overlay_enabled", value).apply()
        }

    fun register(listener: SharedPreferences.OnSharedPreferenceChangeListener) =
        prefs.registerOnSharedPreferenceChangeListener(listener)

    fun unregister(listener: SharedPreferences.OnSharedPreferenceChangeListener) =
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
}
