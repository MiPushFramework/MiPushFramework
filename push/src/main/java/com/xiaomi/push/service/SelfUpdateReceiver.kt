package com.xiaomi.push.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.xiaomi.push.revival.NotificationRevival

class SelfUpdateReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_MY_PACKAGE_REPLACED) return
        NotificationRevival.restoreNotificationsAsync(context)
    }
}