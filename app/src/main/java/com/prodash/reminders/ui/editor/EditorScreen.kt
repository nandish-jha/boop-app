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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
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
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    reminderId: String?,
    initialType: ReminderType = ReminderType.TASK,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditorViewModel = viewModel(),
) {
    LaunchedEffect(reminderId) {
        viewModel.load(reminderId)
    }
    LaunchedEffect(reminderId, initialType) {
        if (reminderId == null) {
            viewModel.updateType(initialType)
        }
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
                        val pickedDate = Instant.ofEpochMilli(pickedMillis).atZone(ZoneOffset.UTC).toLocalDate()
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
                            if (viewModel.type == ReminderType.NOTE) "New Note" else "New Reminder"
                        } else {
                            stringResource(R.string.edit_reminder)
                        },
                        fontWeight = FontWeight.Bold,
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
                        enabled = viewModel.title.isNotBlank() && !viewModel.isSaving,
                    ) {
                        Text(if (viewModel.isSaving) "Saving..." else stringResource(R.string.save))
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
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(
                    selected = viewModel.type == ReminderType.NOTE,
                    onClick = { viewModel.updateType(ReminderType.NOTE) },
                    label = { Text("Notes") },
                )
                FilterChip(
                    selected = viewModel.type == ReminderType.TASK,
                    onClick = { viewModel.updateType(ReminderType.TASK) },
                    label = { Text("Tasks") },
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                tonalElevation = 1.dp,
                shape = MaterialTheme.shapes.extraLarge,
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (viewModel.type == ReminderType.NOTE) "NOTE LABEL" else "REMINDER LABEL",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    TextField(
                        value = viewModel.title,
                        onValueChange = viewModel::updateTitle,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("What needs doing?") },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        ),
                    )
                    if (viewModel.type == ReminderType.NOTE) {
                        TextField(
                            value = viewModel.body,
                            onValueChange = viewModel::updateBody,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp),
                            placeholder = { Text("Start your thought here...") },
                            textStyle = MaterialTheme.typography.titleLarge,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                                unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            ),
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (viewModel.type == ReminderType.NOTE) {
                OutlinedCard(
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = { imagePicker.launch("image/*") },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (viewModel.imageUri == null) "Add image" else "Change image")
                        }
                        viewModel.imageUri?.let { uri ->
                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp),
                                contentScale = ContentScale.Crop,
                            )
                        }
                    }
                }
            } else {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    tonalElevation = 1.dp,
                    shape = MaterialTheme.shapes.extraLarge,
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "SCHEDULE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = dateTimeLabel, style = MaterialTheme.typography.bodyLarge)
                        }
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
