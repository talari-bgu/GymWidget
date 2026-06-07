package com.example.gymwidget

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

class HomeViewModel : ViewModel() {
    // UI State
    var exercises by mutableStateOf(emptyList<Exercise>())
        private set

    var showAddExerciseDialog by mutableStateOf(false)
    var showDeleteConfirmation by mutableStateOf(false)
    var showDeleteLogConfirmation by mutableStateOf(false)
    var selectedExerciseForDetails by mutableStateOf<Exercise?>(null)
    var logToDeleteId by mutableStateOf<String?>(null)

    // Form fields
    var name by mutableStateOf("")
    var category by mutableStateOf(ExerciseCategories.first())
    var sets by mutableStateOf("")
    var weight by mutableStateOf("")
    var date by mutableStateOf(LocalDate.now().toString())

    // Filter state
    var selectedFilterCategory by mutableStateOf<String?>(null)

    fun loadExercises(context: Context) {
        viewModelScope.launch {
            exercises = DataManager.loadExercises(context)
        }
    }

    fun onAddExerciseClick() {
        showAddExerciseDialog = true
        date = LocalDate.now().toString()
        category = ExerciseCategories.first()
    }

    fun onDetailsClick(exercise: Exercise) {
        selectedExerciseForDetails = exercise
    }

    fun onDeleteClick() {
        showDeleteConfirmation = true
    }

    fun onDeleteConfirm(context: Context) {
        val exerciseToDelete = selectedExerciseForDetails ?: return
        val updatedExercises = exercises.filter { it.id != exerciseToDelete.id }
        saveAndUpdate(context, updatedExercises)
        showDeleteConfirmation = false
        selectedExerciseForDetails = null
    }

    fun onDismissDeleteConfirmation() {
        showDeleteConfirmation = false
        showDeleteLogConfirmation = false
        logToDeleteId = null
    }

    fun onDismissDialog() {
        showAddExerciseDialog = false
        showDeleteConfirmation = false
        showDeleteLogConfirmation = false
        selectedExerciseForDetails = null
        logToDeleteId = null
        resetFields()
    }

    fun onSaveNewExercise(context: Context) {
        val weightDouble = weight.toDoubleOrNull() ?: 0.0
        val newLog = WorkoutLog(sets = sets, weight = weightDouble, date = date)
        
        val updatedExercises = exercises.toMutableList()
        val existingIndex = updatedExercises.indexOfFirst { it.name.equals(name, ignoreCase = true) }

        if (existingIndex != -1) {
            val existing = updatedExercises[existingIndex]
            updatedExercises[existingIndex] = existing.copy(logs = existing.logs + newLog)
        } else {
            updatedExercises.add(Exercise(name = name, category = category, logs = listOf(newLog)))
        }

        saveAndUpdate(context, updatedExercises)
        onDismissDialog()
    }

    fun onAddLogToExisting(context: Context) {
        val exercise = selectedExerciseForDetails ?: return
        val weightDouble = weight.toDoubleOrNull() ?: 0.0
        val newLog = WorkoutLog(sets = sets, weight = weightDouble, date = date)

        val updatedExercises = exercises.map {
            if (it.id == exercise.id) {
                it.copy(logs = it.logs + newLog)
            } else it
        }

        saveAndUpdate(context, updatedExercises)
        selectedExerciseForDetails = updatedExercises.find { it.id == exercise.id }
        resetFields()
    }

    fun updateDateFromMillis(millis: Long?) {
        millis?.let {
            date = java.time.Instant.ofEpochMilli(it)
                .atZone(java.time.ZoneId.of("UTC"))
                .toLocalDate()
                .toString()
        }
    }

    fun onDeleteLogClick(logId: String) {
        logToDeleteId = logId
        showDeleteLogConfirmation = true
    }

    fun onDeleteLogConfirm(context: Context) {
        val exercise = selectedExerciseForDetails ?: return
        val logId = logToDeleteId ?: return
        
        val updatedExercises = exercises.map {
            if (it.id == exercise.id) {
                it.copy(logs = it.logs.filter { log -> log.id != logId })
            } else it
        }

        saveAndUpdate(context, updatedExercises)
        selectedExerciseForDetails = updatedExercises.find { it.id == exercise.id }
        showDeleteLogConfirmation = false
        logToDeleteId = null
    }

    private fun saveAndUpdate(context: Context, updatedList: List<Exercise>) {
        exercises = updatedList
        DataManager.saveExercises(context, updatedList)
        
        // Trigger widget update
        viewModelScope.launch {
            val glanceIds = androidx.glance.appwidget.GlanceAppWidgetManager(context)
                .getGlanceIds(ProgressWidget::class.java)
            glanceIds.forEach { id ->
                ProgressWidget().update(context, id)
            }
        }
    }

    private fun resetFields() {
        name = ""
        sets = ""
        weight = ""
        date = LocalDate.now().toString()
    }

    fun formatDate(dateString: String): String {
        return try {
            val date = LocalDate.parse(dateString)
            val now = LocalDate.now()
            
            when {
                date.isEqual(now) -> "Today"
                ChronoUnit.DAYS.between(date, now) < 7 -> {
                    date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
                }
                else -> "${date.dayOfMonth}/${date.monthValue}"
            }
        } catch (e: Exception) {
            dateString
        }
    }

    fun isPersonalRecord(exercise: Exercise, weight: Double): Boolean {
        if (exercise.logs.isEmpty()) return true
        val maxWeight = exercise.logs.maxOfOrNull { it.weight } ?: 0.0
        return weight >= maxWeight && weight > 0.0
    }

    fun calculateOneRepMax(weight: Double, setsString: String): Double {
        val reps = setsString.lowercase().substringAfter('x').trim().toDoubleOrNull() ?: 1.0
        return if (reps <= 1) weight else weight * (1 + reps / 30.0)
    }

    fun getFilteredExercises(): List<Exercise> {
        val filter = selectedFilterCategory
        return if (filter == null) exercises else exercises.filter { it.category == filter }
    }
}
