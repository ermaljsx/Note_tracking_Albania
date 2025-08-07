package com.voiceai.voicenotes.calendar

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract

object CalendarUtils {
    fun openInsertEvent(context: Context, title: String, beginMillis: Long? = null, endMillis: Long? = null) {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, title)
            if (beginMillis != null) putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginMillis)
            if (endMillis != null) putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
        }
        context.startActivity(intent)
    }
}