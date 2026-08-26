package com.holdoff.app.sms

import android.content.Context
import android.telephony.SmsManager

/** Sends only after the visible Send action. No background or accessibility interception. */
class SmsSender(private val context: Context) {
    fun send(address: String, body: String): Result<Unit> = runCatching {
        require(address.isNotBlank()) { "Choose a recipient." }
        require(body.isNotBlank()) { "Write a message first." }
        val drafts = DraftStore(context)
        val draftId = drafts.hold(address, body)
        drafts.updateState(draftId, DraftState.READY_TO_SEND)
        try {
            SmsManager.getDefault().sendTextMessage(address, null, body, null, null)
            drafts.updateState(draftId, DraftState.SENT)
        } catch (e: Exception) {
            drafts.updateState(draftId, DraftState.FAILED)
            throw e
        }
    }
}
