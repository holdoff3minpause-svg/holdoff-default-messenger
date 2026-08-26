package com.holdoff.app.sms

import android.app.Service
import android.content.Intent
import android.os.IBinder

/** System quick-response entry point. UI handling is added only after its device flow is validated. */
class RespondViaMessageService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        stopSelf(startId)
        return START_NOT_STICKY
    }
}
