package com.xiaomi.push.sdk;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

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

    public MyPushMessageHandler() {
        super("my mipush message handler");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
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

        Intent localIntent = new Intent("com.xiaomi.mipush.RECEIVE_MESSAGE");
        localIntent.setComponent(new ComponentName(package_name, "com.xiaomi.mipush.sdk.PushMessageHandler"));
        localIntent.putExtra("mipush_payload", mipush_payloads);
        localIntent.putExtra("mipush_notified", true);
        localIntent.addCategory(String.valueOf(metaInfo.getNotifyId()));

        for (int i = 0; i < 5; i++) {
            try {
                Log4a.d(TAG, "send to service " + package_name);
                ComponentName componentName = startService(localIntent);
                if (componentName == null) {
                    //TODO the service does not exist
                    if (!pullUpAppAndWait(metaInfo, package_name, 100)) {
                        break;
                    }

                } else {
                    int id = MyClientEventDispatcher.getNotificationId(container);
                    ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(id);
                    break;
                }

            } catch (SecurityException e) {
                //the caller does not have permission to access the service or the service can not be found.
                Log4a.e(TAG, e.getLocalizedMessage(), e);
                //TODO warning to user
                pullUpAppAndWait(metaInfo, package_name, 100);
                break;

            } catch (IllegalStateException e) {
                //the application is in a state where the service can not be started (such as not in the foreground in a state when services are allowed).
                if (!pullUpAppAndWait(metaInfo, package_name, 100)) {
                    break;
                }


            } catch (Exception e) {
                Log4a.e(TAG, e.getLocalizedMessage(), e);
                break;
            }

        }

    }

    private boolean pullUpAppAndWait(PushMetaInfo metaInfo, String package_name, long millis) {
        Log4a.i(TAG, "app is not at front , let's pull up");
        PackageManager packageManager = getPackageManager();
        Intent localIntent1 = packageManager.getLaunchIntentForPackage(package_name);
        if (localIntent1 == null) {
            Log4a.e(TAG, "can not get default activity for " + package_name);
            return false;
        }

        localIntent1.addCategory(String.valueOf(metaInfo.getNotifyId()));
        localIntent1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); //TODO not sure about the flags here
        startActivity(localIntent1);
        Log4a.d(TAG, "start activity " + package_name);

        if (millis > 0) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e1) {
                //ignore
            }
        }
        return true;
    }

}

