package com.xiaomi.helper.impl;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

import com.xiaomi.helper.DetectionService;
import com.xiaomi.helper.ITopActivity;
import com.xiaomi.push.service.PushServiceMain;
import com.xiaomi.xmsf.R;

import me.pqpo.librarylog4a.Log4a;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.xiaomi.push.service.PushServiceMain.CHANNEL_STATUS;

/**
 * Created by zts1993 on 2018/2/18.
 */

public class ActivityAccessibilityImpl implements ITopActivity {
    final static String TAG = "ActivityAccessibilityImpl";

    @Override
    public boolean isEnabled(Context context) {
        int accessibilityEnabled = 0;
        try {
            accessibilityEnabled = Settings.Secure.getInt(context.getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            Log4a.e(TAG, e.getMessage(), e);
        }

        if (accessibilityEnabled == 1) {
            String services = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (services != null) {
                return services.toLowerCase().contains(context.getPackageName().toLowerCase());
            }
        }

        return false;
    }


    @Override
    public void guideToEnable(Context context) {

        NotificationManager manager = (NotificationManager)
                context.getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_STATUS,
                    context.getString(R.string.notification_category_alive),
                    NotificationManager.IMPORTANCE_MIN);
            manager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(context,
                CHANNEL_STATUS)
                .setContentTitle("请开启无障碍辅助") //TODO move to xml
                .setContentTitle("点击后开启 Xiaomi Push Service Core的权限")
                .setContentIntent(
                        PendingIntent.getActivity(context.getApplicationContext(), 0,
                                new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS), PendingIntent.FLAG_UPDATE_CURRENT))
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();
        manager.notify(PushServiceMain.NOTIFICATION_ALIVE_ID, notification);
        context.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
    }

    public boolean isAppForeground(Context context, String packageName) {
        return packageName.equals(DetectionService.getForegroundPackageName());
    }

}
