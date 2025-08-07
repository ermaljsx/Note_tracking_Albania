package com.voiceai.voicenotes.ui.screens

import android.Manifest
import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.voiceai.voicenotes.R
import com.voiceai.voicenotes.data.ai.OpenAIRepository
import com.voiceai.voicenotes.data.local.Task
import com.voiceai.voicenotes.data.repository.NotesRepository
import com.voiceai.voicenotes.data.stt.SpeechToTextManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainRecordScreen() {
    val ctx = LocalContext.current
    val app = ctx.applicationContext as Application

    val recordPerm = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    LaunchedEffect(Unit) { if (!recordPerm.status.isGranted) recordPerm.launchPermissionRequest() }

    val stt = remember { SpeechToTextManager(app) }
    val notesRepo = remember { NotesRepository(ctx) }
    val apiRepo = remember { OpenAIRepository { ctx.getSharedPreferences("settings", 0).getString("openai_key", null) } }

    val scope = rememberCoroutineScope()

    val transcript = remember { mutableStateOf("") }
    val enriched = remember { mutableStateOf("") }
    val isProcessing = remember { mutableStateOf(false) }
    val langIndex = remember { mutableIntStateOf(0) } // 0 auto, 1 en-US, 2 sq-AL

    LaunchedEffect(Unit) {
        stt.transcripts.collect { text -> transcript.value = text }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SegmentedButtonRow(Modifier.fillMaxWidth()) {
            val items = listOf(R.string.auto to null, R.string.english to "en-US", R.string.albanian to "sq-AL")
            items.forEachIndexed { idx, pair ->
                SegmentedButton(
                    selected = langIndex.intValue == idx,
                    onClick = { langIndex.intValue = idx },
                    label = { Text(stringResource(id = pair.first)) }
                )
            }
        }

        OutlinedTextField(
            value = transcript.value,
            onValueChange = { transcript.value = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Transcript") }
        )

        if (isProcessing.value) {
            CircularProgressIndicator()
            Text(stringResource(id = R.string.enhancing_note))
        }

        OutlinedTextField(
            value = enriched.value,
            onValueChange = { enriched.value = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("AI Note") }
        )

        Button(onClick = {
            val tag = when (langIndex.intValue) { 1 -> "en-US"; 2 -> "sq-AL"; else -> null }
            stt.startListening(tag)
        }, contentPadding = PaddingValues(16.dp)) { Text(stringResource(id = R.string.record)) }

        Button(onClick = { stt.stopListening() }) { Text(stringResource(id = R.string.stop)) }

        Button(onClick = {
            scope.launch {
                isProcessing.value = true
                val result = apiRepo.enrich(transcript.value, when (langIndex.intValue) { 1 -> "en-US"; 2 -> "sq-AL"; else -> null })
                isProcessing.value = false
                if (result != null) {
                    enriched.value = result.aiNoteText
                    val tasks = result.tasks.map { t ->
                        Task(
                            noteId = 0L,
                            title = t.title,
                            dueAtEpochMillis = t.dueIso8601?.let { runCatching { java.time.Instant.parse(it).toEpochMilli() }.getOrNull() },
                            isDone = false
                        )
                    }
                    notesRepo.createNote(
                        originalText = transcript.value,
                        languageTag = when (langIndex.intValue) { 1 -> "en-US"; 2 -> "sq-AL"; else -> "auto" },
                        enrichedText = enriched.value,
                        tasks = tasks
                    )
                }
            }
        }) { Text(stringResource(id = R.string.save)) }
    }
}