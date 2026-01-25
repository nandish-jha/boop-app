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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.prodash.reminders.data.Reminder
import com.prodash.reminders.data.ReminderType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private enum class HomeTab { HOME, REMINDERS, CALENDAR, NOTES }
private enum class ItemFilter { ALL, TASKS, NOTES, COMPLETED }

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HomeScreen(
    onAddReminder: () -> Unit,
    onOpenReminder: (String) -> Unit,
    onOpenAccount: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
) {
    val reminders by viewModel.reminders.collectAsState()
    val selectedTab = rememberSaveable { androidx.compose.runtime.mutableStateOf(HomeTab.HOME) }
    val selectedFilter = rememberSaveable { androidx.compose.runtime.mutableStateOf(ItemFilter.ALL) }

    val notes = remember(reminders) { reminders.filter { it.type == ReminderType.NOTE } }
    val tasks = remember(reminders) { reminders.filter { it.type == ReminderType.TASK } }
    val now = System.currentTimeMillis()
    val urgent = remember(tasks, now) { tasks.filter { !it.completed && it.dueEpochMillis in now..(now + 8 * 60 * 60 * 1000) } }
    val incompleteTasks = remember(tasks) { tasks.filter { !it.completed } }
    val completedTasks = remember(tasks) { tasks.filter { it.completed } }
    val filteredNotes = remember(notes, selectedFilter.value) {
        when (selectedFilter.value) {
            ItemFilter.ALL -> notes
            ItemFilter.NOTES -> notes
            else -> emptyList()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Boop", fontWeight = FontWeight.ExtraBold) },
                actions = {
                    IconButton(onClick = onOpenAccount) {
                        Surface(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(MaterialTheme.shapes.small),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("👤")
                            }
                        }
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
                )
                NavigationBarItem(
                    selected = selectedTab.value == HomeTab.REMINDERS,
                    onClick = { selectedTab.value = HomeTab.REMINDERS },
                    icon = { Icon(Icons.Default.Notifications, null) },
                    label = { Text("Reminders") },
                )
                NavigationBarItem(
                    selected = selectedTab.value == HomeTab.CALENDAR,
                    onClick = { selectedTab.value = HomeTab.CALENDAR },
                    icon = { Icon(Icons.Default.CalendarMonth, null) },
                    label = { Text("Calendar") },
                )
                NavigationBarItem(
                    selected = selectedTab.value == HomeTab.NOTES,
                    onClick = { selectedTab.value = HomeTab.NOTES },
                    icon = { Icon(Icons.Default.Description, null) },
                    label = { Text("Notes") },
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
            )
            HomeTab.REMINDERS -> RemindersFeed(
                modifier = Modifier.padding(padding),
                incompleteTasks = incompleteTasks,
                completedTasks = completedTasks,
                onOpenReminder = onOpenReminder,
                onChecked = { item, checked -> viewModel.setCompleted(item, checked) },
            )
            HomeTab.CALENDAR -> CalendarScreen(
                modifier = Modifier.padding(padding),
                tasks = tasks,
                onOpenReminder = onOpenReminder,
            )
            HomeTab.NOTES -> NotesGallery(
                modifier = Modifier.padding(padding),
                notes = filteredNotes,
                selectedFilter = selectedFilter.value,
                onFilterChange = { selectedFilter.value = it },
                onOpenReminder = onOpenReminder,
            )
        }
    }
}

@Composable
private fun CalendarScreen(
    modifier: Modifier,
    tasks: List<Reminder>,
    onOpenReminder: (String) -> Unit,
) {
    val now = remember { LocalDate.now() }
    val monthLabel = remember { now.format(DateTimeFormatter.ofPattern("MMMM")) }
    val upcoming = remember(tasks) { tasks.filter { !it.completed }.sortedBy { it.dueEpochMillis } }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("SCHEDULE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(monthLabel, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.ExtraBold)
        }
        item {
            ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Today's Focus", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    if (upcoming.isEmpty()) {
                        Text("No scheduled events", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        upcoming.take(4).forEach { task ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenReminder(task.id) },
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(task.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        formatDateTime(task.dueEpochMillis),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Text("⋮", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeDashboard(
    modifier: Modifier,
    urgent: List<Reminder>,
    notes: List<Reminder>,
    onOpenReminder: (String) -> Unit,
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
                            Column(Modifier.clickable { onOpenReminder(item.id) }) {
                                Text(item.title, fontWeight = FontWeight.Bold)
                                Text(
                                    "Due ${formatDateTime(item.dueEpochMillis)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Recent Notes", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                TextButton(onClick = {}) { Text("View all") }
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
                    Text(note.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
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
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        section("NOT COMPLETED", incompleteTasks, onOpenReminder, onChecked)
        section("COMPLETED", completedTasks, onOpenReminder, onChecked)
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.section(
    title: String,
    items: List<Reminder>,
    onOpenReminder: (String) -> Unit,
    onChecked: (Reminder, Boolean) -> Unit,
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
            }
        }
    }
}

@Composable
private fun NotesGallery(
    modifier: Modifier,
    notes: List<Reminder>,
    selectedFilter: ItemFilter,
    onFilterChange: (ItemFilter) -> Unit,
    onOpenReminder: (String) -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedFilter == ItemFilter.ALL,
                    onClick = { onFilterChange(ItemFilter.ALL) },
                    label = { Text("All") },
                )
                FilterChip(
                    selected = selectedFilter == ItemFilter.TASKS,
                    onClick = { onFilterChange(ItemFilter.TASKS) },
                    label = { Text("Tasks") },
                )
                FilterChip(
                    selected = selectedFilter == ItemFilter.NOTES,
                    onClick = { onFilterChange(ItemFilter.NOTES) },
                    label = { Text("Notes") },
                )
                FilterChip(
                    selected = selectedFilter == ItemFilter.COMPLETED,
                    onClick = { onFilterChange(ItemFilter.COMPLETED) },
                    label = { Text("Completed") },
                )
            }
        }
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
                    Text(note.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 2)
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
