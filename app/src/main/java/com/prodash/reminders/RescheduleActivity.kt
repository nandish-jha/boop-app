package com.prodash.reminders

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.prodash.reminders.data.ReminderRepository
import com.prodash.reminders.notification.ReminderNotificationManager
import com.prodash.reminders.schedule.ReminderScheduler
import com.prodash.reminders.ui.theme.AppTheme
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlinx.coroutines.launch

class RescheduleActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val reminderId = intent.getStringExtra(EXTRA_REMINDER_ID)
        if (reminderId.isNullOrBlank()) {
            finish()
            return
        }

        setContent {
            AppTheme {
                val scope = rememberCoroutineScope()
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                val repo = remember { ReminderRepository() }
                val zone = remember { ZoneId.systemDefault() }

                fun rescheduleTo(newDueMillis: Long) {
                    scope.launch {
                        repo.updateDue(reminderId, newDueMillis)
                        val updated = repo.fetchOne(reminderId) ?: return@launch
                        ReminderNotificationManager.cancel(this@RescheduleActivity, reminderId)
                        ReminderScheduler.schedule(this@RescheduleActivity, updated)
                        finish()
                    }
                }

                ModalBottomSheet(
                    onDismissRequest = { finish() },
                    sheetState = sheetState,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.reschedule_title),
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                rescheduleTo(System.currentTimeMillis() + 15 * 60 * 1000L)
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(stringResource(R.string.snooze_15m))
                        }

                        Button(
                            onClick = {
                                rescheduleTo(System.currentTimeMillis() + 60 * 60 * 1000L)
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(stringResource(R.string.snooze_1h))
                        }

                        Button(
                            onClick = {
                                val tomorrow = LocalDate.now(zone).plusDays(1)
                                var target = ZonedDateTime.of(tomorrow, LocalTime.of(9, 0), zone)
                                var millis = target.toInstant().toEpochMilli()
                                if (millis <= System.currentTimeMillis()) {
                                    target = target.plusDays(1)
                                    millis = target.toInstant().toEpochMilli()
                                }
                                rescheduleTo(millis)
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(stringResource(R.string.snooze_tomorrow))
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
    }
}
