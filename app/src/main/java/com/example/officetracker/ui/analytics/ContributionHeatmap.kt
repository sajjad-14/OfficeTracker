package com.example.officetracker.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun ContributionHeatmap(
    data: Map<LocalDate, Int>, // Date -> Intensity (0-4)
    endDate: LocalDate = LocalDate.now(),
    weeksToShow: Int = 18
) {
    val weeks = rememberHeatmapWeeks(data, endDate, weeksToShow)
    val startMonth = weeks.firstOrNull()?.days?.firstOrNull()?.date?.month?.getDisplayName(TextStyle.SHORT, Locale.getDefault()) ?: ""
    val endMonth = endDate.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Consistency Map",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$startMonth - $endMonth",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                // Day Labels
                Column(
                    modifier = Modifier
                        .padding(end = 8.dp, top = 0.dp)
                        .height(118.dp), // Approx height of grid (7 * 12 + 6 * 4 + padding?)
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Mon", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Wed", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Fri", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(weeks) { week ->
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            week.days.forEach { dayData ->
                                HeatmapCell(intensity = dayData.intensity, date = dayData.date)
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Less", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(4.dp))
                HeatmapCell(0, LocalDate.now())
                Spacer(modifier = Modifier.width(2.dp))
                HeatmapCell(1, LocalDate.now())
                Spacer(modifier = Modifier.width(2.dp))
                HeatmapCell(2, LocalDate.now())
                Spacer(modifier = Modifier.width(2.dp))
                HeatmapCell(3, LocalDate.now())
                Spacer(modifier = Modifier.width(2.dp))
                HeatmapCell(4, LocalDate.now())
                Spacer(modifier = Modifier.width(4.dp))
                Text("More", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

data class DayData(
    val date: LocalDate,
    val intensity: Int
)

data class WeekData(
    val days: List<DayData>
)

@Composable
fun HeatmapCell(intensity: Int, date: LocalDate) {
    val color = when (intensity) {
        0 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        1 -> Color(0xFF9BE9A8) // GitHub Light Green
        2 -> Color(0xFF40C463)
        3 -> Color(0xFF30A14E)
        4 -> Color(0xFF216E39) // GitHub Dark Green
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    // Check if system is dark theme to adjust colors? 
    // For now hardcoded "cool" greens, but let's use MaterialTheme if possible for consistency.
    // Actually user said "cool", maybe neon?
    // Let's stick to theme primaries but with variations.
    
    val themeColor = when (intensity) {
         0 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
         1 -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
         2 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
         3 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
         4 -> MaterialTheme.colorScheme.primary
         else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Box(
        modifier = Modifier
            .size(12.dp)
            .clip(RoundedCornerShape(4.dp)) // Rounder
            .background(themeColor)
    )
}

fun rememberHeatmapWeeks(
    data: Map<LocalDate, Int>,
    endDate: LocalDate,
    count: Int
): List<WeekData> {
    val weeks = mutableListOf<WeekData>()
    var current = endDate
    
    // GitHub ends on Today.
    // Find Sat of this week.
    val dayOfWeek = current.dayOfWeek.value % 7 // Sun=0...
    // Adjust to end on Saturday
    val daysUntilSat = 6 - dayOfWeek
    val gridEndDate = current.plusDays(daysUntilSat.toLong())
    
    // We want 'count' weeks ending at 'gridEndDate'
    val weekDuration = 7
    val totalDays = count * weekDuration
    
    val gridStartDate = gridEndDate.minusDays((totalDays - 1).toLong())
    
    var iterDate = gridStartDate

    repeat(count) {
        val days = mutableListOf<DayData>()
        repeat(7) {
            val intensity = data[iterDate] ?: 0
            // Don't show future days in the last week?
            val actualIntensity = if (iterDate.isAfter(endDate)) 0 else intensity
            
            days.add(DayData(iterDate, actualIntensity))
            iterDate = iterDate.plusDays(1)
        }
        weeks.add(WeekData(days))
    }
    
    return weeks
}
