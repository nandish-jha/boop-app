package com.prodash.reminders.ui.home

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.prodash.reminders.data.Reminder
import com.prodash.reminders.data.ReminderType
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private enum class HomeTab { HOME, REMINDERS, NOTES }

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HomeScreen(
    onAddReminder: () -> Unit,
    onOpenReminder: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
) {
    val reminders by viewModel.reminders.collectAsState()
    val selectedTab = rememberSaveable { androidx.compose.runtime.mutableStateOf(HomeTab.HOME) }

    val notes = remember(reminders) { reminders.filter { it.type == ReminderType.NOTE } }
    val tasks = remember(reminders) { reminders.filter { it.type == ReminderType.TASK } }
    val now = System.currentTimeMillis()
    val urgent = remember(tasks, now) { tasks.filter { !it.completed && it.dueEpochMillis in now..(now + 8 * 60 * 60 * 1000) } }
    val incompleteTasks = remember(tasks) { tasks.filter { !it.completed } }
    val completedTasks = remember(tasks) { tasks.filter { it.completed } }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "BOOP",
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onOpenProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddReminder, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.85f)) {
                NavigationBarItem(
                    selected = selectedTab.value == HomeTab.HOME,
                    onClick = { selectedTab.value = HomeTab.HOME },
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Home") },
                    alwaysShowLabel = false,
                )
                NavigationBarItem(
                    selected = selectedTab.value == HomeTab.REMINDERS,
                    onClick = { selectedTab.value = HomeTab.REMINDERS },
                    icon = { Icon(Icons.Default.Notifications, null) },
                    label = { Text("Reminders") },
                    alwaysShowLabel = false,
                )
                NavigationBarItem(
                    selected = selectedTab.value == HomeTab.NOTES,
                    onClick = { selectedTab.value = HomeTab.NOTES },
                    icon = { Icon(Icons.Default.Description, null) },
                    label = { Text("Notes") },
                    alwaysShowLabel = false,
                )
            }
        },
    ) { padding ->
        when (selectedTab.value) {
            HomeTab.HOME -> HomeDashboard(
                modifier = Modifier.padding(padding),
                urgent = urgent,
                notes = notes.take(4),
                onOpenReminder = onOpenReminder,
                onDeleteReminder = { viewModel.deleteReminder(it) },
                onViewAllNotes = { selectedTab.value = HomeTab.NOTES },
            )
            HomeTab.REMINDERS -> RemindersFeed(
                modifier = Modifier.padding(padding),
                incompleteTasks = incompleteTasks,
                completedTasks = completedTasks,
                onOpenReminder = onOpenReminder,
                onChecked = { item, checked -> viewModel.setCompleted(item, checked) },
                onDeleteReminder = { viewModel.deleteReminder(it) },
            )
            HomeTab.NOTES -> NotesGallery(
                modifier = Modifier.padding(padding),
                notes = notes,
                onOpenReminder = onOpenReminder,
                onDeleteReminder = { viewModel.deleteReminder(it) },
            )
        }
    }
}

@Composable
private fun HomeDashboard(
    modifier: Modifier,
    urgent: List<Reminder>,
    notes: List<Reminder>,
    onOpenReminder: (String) -> Unit,
    onDeleteReminder: (Reminder) -> Unit,
    onViewAllNotes: () -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("Good Morning.", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.ExtraBold)
            Text(
                "You have ${urgent.size} urgent reminders and ${notes.size} notes.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("URGENT PRIORITY", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                    if (urgent.isEmpty()) {
                        Text("No urgent reminders right now.")
                    } else {
                        urgent.take(2).forEach { item ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onOpenReminder(item.id) },
                                ) {
                                    Text(item.title, fontWeight = FontWeight.Bold)
                                    Text(
                                        "Due ${formatDateTime(item.dueEpochMillis)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                IconButton(onClick = { onDeleteReminder(item) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Recent Notes", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                TextButton(onClick = onViewAllNotes) { Text("View all") }
            }
        }
        items(notes, key = { it.id }) { note ->
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenReminder(note.id) },
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            note.title,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        IconButton(onClick = { onDeleteReminder(note) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                    if (note.body.isNotBlank()) {
                        Text(
                            note.body,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RemindersFeed(
    modifier: Modifier,
    incompleteTasks: List<Reminder>,
    completedTasks: List<Reminder>,
    onOpenReminder: (String) -> Unit,
    onChecked: (Reminder, Boolean) -> Unit,
    onDeleteReminder: (Reminder) -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        section("NOT COMPLETED", incompleteTasks, onOpenReminder, onChecked, onDeleteReminder)
        section("COMPLETED", completedTasks, onOpenReminder, onChecked, onDeleteReminder)
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.section(
    title: String,
    items: List<Reminder>,
    onOpenReminder: (String) -> Unit,
    onChecked: (Reminder, Boolean) -> Unit,
    onDeleteReminder: (Reminder) -> Unit,
) {
    item {
        Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
    }
    items(items, key = { it.id }) { item ->
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenReminder(item.id) },
            colors = CardDefaults.elevatedCardColors(
                containerColor = if (item.completed) {
                    MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.7f)
                } else {
                    MaterialTheme.colorScheme.surfaceContainerLow
                },
            ),
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(checked = item.completed, onCheckedChange = { onChecked(item, it) })
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(item.title, fontWeight = FontWeight.Bold)
                    Text(
                        formatDateTime(item.dueEpochMillis),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { onDeleteReminder(item) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

@Composable
private fun NotesGallery(
    modifier: Modifier,
    notes: List<Reminder>,
    onOpenReminder: (String) -> Unit,
    onDeleteReminder: (Reminder) -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (notes.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                    Text("No notes yet")
                }
            }
            return@LazyColumn
        }
        items(notes, key = { it.id }) { note ->
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenReminder(note.id) },
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            note.title,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                        )
                        IconButton(onClick = { onDeleteReminder(note) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                    if (note.body.isNotBlank()) {
                        Text(note.body, style = MaterialTheme.typography.bodySmall, maxLines = 4, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    note.imageUri?.let { image ->
                        AsyncImage(
                            model = image,
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
}

private fun formatDateTime(epochMillis: Long): String =
    Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("MMM d • h:mm a"))
