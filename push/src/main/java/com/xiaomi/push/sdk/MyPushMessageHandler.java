package com.xiaomi.push.sdk;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import com.crossbowffs.remotepreferences.RemotePreferenceAccessException;
import com.xiaomi.helper.ITopActivity;
import com.xiaomi.helper.TopActivityFactory;
import com.xiaomi.push.service.MyClientEventDispatcher;
import com.xiaomi.xmpush.thrift.PushMetaInfo;
import com.xiaomi.xmpush.thrift.XmPushActionContainer;
import com.xiaomi.xmpush.thrift.XmPushThriftSerializeUtils;

import me.pqpo.librarylog4a.Log4a;
import top.trumeet.common.utils.PreferencesUtils;

/**
 * Created by zts1993 on 2018/2/9.
 */

public class MyPushMessageHandler extends IntentService {
    private static final String TAG = "MyPushMessageHandler";

    private static final int APP_CHECK_FRONT_MAX_RETRY = 5;

    static ITopActivity iTopActivity = null;

    public MyPushMessageHandler() {
        super("my mipush message handler");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (iTopActivity == null) {
            iTopActivity = TopActivityFactory.newInstance(getAccessMode());
        }

        if (!iTopActivity.isEnabled(this)) {
            iTopActivity.guideToEnable(this);
            return;
        }

        byte[] payload = intent.getByteArrayExtra("mipush_payload");
        if (payload == null) {
            Log4a.e(TAG, "mipush_payload is null");
            return;
        }

        XmPushActionContainer container = new XmPushActionContainer();
        try {
            XmPushThriftSerializeUtils.convertByteArrayToThriftObject(container, payload);
        } catch (Throwable var3) {
            Log4a.e(TAG, var3);
            return;
        }

        PushMetaInfo metaInfo = container.getMetaInfo();
        String targetPackage = container.getPackageName();

        pullUpApp(metaInfo, targetPackage);

        Intent localIntent = new Intent("com.xiaomi.mipush.RECEIVE_MESSAGE");
        localIntent.setComponent(new ComponentName(targetPackage, "com.xiaomi.mipush.sdk.PushMessageHandler"));
        localIntent.putExtra("mipush_payload", payload);
        localIntent.putExtra("mipush_notified", true);
        localIntent.addCategory(String.valueOf(metaInfo.getNotifyId()));
        try {
            Log4a.d(TAG, "send to service " + targetPackage);
            startService(localIntent);

            int id = MyClientEventDispatcher.getNotificationId(container);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(id);
        } catch (Exception e) {
            Log4a.e(TAG, e.getLocalizedMessage(), e);
        }

    }

    private void pullUpApp(PushMetaInfo metaInfo, String targetPackage) {
        if (!iTopActivity.isAppForeground(this, targetPackage)) {
            Log4a.i(TAG, "app is not at front , let's pull up");
            PackageManager packageManager = getPackageManager();
            Intent localIntent1 = packageManager.getLaunchIntentForPackage(targetPackage);
            if (localIntent1 == null) {
                Log4a.e(TAG, "can not get default activity for " + targetPackage);
            } else {
                localIntent1.addCategory(String.valueOf(metaInfo.getNotifyId()));
                localIntent1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(localIntent1);
                Log4a.d(TAG, "start activity " + targetPackage);
            }
        } else {
            Log4a.d(TAG, "app is at foreground" + targetPackage);
        }

        //wait
        for (int i = 0; i < APP_CHECK_FRONT_MAX_RETRY; i++) {
            if (!iTopActivity.isAppForeground(this, targetPackage)) {
                try {
                    Thread.sleep(100); //TODO let's wait?
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (i == (APP_CHECK_FRONT_MAX_RETRY - 1)) {
                    Log4a.w(TAG, "pull up app timeout" + targetPackage);
                }
            } else {
                break;
            }
        }
    }

    private int getAccessMode() {
        int accessMode = 0;
        SharedPreferences prefs = PreferencesUtils.getPreferences(this);

        try {
            String mode = prefs.getString(PreferencesUtils.KeyAccessMode, "0");
            accessMode = Integer.valueOf(mode);
        } catch (RemotePreferenceAccessException e) {
            Log4a.e(TAG, e);
        }
        return accessMode;
    }

}

