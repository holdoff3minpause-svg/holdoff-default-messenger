package com.holdoff.app.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony

/** Default-SMS delivery endpoint. The system provider owns persistence; HoldOff refreshes its UI. */
class SmsDeliverReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Telephony.Sms.Intents.SMS_DELIVER_ACTION != intent.action) return
        // Deliberately no outbound analysis or automatic reply here. Incoming messages remain in the SMS provider.
    }
}
