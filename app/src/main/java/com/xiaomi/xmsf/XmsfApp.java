package com.xiaomi.xmsf;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.xiaomi.channel.commonutils.logger.LoggerInterface;
import com.xiaomi.mipush.sdk.Logger;
import com.xiaomi.xmsf.push.service.MiuiPushActivateService;

import org.greenrobot.greendao.database.Database;

import top.trumeet.mipushframework.db.DaoMaster;
import top.trumeet.mipushframework.db.DaoSession;
import top.trumeet.mipushframework.push.PushController;

import static top.trumeet.mipushframework.Constants.TAG;
import static top.trumeet.mipushframework.push.PushController.isAppMainProc;

public class XmsfApp extends Application {
    private DaoSession daoSession;

    private long getLastStartupTime() {
        return getSharedPreferences("mipush_extra", 0).getLong("xmsf_startup", 0);
    }

    private boolean setStartupTime(long j) {
        return getSharedPreferences("mipush_extra", 0).edit().putLong("xmsf_startup", j).commit();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "db");
        Database db = helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();
        LoggerInterface newLogger = new LoggerInterface() {
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

    public DaoSession getDaoSession() {
        return daoSession;
    }

    public static DaoSession getDaoSession (Context context) {
        return ((XmsfApp)context.getApplicationContext())
                .getDaoSession();
    }
}
