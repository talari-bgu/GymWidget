package com.example.gymwidget

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

object DataManager {
    private const val FILE_NAME = "exercises.json"

    fun saveExercises(context: Context, exercises: List<Exercise>) {
        val json = Json.encodeToString(exercises)
        context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE).use {
            it.write(json.toByteArray())
        }
    }

    fun loadExercises(context: Context): List<Exercise> {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) return emptyList()

        return try {
            val json = file.readText()
            Json.decodeFromString(json)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
