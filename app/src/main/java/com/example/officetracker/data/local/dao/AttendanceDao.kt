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

    @Query("SELECT * FROM attendance_sessions WHERE date = :date ORDER BY startTime ASC")
    fun getSessionsForDate(date: Long): Flow<List<AttendanceSession>>
    
    @Query("SELECT * FROM attendance_sessions WHERE date = :date ORDER BY startTime ASC")
    suspend fun getSessionsForDateSync(date: Long): List<AttendanceSession>

    @Query("SELECT * FROM attendance_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<AttendanceSession>>

    // Stats
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDailyStat(stat: DailyStat)

    @Query("SELECT * FROM daily_stats WHERE date = :date")
    fun getDailyStat(date: Long): Flow<DailyStat?>

    @Query("SELECT * FROM daily_stats WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC")
    fun getStatsRange(startDate: Long, endDate: Long): Flow<List<DailyStat>>
    
    @Query("SELECT SUM(cappedSeconds) FROM daily_stats WHERE date >= :startDate AND date <= :endDate")
    fun getTotalCappedSecondsRange(startDate: Long, endDate: Long): Flow<Long?>

    @Query("SELECT * FROM daily_stats ORDER BY date DESC")
    fun getAllDailyStats(): Flow<List<DailyStat>>

    @Query("DELETE FROM attendance_sessions")
    suspend fun deleteAllSessions()

    @Query("DELETE FROM daily_stats")
    suspend fun deleteAllDailyStats()
}
