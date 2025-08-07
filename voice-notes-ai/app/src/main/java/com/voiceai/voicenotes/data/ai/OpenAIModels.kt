package com.voiceai.voicenotes.data.ai

import com.squareup.moshi.Json

data class ChatRequest(
    val model: String,
    val messages: List<Message>,
    @Json(name = "response_format") val responseFormat: ResponseFormat? = ResponseFormat("json_object")
)

data class Message(
    val role: String,
    val content: String
)

data class ResponseFormat(val type: String)

data class ChatResponse(
    val choices: List<Choice>
) {
    data class Choice(val message: Message)
}

// Structured response parsed from the model
data class EnrichedNote(
    @Json(name = "ai_note_text") val aiNoteText: String,
    val tasks: List<EnrichedTask> = emptyList(),
    val suggestions: List<String> = emptyList()
)

data class EnrichedTask(
    val title: String,
    @Json(name = "due_iso8601") val dueIso8601: String? = null
)