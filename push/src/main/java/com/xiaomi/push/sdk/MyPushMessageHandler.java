package com.xiaomi.push.sdk;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Process;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

import com.xiaomi.push.service.MyClientEventDispatcher;
import com.xiaomi.push.service.PushServiceMain;
import com.xiaomi.xmpush.thrift.PushMetaInfo;
import com.xiaomi.xmpush.thrift.XmPushActionContainer;
import com.xiaomi.xmpush.thrift.XmPushThriftSerializeUtils;
import com.xiaomi.xmsf.R;

import me.pqpo.librarylog4a.Log4a;
import top.trumeet.common.override.ActivityManagerOverride;
import top.trumeet.common.override.AppOpsManagerOverride;

import static com.xiaomi.push.service.PushServiceMain.CHANNEL_WARNING;

/**
 * Created by zts1993 on 2018/2/9.
 */

public class MyPushMessageHandler extends IntentService {
    private static final String TAG = "MyPushMessageHandler";

    public MyPushMessageHandler() {
        super("my mipush message handler");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int opResult = AppOpsManagerOverride.checkOpNoThrow(AppOpsManagerOverride.OP_GET_USAGE_STATS, Process.myUid(),
                getPackageName(), (AppOpsManager) getSystemService(APP_OPS_SERVICE));
        if (opResult == AppOpsManager.MODE_IGNORED) {
            guideToSetStatsPermission();
            return;
        }

        byte[] mipush_payloads = intent.getByteArrayExtra("mipush_payload");
        if (mipush_payloads == null) {
            Log4a.e(TAG, "mipush_payload is null");
            return;
        }

        XmPushActionContainer container = new XmPushActionContainer();

        try {
            XmPushThriftSerializeUtils.convertByteArrayToThriftObject(container, mipush_payloads);
        } catch (Throwable var3) {
            Log4a.e(TAG, var3);
            return;
        }

        PushMetaInfo metaInfo = container.getMetaInfo();

        Context context = this;
        String package_name = container.getPackageName();

        if (!isAppForeground(package_name, this)) {
            Log4a.i(TAG, "app is not at front , let's pull up");
            PackageManager packageManager = context.getPackageManager();
            Intent localIntent1 = packageManager.getLaunchIntentForPackage(package_name);
            if (localIntent1 == null) {
                Log4a.e(TAG, "can not get default activity for " + package_name);
                return;
            }

            localIntent1.addCategory(String.valueOf(metaInfo.getNotifyId()));
            localIntent1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); //TODO not sure about the flags here
            startActivity(localIntent1);
            Log4a.d(TAG, "start activity " + package_name);
        } else {
            Log4a.d(TAG, "app is at foreground");
        }

        for (int i = 0; i < 5; i++) {
            if (!isAppForeground(package_name, this)) {
                try {
                    Thread.sleep(100); //TODO let's wait?
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                break;
            }
        }

        if (isAppForeground(package_name, this)) {
            Intent localIntent = new Intent("com.xiaomi.mipush.RECEIVE_MESSAGE");
            localIntent.setComponent(new ComponentName(package_name, "com.xiaomi.mipush.sdk.PushMessageHandler"));
            localIntent.putExtra("mipush_payload", mipush_payloads);
            localIntent.putExtra("mipush_notified", true);
            localIntent.addCategory(String.valueOf(metaInfo.getNotifyId()));
            try {
                Log4a.d(TAG, "send to service " + package_name);
                startService(localIntent);

                int id = MyClientEventDispatcher.getNotificationId(container);
                ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(id);

            } catch (Exception e) {
                Log4a.e(TAG, e.getLocalizedMessage(), e);
            }
        } else {
            Log4a.w(TAG, "pull up app timeout" + package_name);
        }

    }

    private void guideToSetStatsPermission () {
        NotificationManager manager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_WARNING,
                    getString(R.string.notification_category_warning),
                    NotificationManager.IMPORTANCE_MAX);
            manager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(this,
                CHANNEL_WARNING)
                .setContentTitle(getString(R.string.notification_stats_permission_title))
                .setContentText(getString(R.string.notification_stats_permission_text))
                .setContentIntent(
                        PendingIntent.getActivity(getApplicationContext(), 0,
                                new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), PendingIntent.FLAG_UPDATE_CURRENT))
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();
        manager.notify(PushServiceMain.NOTIFICATION_ALIVE_ID, notification);
    }

    public static boolean isAppForeground(String packageName, Context context) {
        int level = ActivityManagerOverride.getPackageImportance(packageName,
                ((ActivityManager) context.getSystemService(ACTIVITY_SERVICE)));
        Log4a.d(TAG, "Importance flag: " + level);
        return level == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
    }

}

