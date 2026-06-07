package com.example.gymwidget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.GlanceTheme
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.width
import java.time.LocalDate
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

class ProgressWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val exercises = DataManager.loadExercises(context)
        
        val lastWeek = LocalDate.now().minusDays(7)
        
        // Group logs by date for the last 7 days
        val lastWeekWorkouts = exercises.flatMap { e -> 
            e.logs.map { l -> e.category to l } 
        }
        .filter { (_, log) -> 
            try {
                LocalDate.parse(log.date).isAfter(lastWeek)
            } catch (_: Exception) {
                false
            }
        }
        .groupBy { it.second.date }
        .mapValues { entry -> 
            entry.value.map { it.first }.distinct().sorted() 
        }
        .toList()
        .sortedByDescending { it.first }

        provideContent {
            GlanceTheme {
                WidgetContent(lastWeekWorkouts)
            }
        }
    }

    @Composable
    private fun WidgetContent(workouts: List<Pair<String, List<String>>>) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface)
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Last Week",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                )
            )
            
            Spacer(modifier = GlanceModifier.height(8.dp))
            
            if (workouts.isNotEmpty()) {
                workouts.forEach { (date, categories) ->
                    val dayName = try {
                        val localDate = LocalDate.parse(date)
                        val now = LocalDate.now()
                        when {
                            localDate.isEqual(now) -> "Today"
                            else -> localDate.dayOfWeek.getDisplayName(JavaTextStyle.SHORT, Locale.getDefault())
                        }
                    } catch (_: Exception) {
                        date
                    }

                    Row(
                        modifier = GlanceModifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dayName,
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = GlanceTheme.colors.primary
                            ),
                            modifier = GlanceModifier.width(40.dp)
                        )
                        Text(
                            text = categories.joinToString(", "),
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = GlanceTheme.colors.onSurface
                            )
                        )
                    }
                }
            } else {
                Text(
                    text = "No workouts this week",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = GlanceTheme.colors.onSurface
                    )
                )
            }
        }
    }
}
