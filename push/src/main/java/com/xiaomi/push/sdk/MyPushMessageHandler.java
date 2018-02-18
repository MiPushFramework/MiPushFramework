package com.xiaomi.push.sdk;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.xiaomi.helper.ITopActivity;
import com.xiaomi.helper.TopActivityFactory;
import com.xiaomi.push.service.MyClientEventDispatcher;
import com.xiaomi.xmpush.thrift.PushMetaInfo;
import com.xiaomi.xmpush.thrift.XmPushActionContainer;
import com.xiaomi.xmpush.thrift.XmPushThriftSerializeUtils;

import me.pqpo.librarylog4a.Log4a;

/**
 * Created by zts1993 on 2018/2/9.
 */
public class MyPushMessageHandler extends IntentService {
    private static final String TAG = "MyPushMessageHandler";

    ITopActivity iTopActivity = null;

    public MyPushMessageHandler() {
        super("my mipush message handler");
        iTopActivity = TopActivityFactory.newInstance(TopActivityFactory.AccessMode.USAGE_STATS);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Context context = this;

        if (!iTopActivity.isEnabled(context)) {
            iTopActivity.guideToEnable(context);
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

        String package_name = container.getPackageName();

        if (!iTopActivity.isAppForeground(context, package_name)) {
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
            if (!iTopActivity.isAppForeground(context, package_name)) {
                try {
                    Thread.sleep(100); //TODO let's wait?
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                break;
            }
        }

        if (iTopActivity.isAppForeground(context, package_name)) {

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


}

