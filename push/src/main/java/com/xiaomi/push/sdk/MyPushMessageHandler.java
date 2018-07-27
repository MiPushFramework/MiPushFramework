package com.xiaomi.push.sdk;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Intent;

import top.trumeet.common.ita.ITopActivity;
import top.trumeet.common.ita.TopActivityFactory;
import com.xiaomi.push.service.MIPushNotificationHelper;
import com.xiaomi.push.service.MyClientEventDispatcher;
import com.xiaomi.push.service.MyMIPushNotificationHelper;
import com.xiaomi.push.service.PushConstants;
import com.xiaomi.xmpush.thrift.PushMetaInfo;
import com.xiaomi.xmpush.thrift.XmPushActionContainer;
import com.xiaomi.xmpush.thrift.XmPushThriftSerializeUtils;
import com.xiaomi.xmsf.push.notification.NotificationController;
import com.xiaomi.xmsf.utils.ConfigCenter;

import me.pqpo.librarylog4a.Log4a;

/**
 * @author zts1993
 * @date 2018/2/9
 */

public class MyPushMessageHandler extends IntentService {
    private static final String TAG = "MyPushMessageHandler";

    private static final int APP_CHECK_FRONT_MAX_RETRY = 6;
    private static final int APP_CHECK_SLEEP_DURATION_MS = 300;
    private static final int APP_CHECK_SLEEP_MAX_TIMEOUT_MS = APP_CHECK_FRONT_MAX_RETRY * APP_CHECK_SLEEP_DURATION_MS;

    static ITopActivity iTopActivity = null;

    public MyPushMessageHandler() {
        super("my mipush message handler");
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        if (iTopActivity == null) {
            iTopActivity = TopActivityFactory.newInstance(ConfigCenter.getInstance().accessMode);
        }

        if (!iTopActivity.isEnabled(this)) {
            iTopActivity.guideToEnable(this);
            return;
        }

        byte[] payload = intent.getByteArrayExtra(PushConstants.MIPUSH_EXTRA_PAYLOAD);
        if (payload == null) {
            Log4a.e(TAG, "mipush_payload is null");
            return;
        }

        final XmPushActionContainer container = new XmPushActionContainer();
        try {
            XmPushThriftSerializeUtils.convertByteArrayToThriftObject(container, payload);
        } catch (Throwable var3) {
            Log4a.e(TAG, var3);
            return;
        }

        PushMetaInfo metaInfo = container.getMetaInfo();
        String targetPackage = container.getPackageName();


        pullUpApp(targetPackage, container);

        final Intent localIntent = new Intent(PushConstants.MIPUSH_ACTION_NEW_MESSAGE);
        localIntent.setComponent(new ComponentName(targetPackage, "com.xiaomi.mipush.sdk.PushMessageHandler"));
        localIntent.putExtra(PushConstants.MIPUSH_EXTRA_PAYLOAD, payload);
        localIntent.putExtra(MIPushNotificationHelper.FROM_NOTIFICATION, true);
        localIntent.addCategory(String.valueOf(metaInfo.getNotifyId()));
        try {
            Log4a.d(TAG, "send to service " + targetPackage);

            startService(localIntent);

            int id = MyClientEventDispatcher.getNotificationId(container);
            NotificationController.cancel(this, id);

        } catch (Exception e) {
            Log4a.e(TAG, e.getLocalizedMessage(), e);
        }

    }


    private Intent getJumpIntent(String targetPackage, XmPushActionContainer container) {
        Intent intent = MyMIPushNotificationHelper.getSdkIntent(this, targetPackage, container);
        if (intent == null) {
            try {
                intent = getPackageManager().getLaunchIntentForPackage(targetPackage);
            } catch (RuntimeException ignore) {
            }

        }
        return intent;
    }

    private long pullUpApp(String targetPackage, XmPushActionContainer container) {
        long start = System.currentTimeMillis();

        try {


            if (!iTopActivity.isAppForeground(this, targetPackage)) {
                Log4a.d(TAG, "app is not at front , let's pull up");

                Intent intent = getJumpIntent(targetPackage, container);

                if (intent == null) {
                    throw new RuntimeException("can not get default activity for " + targetPackage);
                } else {
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    startActivity(intent);
                    Log4a.d(TAG, "start activity " + targetPackage);
                }


                //wait
                for (int i = 0; i < APP_CHECK_FRONT_MAX_RETRY; i++) {

                    if (!iTopActivity.isAppForeground(this, targetPackage)) {
                        Thread.sleep(APP_CHECK_SLEEP_DURATION_MS);
                    } else {
                        break;
                    }

                }

                if ((System.currentTimeMillis() - start) >= APP_CHECK_SLEEP_MAX_TIMEOUT_MS) {
                    Log4a.w(TAG, "pull up app timeout" + targetPackage);
                }

            } else {
                Log4a.d(TAG, "app is at foreground" + targetPackage);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            Log4a.e(TAG, "pullUpApp failed " + e.getLocalizedMessage(), e);
        }


        long end = System.currentTimeMillis();
        return end - start;

    }

}

