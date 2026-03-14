package com.example.officetracker.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
    month: LocalDate = LocalDate.now(),
) {
    val firstDayOfMonth = month.withDayOfMonth(1)
    val lastDayOfMonth = month.plusMonths(1).withDayOfMonth(1).minusDays(1)
    val startMonthName = month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val year = month.year

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
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
                    text = "$startMonthName $year",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Day Labels Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val days = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                days.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar Grid
            val daysInMonth = lastDayOfMonth.dayOfMonth
            val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // Sun=0, Mon=1...
            
            val totalCells = ((daysInMonth + firstDayOfWeek + 6) / 7) * 7
            
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (week in 0 until (totalCells / 7)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (day in 0 until 7) {
                            val dayIndex = week * 7 + day
                            val dateNumber = dayIndex - firstDayOfWeek + 1
                            
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                if (dateNumber in 1..daysInMonth) {
                                    val date = firstDayOfMonth.withDayOfMonth(dateNumber)
                                    val intensity = data[date] ?: 0
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        HeatmapCell(intensity = intensity, date = date)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = dateNumber.toString(),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 8.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                } else {
                                    // Empty cell for padding - keep height consistent
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Spacer(modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 8.sp
                                        )
                                    }
                                }
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
                HeatmapCellStatic(0)
                Spacer(modifier = Modifier.width(2.dp))
                HeatmapCellStatic(1)
                Spacer(modifier = Modifier.width(2.dp))
                HeatmapCellStatic(2)
                Spacer(modifier = Modifier.width(2.dp))
                HeatmapCellStatic(3)
                Spacer(modifier = Modifier.width(2.dp))
                HeatmapCellStatic(4)
                Spacer(modifier = Modifier.width(4.dp))
                Text("More", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun HeatmapCell(intensity: Int, date: LocalDate) {
    val themeColor = getIntensityColor(intensity)
    Box(
        modifier = Modifier
            .size(16.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(themeColor)
    )
}

@Composable
fun HeatmapCellStatic(intensity: Int) {
    val themeColor = getIntensityColor(intensity)
    Box(
        modifier = Modifier
            .size(12.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(themeColor)
    )
}

@Composable
private fun getIntensityColor(intensity: Int): Color {
    return when (intensity) {
        0 -> MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        1 -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
        2 -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f) // Cyan hue
        3 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.9f) // Green/Teal hue
        4 -> MaterialTheme.colorScheme.primary // Full brightness green
        else -> MaterialTheme.colorScheme.surface
    }
}
