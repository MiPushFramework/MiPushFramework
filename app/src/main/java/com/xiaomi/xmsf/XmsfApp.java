package com.xiaomi.xmsf;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;

import com.github.yuweiguocn.library.greendao.MigrationHelper;
import com.oasisfeng.condom.CondomOptions;
import com.oasisfeng.condom.CondomProcess;
import com.xiaomi.channel.commonutils.logger.LoggerInterface;
import com.xiaomi.mipush.sdk.Logger;
import com.xiaomi.xmsf.push.service.MiuiPushActivateService;

import org.greenrobot.greendao.database.Database;

import java.util.HashSet;
import java.util.Set;

import top.trumeet.mipushframework.auth.AuthActivity;
import top.trumeet.mipushframework.db.DaoMaster;
import top.trumeet.mipushframework.db.DaoSession;
import top.trumeet.mipushframework.db.EventDao;
import top.trumeet.mipushframework.db.RegisteredApplicationDao;
import top.trumeet.mipushframework.push.PushController;
import top.trumeet.mipushframework.register.RegisteredApplication;

import static top.trumeet.mipushframework.Constants.TAG;
import static top.trumeet.mipushframework.Constants.TAG_CONDOM;
import static top.trumeet.mipushframework.push.PushController.buildOptions;
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
        CondomOptions options = buildOptions(this, TAG_CONDOM + "_PROCESS");
        CondomProcess.installExceptDefaultProcess(this,
                options);
        MigrationHelper.DEBUG = true;
        MySQLiteOpenHelper helper = new MySQLiteOpenHelper(this, "db",
                null);
        daoSession = new DaoMaster(helper.getWritableDatabase())
        .newSession();
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
        Logger.setLogger(PushController.wrapContext(this)
                , newLogger);
        if (PushController.isPrefsEnable(this))
            PushController.setAllEnable(true, this);
        long currentTimeMillis = System.currentTimeMillis();
        long lastStartupTime = getLastStartupTime();
        if (isAppMainProc(this) && (currentTimeMillis - lastStartupTime > 300000 || currentTimeMillis - lastStartupTime < 0)) {
            setStartupTime(currentTimeMillis);
            MiuiPushActivateService.awakePushActivateService(PushController.wrapContext(this)
                    , "com.xiaomi.xmsf.push.SCAN");
        }
        
        // XiaomiPush sdk will register push many times.
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            private Set<String> mAuthPackages = new HashSet<>(0);
            
            @Override
            public void onActivityCreated(Activity activity, Bundle bundle) {
            }

            @Override
            public void onActivityStarted(Activity activity) {
                if (activity.getClass().getName().equals(AuthActivity.class.getName())) {
                    if (activity.getIntent().hasExtra(AuthActivity.EXTRA_REGISTERED_APPLICATION)) {
                        RegisteredApplication application = activity.getIntent()
                                .getParcelableExtra(AuthActivity.EXTRA_REGISTERED_APPLICATION);
                        if (mAuthPackages.contains(application.getPackageName())) {
                            // Double
                            Log.w(TAG, "Handle double request: " + application.getPackageName());
                            activity.finish();
                        } else {
                            Log.w(TAG, "Registering auth progress");
                            mAuthPackages.add(application.getPackageName());
                        }
                    }
                }
            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
                if (activity.getClass().getName().equals(AuthActivity.class.getName())) {
                    if (activity.getIntent().hasExtra(AuthActivity.EXTRA_REGISTERED_APPLICATION)) {
                        RegisteredApplication application = activity.getIntent()
                                .getParcelableExtra(AuthActivity.EXTRA_REGISTERED_APPLICATION);
                        mAuthPackages.remove(application.getPackageName());
                    }
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }

    public static DaoSession getDaoSession (Context context) {
        return ((XmsfApp)context.getApplicationContext())
                .getDaoSession();
    }

    public class MySQLiteOpenHelper extends DaoMaster.OpenHelper {
        public MySQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
            super(context, name, factory);
        }
        @Override
        public void onUpgrade(Database db, int oldVersion, int newVersion) {
            MigrationHelper.migrate(db, new MigrationHelper.ReCreateAllTableListener() {

                @Override
                public void onCreateAllTables(Database db, boolean ifNotExists) {
                    DaoMaster.createAllTables(db, ifNotExists);
                }

                @Override
                public void onDropAllTables(Database db, boolean ifExists) {
                    DaoMaster.dropAllTables(db, ifExists);
                }
            }, EventDao.class, RegisteredApplicationDao.class);
        }
    }
}
