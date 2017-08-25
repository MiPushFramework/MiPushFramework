package com.xiaomi.xmsf;

import android.util.Log;

import com.xiaomi.channel.commonutils.logger.LoggerInterface;
import com.xiaomi.mipush.sdk.Logger;
import com.xiaomi.xmsf.push.service.MiuiPushActivateService;

import miui.external.ApplicationDelegate;
import top.trumeet.mipushframework.push.PushController;

import static top.trumeet.mipushframework.push.PushController.isAppMainProc;

public class XmsfApp extends ApplicationDelegate {
    private long getLastStartupTime() {
        return getSharedPreferences("mipush_extra", 0).getLong("xmsf_startup", 0);
    }

    private boolean setStartupTime(long j) {
        return getSharedPreferences("mipush_extra", 0).edit().putLong("xmsf_startup", j).commit();
    }

    public void onCreate() {
        super.onCreate();
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
        if (PushController.isPrefsEnable(this))
            PushController.setServiceEnable(true, this);
        long currentTimeMillis = System.currentTimeMillis();
        long lastStartupTime = getLastStartupTime();
        if (isAppMainProc(this) && (currentTimeMillis - lastStartupTime > 300000 || currentTimeMillis - lastStartupTime < 0)) {
            setStartupTime(currentTimeMillis);
            MiuiPushActivateService.awakePushActivateService(this, "com.xiaomi.xmsf.push.SCAN");
        }
    }
}
