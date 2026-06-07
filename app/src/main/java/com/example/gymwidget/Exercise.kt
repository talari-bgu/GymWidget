package com.example.gymwidget

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Represents a single logged workout movement history.
 */
@Serializable
data class Exercise(
    val id: String = UUID.randomUUID().toString(), // Unique ID for list rendering stability
    val name: String,
    val category: String = "Other", // Default for migration safety
    val logs: List<WorkoutLog> = emptyList()
)

@Serializable
data class WorkoutLog(
    val id: String = UUID.randomUUID().toString(),
    val sets: String,
    val weight: Double,
    val date: String
)

val ExerciseCategories = listOf(
    "Chest", "Back", "Legs", "Shoulders", "Biceps", "Triceps", "Core"
)
