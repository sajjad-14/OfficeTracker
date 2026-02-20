package com.example.officetracker.ui.analytics

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class AnalyticsComponentsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun dayCell_displaysDayNumber() {
        val day = 15
        
        composeTestRule.setContent {
            DayCell(day = day, stat = null, goalSeconds = 28800L)
        }
        
        composeTestRule.onNodeWithText("15").assertIsDisplayed()
    }
    
    @Test
    fun heatmapCell_renders() {
        // HeatmapCell doesn't have text, just box.
        // We can check if it exists or tag it.
        // For now, just ensuring it doesn't crash is a basic test.
        composeTestRule.setContent {
             HeatmapCell(intensity = 4, date = LocalDate.now())
        }
    }
}
