package com.example.officetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attendance_sessions")
data class AttendanceSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long, // Start of day timestamp (midnight) to easily group by day
    val startTime: Long,
    val endTime: Long? = null,
    val isManual: Boolean = false
)

@Entity(tableName = "daily_stats")
data class DailyStat(
    @PrimaryKey val date: Long, // Midnight timestamp
    val totalSeconds: Long, // Total worked seconds that day
    val cappedSeconds: Long, // Total seconds capped at 10h
    val isGoalMet: Boolean // > 6h
)
