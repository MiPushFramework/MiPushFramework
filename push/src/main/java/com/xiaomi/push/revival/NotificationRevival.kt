package com.xiaomi.push.revival

import android.app.AlarmManager
import android.app.Notification
import android.app.Notification.FLAG_GROUP_SUMMARY
import android.app.Notification.GROUP_ALERT_CHILDREN
import android.app.Notification.GROUP_ALERT_SUMMARY
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_NO_CREATE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.BroadcastReceiver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.Intent.FLAG_RECEIVER_FOREGROUND
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.M
import android.os.Build.VERSION_CODES.O
import android.os.SystemClock
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.annotation.RequiresApi
import com.xiaomi.xmsf.BuildConfig

/** Increase the version if breaking changes have been made to notification data, to avoid restoring from incompatible version. */
private const val BACKUP_VERSION = 1
/** Max time to keep the saved notifications */
private const val TIMEOUT = 30_000
private const val TIMEOUT_DEBUG = 5 * 60_000

/**
 * Save selected notifications when this module is being updated (by [PackageInstaller]),
 * and restored them afterwards (commonly upon [Intent.ACTION_MY_PACKAGE_REPLACED]).
 *
 * BEWARE: This may break restored notifications with local resource ID (e.g. in small icon), which may change across versions.
 * It's fine here, since push notifications use remote resource ID (in target package).
 *
 * It works out of the box for normal installation performed by [PackageInstaller],
 * but requires extra parameter for ADB install (including Run/Debug Configuration of Android Studio):
 * ```
 *     --pkg com.xiaomi.xmsf
 * ```
 *
 * @author oasisfeng
 */
@RequiresApi(M) class NotificationRevival(private val context: Context, private val filter: (StatusBarNotification) -> Boolean): BroadcastReceiver() {

    fun initialize() = context.packageManager.packageInstaller.registerSessionCallback(mInstallSessionCallback)
    fun close() = context.packageManager.packageInstaller.unregisterSessionCallback(mInstallSessionCallback)

    private fun saveActiveNotifications() {
        val notifications = context.getSystemService(NotificationManager::class.java)!!.activeNotifications
        if (notifications.isEmpty()) return
        Log.i(TAG, "Save active notifications...")

        val payload = buildBackupIntent(context)
        val am = context.getSystemService(AlarmManager::class.java)!!
        var i = 0; val expireAtElapsed = SystemClock.elapsedRealtime() + if (BuildConfig.DEBUG) TIMEOUT_DEBUG else TIMEOUT
        for (sbn in notifications) try {
            if (sbn.notification.flags.and(Notification.FLAG_ONGOING_EVENT) != 0 || ! filter(sbn))
                continue    // Skip sticky and filtered notifications
            payload.putExtra(null, sbn)
            val pi = PendingIntent.getBroadcast(context, BACKUP_VERSION + i, payload, FLAG_UPDATE_CURRENT)
            am.set(AlarmManager.ELAPSED_REALTIME, expireAtElapsed, pi)
            i ++
        } catch (e: RuntimeException) {
            Log.w(TAG, "Error saving ${sbn.key}", e)
        }
        Log.i(TAG, "Total $i (of ${notifications.size} active) notifications saved.")
    }

    override fun onReceive(context: Context, intent: Intent) {
        val index = intent.getIntExtra(EXTRA_INDEX, -1)
        if (index < 0) {    // Triggered by AlarmManager, not restoreNotificationsAsync() which will add EXTRA_INDEX
            val sbn = intent.getParcelableExtra<StatusBarNotification>(null)
            Log.w(TAG, "Save is expired: ${sbn?.key}")
        } else Log.i(TAG, "Loading save $index...")
    }

    // Required for instantiation of BroadcastReceiver.
    @Suppress("unused") constructor() : this(ContextWrapper(null), { true })

    companion object {
        fun restoreNotificationsAsync(restrictedContext: Context) {
            val context = restrictedContext.applicationContext
            val retriever = buildBackupIntent(context)
            if (PendingIntent.getBroadcast(context, BACKUP_VERSION - 1, retriever, FLAG_NO_CREATE) != null)
                return Unit.also { Log.w(TAG, "Ignore incompatible backup created by old version.") }
            var i = 0
            while (true) {
                (PendingIntent.getBroadcast(context, BACKUP_VERSION + i, retriever, FLAG_NO_CREATE) ?: break)
                        .send(context, i, Intent().putExtra(EXTRA_INDEX, i), { pi, payload, index, _, _ ->
                            val sbn = payload.getParcelableExtra<StatusBarNotification>(null)
                            if (sbn != null) try {
                                Log.i(TAG, "Restoring notification $index: ${sbn.key}")
                                restoreNotification(context, sbn)
                                context.getSystemService(AlarmManager::class.java)!!.cancel(pi)
                                pi.cancel()
                            } catch (e: RuntimeException) {
                                Log.e(TAG, "Error restoring notification $index", e)
                            } else Log.e(TAG, "Missing or corrupted payload in save $index")
                        }, null)
                i ++
            }
            if (i == 0) Log.d(TAG, "Nothing to restore.")
            else Log.i(TAG, "Total $i to restore.")
        }

        private fun restoreNotification(context: Context, sbn: StatusBarNotification) {
            var n = sbn.notification
            if (SDK_INT >= O) {     // Silence the notification being restored
                val behavior = if (n.flags and FLAG_GROUP_SUMMARY != 0) GROUP_ALERT_CHILDREN else GROUP_ALERT_SUMMARY
                n = Notification.Builder.recoverBuilder(context, n).setGroupAlertBehavior(behavior).build()
            } else @Suppress("DEPRECATION") {
                n.defaults = n.defaults and Notification.DEFAULT_LIGHTS; n.sound = Uri.EMPTY; n.vibrate
            }
            context.getSystemService(NotificationManager::class.java)!!.notify(sbn.tag, sbn.id, n)
        }

        private fun buildBackupIntent(context: Context)
                = Intent(context, NotificationRevival::class.java).addFlags(FLAG_RECEIVER_FOREGROUND)
    }

    private val mInstallSessionCallback = object: PackageInstaller.SessionCallback() {

        override fun onCreated(sessionId: Int) {
            if (isSessionForOurPackage(sessionId)) saveActiveNotifications()
        }

        override fun onFinished(sessionId: Int, success: Boolean) {
            if (BuildConfig.DEBUG && ! success && isSessionForOurPackage(sessionId))    // For debugging convenience
                restoreNotificationsAsync(context)
        }

        fun isSessionForOurPackage(sessionId: Int)
                = context.packageManager.packageInstaller.getSessionInfo(sessionId)?.appPackageName == context.packageName

        override fun onProgressChanged(sessionId: Int, progress: Float) {}
        override fun onActiveChanged(sessionId: Int, active: Boolean) {}
        override fun onBadgingChanged(sessionId: Int) {}
    }
}

private const val EXTRA_INDEX = "i"
private const val TAG = "MPF.NR"