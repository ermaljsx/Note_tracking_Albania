package com.voiceai.voicenotes.data.repository

import android.content.Context
import com.voiceai.voicenotes.data.local.AppDatabase
import com.voiceai.voicenotes.data.local.Note
import com.voiceai.voicenotes.data.local.Task
import kotlinx.coroutines.flow.Flow

class NotesRepository(context: Context) {
    private val db = AppDatabase.get(context)
    private val noteDao = db.noteDao()
    private val taskDao = db.taskDao()

    fun observeNotes() = noteDao.observeNotes()
    fun observeAllTasks() = taskDao.observeAllTasks()

    suspend fun createNote(
        originalText: String,
        languageTag: String,
        enrichedText: String?,
        tasks: List<Task>
    ): Long {
        val noteId = noteDao.insert(
            Note(
                originalText = originalText,
                enrichedText = enrichedText,
                createdAt = System.currentTimeMillis(),
                languageTag = languageTag
            )
        )
        tasks.forEach { task -> taskDao.insert(task.copy(noteId = noteId)) }
        return noteId
    }

    fun observeTasksForNote(noteId: Long): Flow<List<Task>> = taskDao.observeTasksForNote(noteId)

    suspend fun updateTask(task: Task) = taskDao.update(task)
}