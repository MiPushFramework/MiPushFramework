package com.xiaomi.xmsf.push.alive;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.xiaomi.xmsf.R;

/**
 * Created by Trumeet on 2017/12/12.
 */

public class KeepAliveService extends Service {
    public static final String CHANNEL_STATUS = "status";
    private static final int NOTIFICATION_ALIVE_ID = 0;

    public @StartResult int onStartCommand(Intent intent, @StartArgFlags int flags, int startId) {
        NotificationManager manager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_STATUS,
                    getString(R.string.notification_category_alive),
                    NotificationManager.IMPORTANCE_MIN);
            manager.createNotificationChannel(channel);
        }
        Notification notification = new NotificationCompat.Builder(this,
                CHANNEL_STATUS)
                .setContentTitle(getString(R.string.notification_alive))
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setOngoing(true)
                .build();
        manager.notify(NOTIFICATION_ALIVE_ID, notification);
        startForeground(NOTIFICATION_ALIVE_ID, notification);
        return START_STICKY;
    }

    @Override
    public void onDestroy () {
        ((NotificationManager)getSystemService(NOTIFICATION_SERVICE))
    .cancel(NOTIFICATION_ALIVE_ID);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
