package com.example.officetracker.data.repository

import android.content.Context
import com.example.officetracker.data.local.dao.AttendanceDao
import com.example.officetracker.data.local.entity.AttendanceSession
import com.example.officetracker.data.prefs.UserPreferences
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class AttendanceRepositoryTest {

    private lateinit var repository: AttendanceRepository
    private val attendanceDao: AttendanceDao = mockk(relaxed = true)
    private val userPreferences: UserPreferences = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = AttendanceRepository(attendanceDao, userPreferences, context)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getSessionsForDate returns sessions from dao`() = runTest {
        val date = 1678886400000L // Some timestamp
        val expectedSessions = listOf(
            AttendanceSession(1, date, date + 3600000, false)
        )
        
        // Mock DAO response
        // Note: Repository likely transforms date range. 
        // Let's assume passed date is start of day for simplicity or checked logic.
        // Actually, Repository.getSessionsForDate logic:
        // val startOfDay = LocalDate.ofEpochDay(date / (24 * 60 * 60 * 1000)).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        // wait, repository logic is:
        // fun getSessionsForDate(dateInMillis: Long): Flow<List<AttendanceSession>> {
        //     val startOfDay = Instant.ofEpochMilli(dateInMillis).atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        //     val endOfDay = startOfDay + 86400000 - 1
        //     return attendanceDao.getSessionsBetween(startOfDay, endOfDay)
        // }
        // So we just mock the dao call.
        
        coEvery { attendanceDao.getSessionsBetween(any(), any()) } returns flowOf(expectedSessions)

        val result = repository.getSessionsForDate(date)
        
        result.collect { sessions ->
            assertEquals(expectedSessions, sessions)
        }
    }

    @Test
    fun `addPastSession inserts session into dao`() = runTest {
        val start = 1000L
        val end = 2000L
        val slot = slot<AttendanceSession>()

        repository.addPastSession(start, end)
        
        coVerify { attendanceDao.insertSession(capture(slot)) }
        assertEquals(start, slot.captured.startTime)
        assertEquals(end, slot.captured.endTime)
        assertEquals(true, slot.captured.isManual)
    }

    @Test
    fun `updateSession updates session in dao`() = runTest {
        val session = AttendanceSession(1, 1000, 2000, false)
        repository.updateSession(session)
        coVerify { attendanceDao.updateSession(session) }
    }

    @Test
    fun `deleteSession deletes session from dao`() = runTest {
        val session = AttendanceSession(1, 1000, 2000, false)
        repository.deleteSession(session)
        coVerify { attendanceDao.deleteSession(session) }
    }
}
