package com.xiaomi.push.sdk;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Consumer;

import com.catchingnow.icebox.sdk_client.IceBox;
import com.elvishew.xlog.Logger;
import com.elvishew.xlog.XLog;
import com.xiaomi.push.service.BackgroundActivityStartEnabler;
import com.xiaomi.push.service.MIPushNotificationHelper;
import com.xiaomi.push.service.MyClientEventDispatcher;
import com.xiaomi.push.service.MyMIPushNotificationHelper;
import com.xiaomi.push.service.PushConstants;
import com.xiaomi.xmpush.thrift.PushMetaInfo;
import com.xiaomi.xmpush.thrift.XmPushActionContainer;
import com.xiaomi.xmpush.thrift.XmPushThriftSerializeUtils;
import com.xiaomi.xmsf.push.notification.NotificationController;
import com.xiaomi.xmsf.utils.ConfigCenter;

import top.trumeet.common.ita.ITopActivity;
import top.trumeet.common.ita.TopActivityFactory;
import top.trumeet.common.utils.Utils;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.P;

/**
 * @author zts1993
 * @date 2018/2/9
 */

public class MyPushMessageHandler extends IntentService {
    private Logger logger = XLog.tag("MyPushMessageHandler").build();

    private static final int APP_CHECK_FRONT_MAX_RETRY = 8;
    private static final int APP_CHECK_SLEEP_DURATION_MS = 500;
    private static final int APP_CHECK_SLEEP_MAX_TIMEOUT_MS = APP_CHECK_FRONT_MAX_RETRY * APP_CHECK_SLEEP_DURATION_MS;

    static ITopActivity iTopActivity = null;

    public MyPushMessageHandler() {
        super("my mipush message handler");
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        if (iTopActivity == null) {
            iTopActivity = TopActivityFactory.newInstance(ConfigCenter.getInstance().getAccessMode(this));
        }

        if (!iTopActivity.isEnabled(this)) {
            iTopActivity.guideToEnable(this);
            return;
        }

        byte[] payload = intent.getByteArrayExtra(PushConstants.MIPUSH_EXTRA_PAYLOAD);
        if (payload == null) {
            logger.e("mipush_payload is null");
            return;
        }

        final XmPushActionContainer container = new XmPushActionContainer();
        try {
            XmPushThriftSerializeUtils.convertByteArrayToThriftObject(container, payload);
        } catch (Throwable var3) {
            logger.e(var3);
            return;
        }

        PushMetaInfo metaInfo = container.getMetaInfo();
        String targetPackage = container.getPackageName();

        activeApp(targetPackage);

        runWithAppStateElevatedToForeground(targetPackage, elevated -> {
            final Intent localIntent = MyMIPushNotificationHelper.buildTargetIntentWithoutExtras(targetPackage, metaInfo);
            PendingIntent pi = PendingIntent.getService(this, 0, localIntent, PendingIntent.FLAG_NO_CREATE);
            if (pi == null) {
                logger.e("Error retrieving target intent for "+ targetPackage);
                return;
            }
            final PendingIntent clone;
            if (elevated && SDK_INT > P
                    && (clone = BackgroundActivityStartEnabler.clonePendingIntentForBackgroundActivityStart(pi)) != null) {
                pi = clone;
            } else pullUpApp(targetPackage, container);

            try {
                logger.d("send to service " + targetPackage);
                final Intent extras = new Intent().putExtra(PushConstants.MIPUSH_EXTRA_PAYLOAD, payload)
                        .putExtra(MIPushNotificationHelper.FROM_NOTIFICATION, true);
                pi.send(this, 0, extras);

                final int id = MyClientEventDispatcher.getNotificationId(container);
                NotificationController.cancel(this, id);
            } catch (Exception e) {
                logger.e(e.getLocalizedMessage(), e);
            }
        });
    }

    private void activeApp(String targetPackage) {
        try {
            if (!ConfigCenter.getInstance().isIceboxSupported(this)) {
                return;
            }

            if (!Utils.isAppInstalled(IceBox.PACKAGE_NAME)) {
                return;
            }

            if (ContextCompat.checkSelfPermission(this, IceBox.SDK_PERMISSION) == PackageManager.PERMISSION_GRANTED) {

                int enabledSetting = IceBox.getAppEnabledSetting(this, targetPackage);
                if (enabledSetting != 0) {
                    logger.w("active app " + targetPackage + " by IceBox SDK");
                    IceBox.setAppEnabledSettings(this, true, targetPackage);
                }

            } else {
                logger.w("skip active app " + targetPackage + " by IceBox SDK due to lack of permissions");
            }
        } catch (Throwable e) {
            logger.e("activeApp failed " + e.getLocalizedMessage(), e);
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

    private void runWithAppStateElevatedToForeground(final String pkg, final Consumer<Boolean> task) {
        if (SDK_INT < P) {      // onNullBinding() was introduced in Android P.
            task.accept(Boolean.FALSE);
            return;
        }
        final Intent intent = new Intent().setClassName(pkg, MyMIPushNotificationHelper.CLASS_NAME_PUSH_MESSAGE_HANDLER);
        final Context appContext = getApplicationContext();
        final boolean successful = appContext.bindService(intent, new ServiceConnection() {

            private void runTaskAndUnbind() {
                task.accept(Boolean.TRUE);
                appContext.unbindService(this);
            }

            @RequiresApi(P) @Override public void onNullBinding(final ComponentName name) {
                runTaskAndUnbind();
            }

            @Override public void onServiceConnected(final ComponentName name, final IBinder service) {
                runTaskAndUnbind();     // Should not happen
            }

            @Override public void onServiceDisconnected(final ComponentName name) {}
        }, BIND_AUTO_CREATE | BIND_IMPORTANT | BIND_ABOVE_CLIENT);

        if (! successful) task.accept(Boolean.FALSE);
    }

    private long pullUpApp(String targetPackage, XmPushActionContainer container) {
        long start = System.currentTimeMillis();

        try {


            if (!iTopActivity.isAppForeground(this, targetPackage)) {
                logger.d("app is not at front , let's pull up");

                Intent intent = getJumpIntent(targetPackage, container);

                if (intent == null) {
                    throw new RuntimeException("can not get default activity for " + targetPackage);
                } else {
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    startActivity(intent);
                    logger.d("start activity " + targetPackage);
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
                    logger.w("pull up app timeout" + targetPackage);
                }

            } else {
                logger.d("app is at foreground" + targetPackage);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            logger.e("pullUpApp failed " + e.getLocalizedMessage(), e);
        }


        long end = System.currentTimeMillis();
        return end - start;

    }

}

