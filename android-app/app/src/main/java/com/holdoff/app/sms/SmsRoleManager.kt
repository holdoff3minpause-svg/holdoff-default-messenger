package com.holdoff.app.sms

import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony

/** Requests the SMS role only after a person explicitly chooses full messenger setup. */
object SmsRoleManager {
    const val REQUEST_SMS_ROLE = 4101

    fun isDefaultSmsApp(context: Context): Boolean =
        Telephony.Sms.getDefaultSmsPackage(context) == context.packageName

    fun request(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roles = activity.getSystemService(RoleManager::class.java)
            if (roles.isRoleAvailable(RoleManager.ROLE_SMS) && !roles.isRoleHeld(RoleManager.ROLE_SMS)) {
                activity.startActivityForResult(
                    roles.createRequestRoleIntent(RoleManager.ROLE_SMS),
                    REQUEST_SMS_ROLE
                )
            }
        } else {
            activity.startActivity(Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT).apply {
                putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, activity.packageName)
            })
        }
    }
}
