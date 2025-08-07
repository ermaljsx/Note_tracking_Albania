package com.voiceai.voicenotes.ui.screens

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.voiceai.voicenotes.data.local.Note
import com.voiceai.voicenotes.data.local.AppDatabase

@Composable
fun NotesListScreen() {
    val ctx = LocalContext.current
    val dao = remember { AppDatabase.get(ctx).noteDao() }
    val notes by dao.observeNotes().collectAsState(initial = emptyList())

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(notes) { note ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { }
                    .padding(16.dp)
            ) {
                Text(text = (note.enrichedText ?: note.originalText).lineSequence().firstOrNull() ?: "")
                Text(text = note.originalText, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}