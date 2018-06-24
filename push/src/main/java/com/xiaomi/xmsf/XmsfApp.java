package com.xiaomi.xmsf;

import android.Manifest;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;

import com.oasisfeng.condom.CondomOptions;
import com.oasisfeng.condom.CondomProcess;
import com.taobao.android.dexposed.XC_MethodHook;
import com.xiaomi.channel.commonutils.logger.LoggerInterface;
import com.xiaomi.channel.commonutils.logger.MyLog;
import com.xiaomi.channel.commonutils.misc.ScheduledJobManager;
import com.xiaomi.mipush.sdk.Logger;
import com.xiaomi.push.service.OnlineConfig;
import com.xiaomi.xmpush.thrift.ConfigKey;
import com.xiaomi.xmsf.crash.CrashHandler;
import com.xiaomi.xmsf.push.control.PushControllerUtils;
import com.xiaomi.xmsf.push.control.XMOutbound;
import com.xiaomi.xmsf.push.hooks.PushSdkHooks;
import com.xiaomi.xmsf.push.notification.NotificationController;
import com.xiaomi.xmsf.push.service.MiuiPushActivateService;
import com.xiaomi.xmsf.push.service.notificationcollection.NotificationListener;
import com.xiaomi.xmsf.push.service.notificationcollection.UploadNotificationJob;
import com.xiaomi.xmsf.push.utils.RemoveTremblingUtils;
import com.xiaomi.xmsf.utils.ConfigCenter;
import com.xiaomi.xmsf.utils.LogUtils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import me.pqpo.librarylog4a.Log4a;
import top.trumeet.mipush.provider.DatabaseUtils;

import static com.xiaomi.xmsf.push.control.PushControllerUtils.isAppMainProc;
import static top.trumeet.common.Constants.TAG_CONDOM;

public class XmsfApp extends Application {

    private RemoveTremblingUtils mRemoveTrembling;

    private long getLastStartupTime() {
        return getSharedPreferences("mipush_extra", 0).getLong("xmsf_startup", 0);
    }

    private boolean setStartupTime(long j) {
        return getSharedPreferences("mipush_extra", 0).edit().putLong("xmsf_startup", j).commit();
    }

    private XC_MethodHook.Unhook[] mUnHooks;

    @Override
    public void onTerminate() {
        if (mUnHooks != null) {
            for (XC_MethodHook.Unhook unhook : mUnHooks) {
                unhook.unhook();
            }
        }
        Log4a.flush();
        super.onTerminate();
    }

    @Override
    public void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        DatabaseUtils.init(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        ConfigCenter.reloadConf(this, true);

        LogUtils.configureLog(this);

        MyLog.setLogger(new LoggerInterface() {
            private String mTag = "xiaomi-patched";

            @Override
            public void setTag(String tag) {
                this.mTag = tag;
            }

            @Override
            public void log(String content) {
                Log4a.d(this.mTag, content);
            }

            @Override
            public void log(String content, Throwable t) {
                if (content.contains("isMIUI")) {
                    return;
                }
                if (t == null) {
                    Log4a.i(mTag, content);
                } else {
                    Log4a.e(mTag, content, t);
                }
            }
        });

        Thread.currentThread().setUncaughtExceptionHandler(
                new CrashHandler(Thread.currentThread().getUncaughtExceptionHandler())
        );

        mUnHooks = new PushSdkHooks().getHooks();

        CondomOptions options = XMOutbound.create(this, TAG_CONDOM + "_PROCESS",
                false);
        CondomProcess.installExceptDefaultProcess(this, options);
        LoggerInterface newLogger = new LoggerInterface() {
            private static final String TAG = "PushCore";

            @Override
            public void setTag(String tag) {
                // ignore
            }

            @Override
            public void log(String content, Throwable t) {
                if (t == null) {
                    Log4a.i(TAG, content);
                } else {
                    Log4a.e(TAG, content, t);
                }
            }

            @Override
            public void log(String content) {
                Log4a.d(TAG, content);
            }
        };
        Logger.setLogger(PushControllerUtils.wrapContext(this), newLogger);
        if (PushControllerUtils.isPrefsEnable(this)) {
            PushControllerUtils.setAllEnable(true, this);
        }
        scheduleUploadNotificationInfo();
        long currentTimeMillis = System.currentTimeMillis();
        long lastStartupTime = getLastStartupTime();
        if (isAppMainProc(this)) {
            if ((currentTimeMillis - lastStartupTime > 300000 || currentTimeMillis - lastStartupTime < 0)) {
                setStartupTime(currentTimeMillis);
                MiuiPushActivateService.awakePushActivateService(PushControllerUtils.wrapContext(this)
                        , "com.xiaomi.xmsf.push.SCAN");
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationController.deleteOldNotificationChannelGroup(this);
        }
    }

    public RemoveTremblingUtils getRemoveTremblingInstance() {
        if (mRemoveTrembling != null) {
            return mRemoveTrembling;
        }
        mRemoveTrembling = new RemoveTremblingUtils();
        return mRemoveTrembling;
    }

    public static XmsfApp getSession(Context context) {
        return ((XmsfApp) context.getApplicationContext());
    }

    private HashSet<ComponentName> loadEnabledServices() {
        HashSet<ComponentName> hashSet = new HashSet<>();
        String string = Settings.Secure.getString(getContentResolver()
                , "enabled_notification_listeners");
        if (!(string == null || "".equals(string))) {
            String[] split = string.split(":");
            for (String unflattenFromString : split) {
                ComponentName unflattenFromString2 = ComponentName.unflattenFromString(unflattenFromString);
                if (unflattenFromString2 != null) {
                    hashSet.add(unflattenFromString2);
                }
            }
        }
        return hashSet;
    }

    private void saveEnabledServices(HashSet<ComponentName> hashSet) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_SECURE_SETTINGS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        StringBuilder stringBuilder = null;
        Iterator it = hashSet.iterator();
        while (it.hasNext()) {
            ComponentName componentName = (ComponentName) it.next();
            if (stringBuilder == null) {
                stringBuilder = new StringBuilder();
            } else {
                stringBuilder.append(':');
            }
            stringBuilder.append(componentName.flattenToString());
        }
        Settings.Secure.putString(getContentResolver(), "enabled_notification_listeners", stringBuilder != null ? stringBuilder.toString() : "");
    }

    private void setListenerDefaultAdded() {
        getSharedPreferences("mipush_extra", 0).edit().putBoolean("notification_listener_added", true).apply();
    }

    private boolean isListenerDefaultAdded() {
        return getSharedPreferences("mipush_extra", 0).getBoolean("notification_listener_added", false);
    }

    private void scheduleUploadNotificationInfo() {
        try {
            if (!isListenerDefaultAdded() && Build.VERSION.SDK_INT >= 19) {
                HashSet<ComponentName> loadEnabledServices = loadEnabledServices();
                loadEnabledServices.add(new ComponentName(this, NotificationListener.class));
                saveEnabledServices(loadEnabledServices);
                setListenerDefaultAdded();
            }
            int intValue = OnlineConfig.getInstance(this).getIntValue(ConfigKey.UploadNotificationInfoFrequency.getValue(), 120) * 60;
            int nextInt = (new Random().nextInt(intValue) + intValue) / 2;
            ScheduledJobManager.getInstance(this).addRepeatJob(new UploadNotificationJob(this)
                    , intValue, nextInt);
        } catch (Throwable th) {
            MyLog.e(th);
        }
    }
}
