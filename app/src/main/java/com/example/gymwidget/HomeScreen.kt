package com.example.gymwidget

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.lineModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    date: String,
    onDateSelected: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = try {
            java.time.LocalDate.parse(date)
                .atStartOfDay(java.time.ZoneId.of("UTC"))
                .toInstant()
                .toEpochMilli()
        } catch (_: Exception) {
            null
        },
        selectableDates = object : androidx.compose.material3.SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= System.currentTimeMillis()
            }
        }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onDateSelected(datePickerState.selectedDateMillis)
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    OutlinedTextField(
        value = date,
        onValueChange = {},
        readOnly = true,
        label = { Text("Date") },
        trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Select Date"
                )
            }
        },
        modifier = modifier.fillMaxWidth(),
        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            .also { interactionSource ->
                LaunchedEffect(interactionSource) {
                    interactionSource.interactions.collect {
                        if (it is androidx.compose.foundation.interaction.PressInteraction.Release) {
                            showDatePicker = true
                        }
                    }
                }
            }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(innerPadding: PaddingValues, viewModel: HomeViewModel = viewModel()) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadExercises(context)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "My Exercises",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Category Filter Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = viewModel.selectedFilterCategory == null,
                    onClick = { viewModel.selectedFilterCategory = null },
                    label = { Text("All") }
                )
                ExerciseCategories.forEach { cat ->
                    FilterChip(
                        selected = viewModel.selectedFilterCategory == cat,
                        onClick = { viewModel.selectedFilterCategory = cat },
                        label = { Text(cat) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(viewModel.getFilteredExercises()) { exercise ->
                    ExerciseCard(
                        exercise = exercise,
                        formatDate = { viewModel.formatDate(it) },
                        onDetailsClick = { viewModel.onDetailsClick(exercise) },
                        isPR = viewModel.isPersonalRecord(exercise, exercise.logs.lastOrNull()?.weight ?: 0.0)
                    )
                }
            }
        }

        // Dialog for adding a COMPLETELY NEW exercise
        if (viewModel.showAddExerciseDialog) {
            ExerciseFormDialog(
                title = "New Exercise",
                name = viewModel.name,
                onNameChange = { viewModel.name = it },
                category = viewModel.category,
                onCategoryChange = { viewModel.category = it },
                sets = viewModel.sets,
                onSetsChange = { viewModel.sets = it },
                weight = viewModel.weight,
                onWeightChange = { viewModel.weight = it },
                date = viewModel.date,
                onDateSelected = { viewModel.updateDateFromMillis(it) },
                onConfirm = { viewModel.onSaveNewExercise(context) },
                onDismiss = { viewModel.onDismissDialog() }
            )
        }

        // Dialog for Exercise DETAILS and adding NEW LOGS to it
        viewModel.selectedExerciseForDetails?.let { exercise ->
            AlertDialog(
                onDismissRequest = { viewModel.onDismissDialog() },
                title = { 
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = exercise.name,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { viewModel.onDeleteClick() },
                            colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Exercise"
                            )
                        }
                    }
                },
                text = {
                    Column {
                        // 1RM Display
                        val lastLog = exercise.logs.lastOrNull()
                        if (lastLog != null) {
                            val oneRM = viewModel.calculateOneRepMax(lastLog.weight, lastLog.sets)
                            Text(
                                text = "Estimated 1RM: ${String.format(Locale.getDefault(), "%.1f", oneRM)}kg",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        Text("Add New Workout", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = viewModel.sets,
                            onValueChange = { viewModel.sets = it },
                            label = { Text("Sets (e.g., 5X5)") }
                        )
                        OutlinedTextField(
                            value = viewModel.weight,
                            onValueChange = { viewModel.weight = it },
                            label = { Text("Weight") }
                        )
                        DatePickerField(
                            date = viewModel.date,
                            onDateSelected = { viewModel.updateDateFromMillis(it) }
                        )
                        Button(
                            onClick = { viewModel.onAddLogToExisting(context) },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Add Log")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Progress History Chart
                        if (exercise.logs.size >= 2) {
                            val modelProducer = remember { CartesianChartModelProducer() }
                            val lastMonth = java.time.LocalDate.now().minusMonths(1)
                            val filteredLogs = exercise.logs.filter { 
                                try {
                                    java.time.LocalDate.parse(it.date).isAfter(lastMonth)
                                } catch (_: Exception) {
                                    false
                                }
                            }

                            if (filteredLogs.size >= 2) {
                                LaunchedEffect(filteredLogs) {
                                    modelProducer.runTransaction {
                                        lineModel {
                                            series(filteredLogs.map { it.weight })
                                        }
                                    }
                                }

                                Text("Weight Progress (Last Month)", style = MaterialTheme.typography.labelLarge)
                                CartesianChartHost(
                                    chart = rememberCartesianChart(
                                        rememberLineCartesianLayer(),
                                        startAxis = VerticalAxis.rememberStart(),
                                        bottomAxis = HorizontalAxis.rememberBottom(
                                            valueFormatter = { value, _, _ ->
                                                val index = value.toString().toDoubleOrNull()?.toInt() ?: -1
                                                if (index in filteredLogs.indices) {
                                                    viewModel.formatDate(filteredLogs[index].date)
                                                } else {
                                                    "\u200B" // Invisibility to avoid blank string crash
                                                }
                                            },
                                            itemPlacer = HorizontalAxis.ItemPlacer.aligned()
                                        ),
                                    ),
                                    modelProducer = modelProducer,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(150.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }

                        Text("Past Workouts", style = MaterialTheme.typography.titleSmall)
                        LazyColumn(modifier = Modifier.height(150.dp)) {
                            items(exercise.logs.reversed()) { log ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${viewModel.formatDate(log.date)}: ${log.sets} @ ${log.weight}kg")
                                    IconButton(
                                        onClick = { viewModel.onDeleteLogClick(log.id) },
                                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Log",
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.onDismissDialog() }) {
                        Text("Close")
                    }
                }
            )
        }

        // Are you sure popup for deletion
        if (viewModel.showDeleteConfirmation) {
            AlertDialog(
                onDismissRequest = { viewModel.onDismissDeleteConfirmation() },
                title = { Text("Delete Exercise") },
                text = { Text("Are you sure you want to delete '${viewModel.selectedExerciseForDetails?.name}' and all its logs? This cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = { viewModel.onDeleteConfirm(context) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.onDismissDeleteConfirmation() }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Are you sure popup for deleting a specific log
        if (viewModel.showDeleteLogConfirmation) {
            AlertDialog(
                onDismissRequest = { viewModel.onDismissDeleteConfirmation() },
                title = { Text("Delete Workout Log") },
                text = { Text("Are you sure you want to delete this specific workout log? This cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = { viewModel.onDeleteLogConfirm(context) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.onDismissDeleteConfirmation() }) {
                        Text("Cancel")
                    }
                }
            )
        }

        Button(
            onClick = { viewModel.onAddExerciseClick() },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Text("+ Add Exercise")
        }
    }
}

@Composable
fun ExerciseCard(
    exercise: Exercise,
    formatDate: (String) -> String,
    onDetailsClick: () -> Unit,
    isPR: Boolean
) {
    val lastLog = exercise.logs.lastOrNull()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Line 1: Name and PR badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = exercise.name, style = MaterialTheme.typography.titleLarge)
                    if (isPR) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                        ) {
                            Text(
                                text = "NEW PR!",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(text = exercise.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                
                lastLog?.let {
                    // Line 2: Weight and Sets
                    Text(
                        text = "${it.weight}kg - ${it.sets}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    // Line 3: Date
                    Text(
                        text = formatDate(it.date),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } ?: Text("No logs yet")
            }

            IconButton(
                onClick = onDetailsClick,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "View Details"
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseFormDialog(
    title: String,
    name: String,
    onNameChange: (String) -> Unit,
    category: String,
    onCategoryChange: (String) -> Unit,
    sets: String,
    onSetsChange: (String) -> Unit,
    weight: String,
    onWeightChange: (String) -> Unit,
    date: String,
    onDateSelected: (Long?) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = onNameChange, label = { Text("Name") })
                Spacer(modifier = Modifier.height(8.dp))
                
                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        ExerciseCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    onCategoryChange(cat)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = sets, onValueChange = onSetsChange, label = { Text("Sets") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = weight, onValueChange = onWeightChange, label = { Text("Weight") })
                Spacer(modifier = Modifier.height(8.dp))
                DatePickerField(date = date, onDateSelected = onDateSelected)
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
