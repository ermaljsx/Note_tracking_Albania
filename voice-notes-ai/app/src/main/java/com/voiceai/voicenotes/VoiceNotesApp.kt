package com.voiceai.voicenotes

import android.app.Application
import com.voiceai.voicenotes.reminder.NotificationUtils

class VoiceNotesApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationUtils.createNotificationChannel(this)
    }
}