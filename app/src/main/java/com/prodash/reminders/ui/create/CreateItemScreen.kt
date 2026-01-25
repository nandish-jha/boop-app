package com.prodash.reminders.ui.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateItemScreen(
    onClose: () -> Unit,
    onCreateNote: () -> Unit,
    onCreateReminder: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BOOP", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("Create\nNew Item", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.ExtraBold)
            Text(
                "Select a format to capture your thoughts, tasks, or immediate voice reminders.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.Description, contentDescription = null)
                    Text("New Note", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Architectural thoughts and long-form archive for your digital workspace.")
                    Button(onClick = onCreateNote, modifier = Modifier.fillMaxWidth()) {
                        Text("Create Archive")
                    }
                }
            }

            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.Notifications, contentDescription = null)
                    Text("New Reminder", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Set precision alerts and temporal triggers for your daily workflow.")
                    Button(onClick = onCreateReminder, modifier = Modifier.fillMaxWidth()) {
                        Text("Schedule Alert")
                    }
                }
            }

            Spacer(Modifier.height(6.dp))
            Button(onClick = { }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Mic, contentDescription = null)
                Spacer(Modifier.height(0.dp))
                Text("  Quick Capture Voice")
            }
        }
    }
}
