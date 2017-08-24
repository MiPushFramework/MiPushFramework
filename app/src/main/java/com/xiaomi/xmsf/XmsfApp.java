package com.xiaomi.xmsf;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.provider.Settings.Secure;
import android.util.Log;

import com.xiaomi.channel.commonutils.logger.LoggerInterface;
import com.xiaomi.mipush.sdk.Logger;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.xiaomi.xmsf.push.service.MiuiPushActivateService;
import java.util.HashSet;
import java.util.Iterator;

import miui.external.ApplicationDelegate;

public class XmsfApp extends ApplicationDelegate {
    private static String APP_ID = "1000271";
    private static String APP_KEY = "420100086271";

    private long getLastStartupTime() {
        return getSharedPreferences("mipush_extra", 0).getLong("xmsf_startup", 0);
    }

    private boolean isAppMainProc() {
        for (RunningAppProcessInfo runningAppProcessInfo : ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE))
                .getRunningAppProcesses()) {
            if (runningAppProcessInfo.pid == Process.myPid() && runningAppProcessInfo.processName.equals(getPackageName())) {
                return true;
            }
        }
        return false;
    }

    private boolean setStartupTime(long j) {
        return getSharedPreferences("mipush_extra", 0).edit().putLong("xmsf_startup", j).commit();
    }

    public void onCreate() {
        super.onCreate();
        if (isAppMainProc()) {
            MiPushClient.registerPush(this, APP_ID, APP_KEY);
            LoggerInterface newLogger = new LoggerInterface() {
                private static final String TAG = "XMPush";

                @Override
                public void setTag(String tag) {
                    // ignore
                }
                @Override
                public void log(String content, Throwable t) {
                    Log.d(TAG, content, t);
                }
                @Override
                public void log(String content) {
                    Log.d(TAG, content);
                }
            };
            Logger.setLogger(this, newLogger);
        }
        long currentTimeMillis = System.currentTimeMillis();
        long lastStartupTime = getLastStartupTime();
        if (isAppMainProc() && (currentTimeMillis - lastStartupTime > 300000 || currentTimeMillis - lastStartupTime < 0)) {
            setStartupTime(currentTimeMillis);
            MiuiPushActivateService.awakePushActivateService(this, "com.xiaomi.xmsf.push.SCAN");
        }
    }
}
