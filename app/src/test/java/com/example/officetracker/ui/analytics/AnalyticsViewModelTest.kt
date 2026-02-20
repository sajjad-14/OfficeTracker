package com.example.officetracker.ui.analytics

import com.example.officetracker.data.local.entity.AttendanceSession
import com.example.officetracker.data.local.entity.DailyStat
import com.example.officetracker.data.prefs.UserPreferences
import com.example.officetracker.data.repository.AttendanceRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
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
import java.time.LocalDate
import java.time.ZoneId

@ExperimentalCoroutinesApi
class AnalyticsViewModelTest {

    private lateinit var viewModel: AnalyticsViewModel
    private val repository: AttendanceRepository = mockk(relaxed = true)
    private val userPreferences: UserPreferences = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        
        // Mock initial flows to avoid NPE during init
        coEvery { repository.getFullMonthHistory() } returns flowOf(emptyList())
        coEvery { repository.getCurrentActiveSession() } returns flowOf(null)
        coEvery { userPreferences.userGoals } returns flowOf(UserPreferences.UserGoals())
        
        viewModel = AnalyticsViewModel(repository, userPreferences)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getHeatmapData maps intensity correctly`() = runTest {
        // Prepare mock data
        val today = LocalDate.now() // Needs manual date mocking or fixed date. 
        // For standard unit test, LocalDate.now() depends on system. 
        // Best to use a fixed stat date.
        
        val date1 = 1678886400000L // Some date in MS
        val date2 = 1678972800000L // Next day
        
        val stat1 = DailyStat(date1, 3600L, 3600L, false) // 1 Hour -> Level 1
        val stat2 = DailyStat(date2, 8 * 3600L, 8 * 3600L, true) // 8 Hours -> Level 4
        
        val historyList = listOf(stat1, stat2)
        
        // Mock history state flow - wait, history is a private val exposed via stateIn?
        // No, it's public val history.
        // It combines flows. We mocked the inputs (getFullMonthHistory).
        // Let's update the mock to return our list.
        coEvery { repository.getFullMonthHistory() } returns flowOf(historyList)
        
        // Re-init viewmodel to pick up new flow? Or just use flow emission.
        // history is a StateFlow started in init. 
        // Since we mocked it in setup, we might need a way to emit new values or just create a new VM.
        // Let's create a new VM with the specific mock.
        
        val newViewModel = AnalyticsViewModel(repository, userPreferences)
        
        // Test getHeatmapData
        val heatmapFlow = newViewModel.getHeatmapData()
        
        // Collect
        heatmapFlow.collect { map ->
            // Verification logic
            // Check count
            assertEquals(2, map.size)
            
            val localDate1 = java.time.Instant.ofEpochMilli(date1).atZone(ZoneId.systemDefault()).toLocalDate()
            val localDate2 = java.time.Instant.ofEpochMilli(date2).atZone(ZoneId.systemDefault()).toLocalDate()
            
            assertEquals(1, map[localDate1]) // 1 hour < 4 -> Level 1
            assertEquals(4, map[localDate2]) // 8 hours >= 8 -> Level 4
        }
    }
}
