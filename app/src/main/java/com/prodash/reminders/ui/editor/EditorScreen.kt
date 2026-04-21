package com.prodash.reminders.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.prodash.reminders.R
import com.prodash.reminders.data.ReminderType
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    reminderId: String?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditorViewModel = viewModel(),
) {
    LaunchedEffect(reminderId) {
        viewModel.load(reminderId)
    }

    if (!viewModel.loaded) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
        ) {
            CircularProgressIndicator()
        }
        return
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val imagePicker = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent(),
    ) { uri ->
        viewModel.updateImageUri(uri?.toString())
    }

    val zone = remember { ZoneId.systemDefault() }
    val dateTimeLabel = remember(viewModel.dueEpochMillis) {
        val dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(viewModel.dueEpochMillis), zone)
        dt.format(DateTimeFormatter.ofPattern("MMM d, yyyy • h:mm a"))
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = viewModel.dueEpochMillis,
            initialDisplayMode = DisplayMode.Picker,
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val pickedMillis = datePickerState.selectedDateMillis ?: viewModel.dueEpochMillis
                        val current = LocalDateTime.ofInstant(Instant.ofEpochMilli(viewModel.dueEpochMillis), zone)
                        val pickedDate = Instant.ofEpochMilli(pickedMillis).atZone(zone).toLocalDate()
                        val merged = LocalDateTime.of(pickedDate, current.toLocalTime())
                        viewModel.updateDue(merged.atZone(zone).toInstant().toEpochMilli())
                        showDatePicker = false
                    },
                ) {
                    Text(stringResource(R.string.done))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val current = LocalDateTime.ofInstant(Instant.ofEpochMilli(viewModel.dueEpochMillis), zone)
        val timeState = rememberTimePickerState(
            initialHour = current.hour,
            initialMinute = current.minute,
            is24Hour = false,
        )
        Dialog(onDismissRequest = { showTimePicker = false }) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp,
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    TimePicker(
                        state = timeState,
                        colors = TimePickerDefaults.colors(),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = { showTimePicker = false }) {
                            Text(stringResource(android.R.string.cancel))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = {
                                val date = LocalDateTime.ofInstant(
                                    Instant.ofEpochMilli(viewModel.dueEpochMillis),
                                    zone,
                                ).toLocalDate()
                                val merged = LocalDateTime.of(date, LocalTime.of(timeState.hour, timeState.minute))
                                viewModel.updateDue(merged.atZone(zone).toInstant().toEpochMilli())
                                showTimePicker = false
                            },
                        ) {
                            Text(stringResource(R.string.done))
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (reminderId == null) {
                            stringResource(R.string.add_reminder)
                        } else {
                            stringResource(R.string.edit_reminder)
                        },
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.save(reminderId) { onBack() } },
                        enabled = viewModel.title.isNotBlank(),
                    ) {
                        Text(stringResource(R.string.save))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = viewModel.type == ReminderType.NOTE,
                    onClick = { viewModel.updateType(ReminderType.NOTE) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = 0,
                        count = 2,
                    ),
                ) { Text("Note") }
                SegmentedButton(
                    selected = viewModel.type == ReminderType.TASK,
                    onClick = { viewModel.updateType(ReminderType.TASK) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = 1,
                        count = 2,
                    ),
                ) { Text("Task") }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                tonalElevation = 1.dp,
                shape = MaterialTheme.shapes.large,
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    OutlinedTextField(
                        value = viewModel.title,
                        onValueChange = viewModel::updateTitle,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(if (viewModel.type == ReminderType.NOTE) "Title" else "Task title") },
                        singleLine = true,
                    )
                    if (viewModel.type == ReminderType.NOTE) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = viewModel.body,
                            onValueChange = viewModel::updateBody,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            label = { Text("Write your note") },
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (viewModel.type == ReminderType.NOTE) {
                OutlinedButton(
                    onClick = { imagePicker.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.Image, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (viewModel.imageUri == null) "Add image" else "Change image")
                }
                viewModel.imageUri?.let { uri ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(shape = MaterialTheme.shapes.large) {
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(210.dp),
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
            } else {
                Surface(
                    tonalElevation = 1.dp,
                    shape = MaterialTheme.shapes.large,
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = dateTimeLabel, style = MaterialTheme.typography.bodyLarge)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(onClick = { showDatePicker = true }) {
                                Text(stringResource(R.string.pick_date))
                            }
                            OutlinedButton(onClick = { showTimePicker = true }) {
                                Text(stringResource(R.string.pick_time))
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
