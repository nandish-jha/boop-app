package com.prodash.reminders.ui.home

import android.app.AlarmManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChecklistRtl
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.prodash.reminders.R
import com.prodash.reminders.data.Reminder
import com.prodash.reminders.data.ReminderType
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddReminder: () -> Unit,
    onOpenReminder: (String) -> Unit,
    onSignedOut: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
) {
    val reminders by viewModel.reminders.collectAsState()
    var menuExpanded by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val alarmManager = remember { context.getSystemService(AlarmManager::class.java) }
    val needsExactAlarm = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
        alarmManager != null &&
        !alarmManager.canScheduleExactAlarms()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Your notes & tasks") },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete completed tasks") },
                            onClick = {
                                menuExpanded = false
                                viewModel.deleteCompletedTasks()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.sign_out)) },
                            onClick = {
                                menuExpanded = false
                                viewModel.signOut(onSignedOut)
                            },
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddReminder) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_reminder))
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            AnimatedVisibility(visible = needsExactAlarm) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.exact_alarm_title),
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(
                                text = stringResource(R.string.exact_alarm_body),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        TextButton(
                            onClick = {
                                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                context.startActivity(intent)
                            },
                        ) {
                            Text(stringResource(R.string.open_settings))
                        }
                    }
                }
            }

            if (reminders.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(R.string.empty_state),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onAddReminder) {
                        Text(stringResource(R.string.add_reminder))
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 170.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 88.dp,
                    ),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(reminders, key = { it.id }) { reminder ->
                        ReminderRow(
                            reminder = reminder,
                            onOpen = { onOpenReminder(reminder.id) },
                            onCheckedChange = { checked ->
                                viewModel.setCompleted(reminder, checked)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReminderRow(
    reminder: Reminder,
    onOpen: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
) {
    val formatter = remember { DateTimeFormatter.ofPattern("EEE, MMM d • h:mm a") }
    val label = remember(reminder.dueEpochMillis) {
        if (reminder.type == ReminderType.TASK) {
            Instant.ofEpochMilli(reminder.dueEpochMillis)
                .atZone(ZoneId.systemDefault())
                .format(formatter)
        } else {
            "Note"
        }
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .padding(vertical = 1.dp)
            .height(if (reminder.imageUri != null) 220.dp else 150.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (reminder.type == ReminderType.TASK) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = reminder.completed,
                            onCheckedChange = onCheckedChange,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ChecklistRtl, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Task", style = MaterialTheme.typography.labelSmall)
                    }
                } else {
                    AssistChip(
                        onClick = onOpen,
                        label = { Text("Note") },
                        leadingIcon = { Icon(Icons.Default.StickyNote2, contentDescription = null) },
                    )
                }
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (reminder.completed) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (reminder.type == ReminderType.NOTE && reminder.body.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = reminder.body,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (reminder.type == ReminderType.NOTE && reminder.imageUri != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    AsyncImage(
                        model = reminder.imageUri,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(92.dp),
                        contentScale = ContentScale.Crop,
                    )
                }
            }
        }
    }
}
