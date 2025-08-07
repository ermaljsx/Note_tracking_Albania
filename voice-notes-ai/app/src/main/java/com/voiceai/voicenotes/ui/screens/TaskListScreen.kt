package com.voiceai.voicenotes.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.voiceai.voicenotes.calendar.CalendarUtils
import com.voiceai.voicenotes.data.local.AppDatabase
import com.voiceai.voicenotes.data.local.Task
import com.voiceai.voicenotes.reminder.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun TaskListScreen() {
    val ctx = LocalContext.current
    val db = remember { AppDatabase.get(ctx) }
    val tasks by db.taskDao().observeAllTasks().collectAsState(initial = emptyList())

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(tasks) { task ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row { Checkbox(checked = task.isDone, onCheckedChange = { checked ->
                    CoroutineScope(Dispatchers.IO).launch {
                        db.taskDao().update(task.copy(isDone = checked))
                    }
                }); Text(task.title) }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { CalendarUtils.openInsertEvent(ctx, task.title, task.dueAtEpochMillis) }) { Text("Calendar") }
                    Button(onClick = {
                        val whenMillis = task.dueAtEpochMillis ?: (System.currentTimeMillis() + 60_000)
                        ReminderScheduler.schedule(ctx, whenMillis, task.id.toInt(), task.title)
                    }) { Text("Remind") }
                }
            }
        }
    }
}