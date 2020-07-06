package com.xiaomi.push.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Handler;
import android.os.Parcel;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import com.xiaomi.xmsf.R;

import java.util.Objects;

import static android.app.Notification.GROUP_ALERT_SUMMARY;
import static android.app.NotificationManager.IMPORTANCE_LOW;

@RequiresApi(29/* Q */) public class BackgroundActivityStartEnabler {

	private static final String CHANNEL_STATUS = PushServiceMain.CHANNEL_STATUS;

	public static @Nullable PendingIntent clonePendingIntentForBackgroundActivityStart(final PendingIntent pi) {
		final Notification whitelistedN = sWhitelistedNotification;
		if (whitelistedN == null) return null;
		whitelistedN.contentIntent = pi;
		final Parcel parcel = Parcel.obtain();
		try {
			whitelistedN.writeToParcel(parcel, 0);
			parcel.setDataPosition(0);
			final Notification n = Notification.CREATOR.createFromParcel(parcel);
			final PendingIntent whitelisted = n.contentIntent;
			n.contentIntent = null;
			return whitelisted;
		} finally {
			parcel.recycle();
			whitelistedN.contentIntent = null;
		}
	}

	static void initialize(final Context context) {
		String channelId = CHANNEL_STATUS; int channelPostfix = 0;
		final NotificationManager nm = Objects.requireNonNull(context.getSystemService(NotificationManager.class));
		for (;;) {
			@Nullable NotificationChannel channel = nm.getNotificationChannel(channelId);
			if (channel == null) {
				if (CHANNEL_STATUS.equals(channelId)) continue;     // Never create original channel "status" here.
				channel = new NotificationChannel(channelId, context.getString(R.string.notification_category_alive), IMPORTANCE_LOW);
				nm.createNotificationChannel(channel);
				break;
			} else {
				if (channel.getImportance() > NotificationManager.IMPORTANCE_NONE) break;
				if (channelPostfix == 16) {
					Log.e(TAG, "Failed to obtain available notification channel.");
					return;
				}
				channelId = CHANNEL_STATUS + (++ channelPostfix);   // If channel is disabled, try another temporary channel ID.
			}
		}
		final Notification n = new Notification.Builder(context, channelId).setTimeoutAfter(5_000)  // Must be long enough for all retries.
				.setContentTitle("Initializing...").setOngoing(true)        // To avoid being cancelled before capture
				.setGroup(TAG).setGroupAlertBehavior(GROUP_ALERT_SUMMARY)   // Effectively mute this notification
				.setSmallIcon(android.R.drawable.stat_notify_sync_noanim).build();
		nm.notify(TAG, 0, n);
		scheduleCapture(nm, 5);
	}

	private static void scheduleCapture(final NotificationManager nm, final int retries) {
		new Handler().postDelayed(() -> {
			final StatusBarNotification[] ns = nm.getActiveNotifications();
			for (final StatusBarNotification n : ns)
				if (n.getId() == 0 && TAG.equals(n.getTag())) {
					sWhitelistedNotification = n.getNotification(); // Borrow mWhitelistToken from our foreground notification.
					nm.cancel(TAG, 0);
					break;
				}
			if (sWhitelistedNotification != null) {
				final String channelId = sWhitelistedNotification.getChannelId();
				if (! CHANNEL_STATUS.equals(channelId))
					nm.deleteNotificationChannel(channelId);        // Delete channel if it is temporary
			} else if (retries == 0) {
				Log.e(TAG, "Failed to capture active notification.");
				nm.cancel(TAG, 0);      // In case it's there but unable to be captured.
			} else {
				Log.i(TAG, "Wait to capture active notification.");
				scheduleCapture(nm, retries - 1);
			}
		}, 500);
	}

	private static @Nullable Notification sWhitelistedNotification;

	private static final String TAG = "MPF.BAFE";
}
