package com.holdoff.app.sms

import android.content.ContentResolver
import android.content.Context
import android.provider.Telephony

data class Conversation(val threadId: Long, val address: String, val snippet: String, val date: Long)
data class TextMessage(val id: Long, val address: String, val body: String, val date: Long, val outgoing: Boolean)

/** Reads the system SMS provider only while HoldOff holds the SMS role and permission. */
class MessageRepository(context: Context) {
    private val resolver: ContentResolver = context.contentResolver

    fun conversations(): List<Conversation> {
        val rows = mutableListOf<Conversation>()
        resolver.query(
            Telephony.Sms.Conversations.CONTENT_URI,
            arrayOf("thread_id", "address", "snippet", "date"),
            null, null, "date DESC"
        )?.use { c ->
            while (c.moveToNext()) rows += Conversation(c.getLong(0), c.getString(1).orEmpty(), c.getString(2).orEmpty(), c.getLong(3))
        }
        return rows
    }

    fun messages(threadId: Long): List<TextMessage> {
        val rows = mutableListOf<TextMessage>()
        resolver.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf(Telephony.Sms._ID, Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE, Telephony.Sms.TYPE),
            "thread_id=?", arrayOf(threadId.toString()), "date ASC"
        )?.use { c ->
            while (c.moveToNext()) rows += TextMessage(c.getLong(0), c.getString(1).orEmpty(), c.getString(2).orEmpty(), c.getLong(3), c.getInt(4) == Telephony.Sms.MESSAGE_TYPE_SENT)
        }
        return rows
    }
}
