package com.xiaomi.xmsf;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.github.yuweiguocn.library.greendao.MigrationHelper;
import com.oasisfeng.condom.CondomOptions;
import com.oasisfeng.condom.CondomProcess;
import com.xiaomi.channel.commonutils.logger.LoggerInterface;
import com.xiaomi.mipush.sdk.Logger;
import com.xiaomi.xmsf.push.service.MiuiPushActivateService;

import org.greenrobot.greendao.database.Database;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.android.LogcatAppender;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import top.trumeet.mipushframework.auth.AuthActivity;
import top.trumeet.mipushframework.db.DaoMaster;
import top.trumeet.mipushframework.db.DaoSession;
import top.trumeet.mipushframework.db.EventDao;
import top.trumeet.mipushframework.db.RegisteredApplicationDao;
import top.trumeet.mipushframework.log.LogUtils;
import top.trumeet.mipushframework.push.PushController;
import top.trumeet.mipushframework.register.RegisteredApplication;

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

        configureLogbackDirectly();

        CondomOptions options = buildOptions(this, TAG_CONDOM + "_PROCESS");
        CondomProcess.installExceptDefaultProcess(this,
                options);
        LoggerInterface newLogger = new LoggerInterface() {
            org.slf4j.Logger logger = LoggerFactory.getLogger("PushCore");
            @Override
            public void setTag(String tag) {
                // ignore
            }
            @Override
            public void log(String content, Throwable t) {
                logger.debug(content, t);
            }
            @Override
            public void log(String content) {
                logger.debug(content);
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
            org.slf4j.Logger logger = LoggerFactory.getLogger("AuthActivityCallback");
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
                            logger.debug("Handle double request: " + application.getPackageName());
                            activity.finish();
                        } else {
                            logger.warn("Registering auth progress");
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
        if (daoSession != null)
            return daoSession;
        MigrationHelper.DEBUG = true;
        MySQLiteOpenHelper helper = new MySQLiteOpenHelper(this, "db",
                null);
        daoSession = new DaoMaster(helper.getWritableDatabase())
                .newSession();
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

    /**
     * 将所有日志级别都记录到文件和logcat
     * @see <a href="http://i.woblog.cn/2017/01/08/android-logback/" />
     */
    private void configureLogbackDirectly() {
        // reset the default context (which may already have been initialized)
        // since we want to reconfigure it
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        lc.reset();
        // setup FileAppender
        PatternLayoutEncoder encoder1 = new PatternLayoutEncoder();
        encoder1.setContext(lc);
        encoder1.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
        encoder1.start();
        FileAppender<ILoggingEvent> fileAppender = new FileAppender<ILoggingEvent>();
        fileAppender.setContext(lc);
        fileAppender.setFile(LogUtils.getLogFile(this));
        fileAppender.setEncoder(encoder1);
        fileAppender.start();
        // setup LogcatAppender
        PatternLayoutEncoder encoder2 = new PatternLayoutEncoder();
        encoder2.setContext(lc);
        encoder2.setPattern("[%thread] %msg%n");
        encoder2.start();
        LogcatAppender logcatAppender = new LogcatAppender();
        logcatAppender.setContext(lc);
        logcatAppender.setEncoder(encoder2);
        logcatAppender.start();
        // add the newly created appenders to the root logger;
        // qualify Logger to disambiguate from org.slf4j.Logger
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        root.addAppender(fileAppender);
        root.addAppender(logcatAppender);
    }
}
