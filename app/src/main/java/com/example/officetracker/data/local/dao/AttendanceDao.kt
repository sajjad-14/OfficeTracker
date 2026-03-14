package com.example.officetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.officetracker.data.local.entity.AttendanceSession
import com.example.officetracker.data.local.entity.DailyStat
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    // Sessions
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: AttendanceSession): Long

    @Update
    suspend fun updateSession(session: AttendanceSession)

    @Delete
    suspend fun deleteSession(session: AttendanceSession)

    @Query("SELECT * FROM attendance_sessions WHERE endTime IS NULL ORDER BY startTime DESC LIMIT 1")
    suspend fun getCurrentActiveSession(): AttendanceSession?

    // Efficient Flow-based active session watcher — no full table scan
    @Query("SELECT * FROM attendance_sessions WHERE endTime IS NULL ORDER BY startTime DESC LIMIT 1")
    fun observeActiveSession(): Flow<AttendanceSession?>

    @Query("SELECT * FROM attendance_sessions WHERE date = :date ORDER BY startTime ASC")
    fun getSessionsForDate(date: Long): Flow<List<AttendanceSession>>
    
    @Query("SELECT * FROM attendance_sessions WHERE date = :date ORDER BY startTime ASC")
    suspend fun getSessionsForDateSync(date: Long): List<AttendanceSession>

    @Query("SELECT * FROM attendance_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<AttendanceSession>>

    // Count distinct days with at least one session in a date range (used for "Days in Office")
    @Query("SELECT COUNT(DISTINCT date) FROM attendance_sessions WHERE date >= :startDate AND date <= :endDate")
    fun countDistinctDaysInRange(startDate: Long, endDate: Long): Flow<Int>

    // Stats
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDailyStat(stat: DailyStat)

    @Query("SELECT * FROM daily_stats WHERE date = :date")
    fun getDailyStat(date: Long): Flow<DailyStat?>

    @Query("SELECT * FROM daily_stats WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC")
    fun getStatsRange(startDate: Long, endDate: Long): Flow<List<DailyStat>>
    
    @Query("SELECT * FROM daily_stats WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC")
    suspend fun getStatsRangeSync(startDate: Long, endDate: Long): List<DailyStat>
    
    @Query("SELECT SUM(cappedSeconds) FROM daily_stats WHERE date >= :startDate AND date <= :endDate")
    fun getTotalCappedSecondsRange(startDate: Long, endDate: Long): Flow<Long?>

    @Query("SELECT COUNT(*) FROM daily_stats WHERE date >= :startDate AND date <= :endDate AND totalSeconds > 0")
    fun countDaysWithAttendanceRange(startDate: Long, endDate: Long): Flow<Int>

    @Query("SELECT * FROM daily_stats ORDER BY date DESC")
    fun getAllDailyStats(): Flow<List<DailyStat>>

    // Efficient discipline score queries — scoped to a date range to avoid loading all history
    @Query("SELECT COUNT(*) FROM daily_stats WHERE date >= :startDate AND totalSeconds > 0")
    fun countDaysWithWorkSince(startDate: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM daily_stats WHERE date >= :startDate AND isGoalMet = 1")
    fun countGoalMetDaysSince(startDate: Long): Flow<Int>

    @Query("DELETE FROM attendance_sessions")
    suspend fun deleteAllSessions()

    @Query("DELETE FROM daily_stats")
    suspend fun deleteAllDailyStats()
}
