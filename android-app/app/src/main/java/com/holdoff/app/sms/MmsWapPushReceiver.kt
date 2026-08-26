package com.holdoff.app.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Required default-SMS component; MMS transport is intentionally not represented as completed UI support yet. */
class MmsWapPushReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = Unit
}
