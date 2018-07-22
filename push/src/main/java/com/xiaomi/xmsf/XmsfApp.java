package com.xiaomi.xmsf;

import android.Manifest;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;

import com.crashlytics.android.Crashlytics;
import com.oasisfeng.condom.CondomOptions;
import com.oasisfeng.condom.CondomProcess;
import com.taobao.android.dexposed.XC_MethodHook;
import com.xiaomi.channel.commonutils.logger.LoggerInterface;
import com.xiaomi.channel.commonutils.logger.MyLog;
import com.xiaomi.channel.commonutils.misc.ScheduledJobManager;
import com.xiaomi.mipush.sdk.Logger;
import com.xiaomi.push.service.OnlineConfig;
import com.xiaomi.xmpush.thrift.ConfigKey;
import com.xiaomi.xmsf.push.control.PushControllerUtils;
import com.xiaomi.xmsf.push.control.XMOutbound;
import com.xiaomi.xmsf.push.hooks.PushSdkHooks;
import com.xiaomi.xmsf.push.notification.NotificationController;
import com.xiaomi.xmsf.push.service.MiuiPushActivateService;
import com.xiaomi.xmsf.push.service.notificationcollection.NotificationListener;
import com.xiaomi.xmsf.push.service.notificationcollection.UploadNotificationJob;
import com.xiaomi.xmsf.utils.ConfigCenter;
import com.xiaomi.xmsf.utils.LogUtils;

import java.util.HashSet;
import java.util.Random;

import io.fabric.sdk.android.Fabric;
import me.pqpo.librarylog4a.Log4a;
import top.trumeet.mipush.provider.DatabaseUtils;

import static com.xiaomi.xmsf.push.control.PushControllerUtils.isAppMainProc;
import static top.trumeet.common.Constants.TAG_CONDOM;

public class XmsfApp extends Application {

    private static final String MIPUSH_EXTRA = "mipush_extra";

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

        if (!BuildConfig.DEBUG) {
            final Fabric fabric = new Fabric.Builder(this)
                    .kits(new Crashlytics())
                    .build();
            Fabric.with(fabric);
        }

        ConfigCenter.reloadConf(this);

        LogUtils.configureLog(this);

        initMiSdkLogger();

        mUnHooks = new PushSdkHooks().getHooks();

        CondomOptions options = XMOutbound.create(this, TAG_CONDOM + "_PROCESS",
                false);
        CondomProcess.installExceptDefaultProcess(this, options);

        initPushLogger();

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

    private void initPushLogger() {
        Logger.setLogger(PushControllerUtils.wrapContext(this), new LoggerInterface() {
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
        });
    }

    private void initMiSdkLogger() {
        MyLog.setLogger(new LoggerInterface() {
            private static final String M_TAG = "xiaomi-patched";

            @Override
            public void setTag(String tag) {
            }

            @Override
            public void log(String content) {
                Log4a.d(M_TAG, content);
            }

            @Override
            public void log(String content, Throwable t) {
                if (content.contains("isMIUI")) {
                    return;
                }
                if (t == null) {
                    Log4a.i(M_TAG, content);
                } else {
                    Log4a.e(M_TAG, content, t);
                }
            }
        });
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

        StringBuilder sb = new StringBuilder();
        for (ComponentName componentName : hashSet) {
            sb.append(componentName.flattenToString()).append(':');
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        Settings.Secure.putString(getContentResolver(), "enabled_notification_listeners", sb.toString());
    }

    private void setListenerDefaultAdded() {
        getDefaultPreferences().edit().putBoolean("notification_listener_added", true).apply();
    }

    private boolean isListenerDefaultAdded() {
        return getDefaultPreferences().getBoolean("notification_listener_added", false);
    }


    private long getLastStartupTime() {
        return getDefaultPreferences().getLong("xmsf_startup", 0);
    }

    private boolean setStartupTime(long j) {
        return getDefaultPreferences().edit().putLong("xmsf_startup", j).commit();
    }

    private SharedPreferences getDefaultPreferences() {
        return getSharedPreferences(MIPUSH_EXTRA, 0);
    }

    private void scheduleUploadNotificationInfo() {
        try {
            if (!isListenerDefaultAdded()) {
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
