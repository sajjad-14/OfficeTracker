package com.example.officetracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.officetracker.data.local.dao.AttendanceDao
import com.example.officetracker.data.local.entity.AttendanceSession
import com.example.officetracker.data.local.entity.DailyStat

@Database(entities = [AttendanceSession::class, DailyStat::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun attendanceDao(): AttendanceDao
}
