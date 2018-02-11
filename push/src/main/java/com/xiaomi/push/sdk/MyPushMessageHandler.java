package com.xiaomi.push.sdk;

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
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

import com.xiaomi.helper.DetectionService;
import com.xiaomi.push.service.MyClientEventDispatcher;
import com.xiaomi.push.service.PushServiceMain;
import com.xiaomi.xmpush.thrift.PushMetaInfo;
import com.xiaomi.xmpush.thrift.XmPushActionContainer;
import com.xiaomi.xmpush.thrift.XmPushThriftSerializeUtils;
import com.xiaomi.xmsf.R;

import me.pqpo.librarylog4a.Log4a;

import static com.xiaomi.push.service.PushServiceMain.CHANNEL_STATUS;

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

        if (!DetectionService.isAccessibilitySettingsOn(getApplicationContext())) {
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
                    .setContentTitle("请开启无障碍辅助") //TODO move to xml
                    .setContentTitle("点击后开启 Xiaomi Push Service Core的权限")
                    .setContentIntent(
                            PendingIntent.getActivity(getApplicationContext(), 0,
                                    new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS), PendingIntent.FLAG_UPDATE_CURRENT))
                    .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .build();
            manager.notify(PushServiceMain.NOTIFICATION_ALIVE_ID, notification);
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
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

        if (!isAppForeground(package_name)) {
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
            if (!isAppForeground(package_name)) {
                try {
                    Thread.sleep(100); //TODO let's wait?
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                break;
            }
        }

        if (isAppForeground(package_name)) {

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

    public static boolean isAppForeground(String packageName) {
        return packageName.equals(DetectionService.getForegroundPackageName());
    }

}

