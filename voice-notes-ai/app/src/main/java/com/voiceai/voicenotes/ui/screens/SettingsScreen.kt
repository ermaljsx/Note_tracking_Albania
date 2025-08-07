package com.voiceai.voicenotes.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen() {
    val ctx = LocalContext.current
    val prefs = remember { ctx.getSharedPreferences("settings", 0) }
    val keyState = remember { mutableStateOf(prefs.getString("openai_key", "") ?: "") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("OpenAI API Key")
        OutlinedTextField(
            value = keyState.value,
            onValueChange = { keyState.value = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("sk-...") }
        )
        Button(onClick = { prefs.edit().putString("openai_key", keyState.value).apply() }, modifier = Modifier.padding(top = 8.dp)) {
            Text("Save")
        }
    }
}