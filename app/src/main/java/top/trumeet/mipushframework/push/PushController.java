package top.trumeet.mipushframework.push;

import android.app.ActivityManager;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Process;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.oasisfeng.condom.CondomContext;
import com.oasisfeng.condom.CondomOptions;
import com.oasisfeng.condom.OutboundJudge;
import com.oasisfeng.condom.OutboundType;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.xiaomi.push.service.XMPushService;
import com.xiaomi.xmsf.BuildConfig;
import com.xiaomi.xmsf.push.service.receivers.BootReceiver;

import java.util.List;

import top.trumeet.mipushframework.Constants;
import top.trumeet.mipushframework.event.Event;
import top.trumeet.mipushframework.event.EventDB;
import top.trumeet.mipushframework.register.RegisterDB;
import top.trumeet.mipushframework.register.RegisteredApplication;

import static android.content.Context.ACTIVITY_SERVICE;
import static top.trumeet.mipushframework.Constants.APP_ID;
import static top.trumeet.mipushframework.Constants.APP_KEY;
import static top.trumeet.mipushframework.Constants.TAG;
import static top.trumeet.mipushframework.Constants.TAG_CONDOM;

/**
 * Created by Trumeet on 2017/8/25.
 * @author Trumeet
 */

public class PushController {
    private static SharedPreferences getPrefs (Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }

    /**
     * Get is user enable push in settings.
     * @see Constants#KEY_ENABLE_PUSH
     * @param context Context param
     * @return is enable
     */
    public static boolean isPrefsEnable (Context context) {
        return getPrefs(context)
                .getBoolean(Constants.KEY_ENABLE_PUSH, true);
    }

    /**
     * Set push enable
     * @see Constants#KEY_ENABLE_PUSH
     * @param value is enable
     * @param context Context param
     */
    public static void setPrefsEnable (boolean value, Context context) {
        getPrefs(context)
                .edit()
                .putBoolean(Constants.KEY_ENABLE_PUSH, value)
                .apply();
    }

    /**
     * Check is in main app process
     * @param context Context param
     * @return is in main process
     */
    public static boolean isAppMainProc(Context context) {
        for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : ((ActivityManager)
                context.getSystemService(Context.ACTIVITY_SERVICE))
                .getRunningAppProcesses()) {
            if (runningAppProcessInfo.pid == Process.myPid() && runningAppProcessInfo.processName.equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Set XMPush sdk enable
     * @param enable enable
     * @param context context param
     */
    public static void setServiceEnable (boolean enable, Context context) {
        if (enable && isAppMainProc(context)) {
            MiPushClient.registerPush(wrapContext(context), APP_ID, APP_KEY);
        } else {
            MiPushClient.unregisterPush(wrapContext(context));
            // Force stop and disable services.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
                scheduler.cancelAll();
            }
            context.stopService(new Intent(context, XMPushService.class));
        }
    }

    /**
     * Set SP and XMPush enable
     * @param enable is enable
     * @param context Context param
     */
    public static void setAllEnable (boolean enable, Context context) {
        setPrefsEnable(enable, context);
        setServiceEnable(enable, context);
        setBootReceiverEnable(enable, context);
    }

    /**
     * Check SP and service is enable
     * @param context Context param
     * @return is all enable
     */
    public static boolean isAllEnable (Context context) {
        return isPrefsEnable(context) && isServiceRunning (context);
    }

    /**
     * Check push service is running
     * @see com.xiaomi.push.service.XMPushService
     * @param context context param
     * @return is running
     */
    public static boolean isServiceRunning (Context context) {
        ActivityManager activityManager = (ActivityManager)
                context.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> list =
                activityManager.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo info :
                list) {
            String pkg = info.service.getPackageName();
            String clz = info.service.getClassName();
            //Log.d(TAG, "process -> " + info.process);
            //Log.d(TAG, "package -> " + pkg);
            //Log.d(TAG, "className -> " + clz);
            //Log.d(TAG, "started -> "
            //        + info.started);
            if (context.getPackageName().equals(pkg)) {
                if (!pkg.equals(context.getPackageName()) ||
                        !clz.equals(XMPushService.class.getName()) ||
                        !info.started) {
                    Log.e(TAG, "SERVICE NOT RUNNING! PLZ CHECK YOUR ROM OR REPORT AN ISSUE!");
                    return false;
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    private static void setBootReceiverEnable (boolean enable, Context context) {
        context.getPackageManager()
                .setComponentEnabledSetting(new ComponentName(context,
                                BootReceiver.class),
                        enable ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);
    }

    public static Context wrapContext (final Context context) {
        return CondomContext.wrap(context, TAG_CONDOM, buildOptions(context, TAG_CONDOM));
    }

    private static CondomOptions sOptions;

    public static CondomOptions buildOptions (final Context context, final String TAG) {
        if (sOptions != null)
            return sOptions;
        sOptions = new CondomOptions()
                .setOutboundJudge(new OutboundJudge() {
                    @Override
                    public boolean shouldAllow(@NonNull OutboundType type, @Nullable Intent intent, @NonNull String target_package) {
                        Log.d(TAG, "shouldAllow ->" + type.toString());
                        if (type == OutboundType.START_SERVICE ||
                                type == OutboundType.BIND_SERVICE) {
                            Log.i(TAG, "Allowed start or bind service: " + intent);
                            return true;
                        }
                        if (type == OutboundType.BROADCAST) {
                            if (intent == null) {
                                Log.e(TAG, "Not allowed broadcast with null intent: " + target_package);
                                return false;
                            }

                            if (intent.getAction().equals(Constants.ACTION_MESSAGE_ARRIVED) ||
                                    intent.getAction().equals(Constants.ACTION_ERROR) ||
                                    intent.getAction().equals(Constants.ACTION_RECEIVE_MESSAGE)) {
                                Log.d(TAG, "Handle message broadcast: " + intent + ", " +
                                        target_package);
                                RegisteredApplication application = RegisterDB.registerApplication(target_package,
                                        false, context);
                                if (application == null) {
                                    Log.w(TAG, "Not registered application: " + target_package);
                                    return true;
                                }
                                if (BuildConfig.DEBUG) {
                                    // TODO: Always false?
                                    Log.d(TAG, "hasExtra: " +
                                            intent.hasExtra(Constants.EXTRA_MESSAGE_TYPE));
                                }
                                int messageType = intent.getIntExtra(Constants.EXTRA_MESSAGE_TYPE
                                        , Constants.MESSAGE_TYPE_PUSH);
                                Log.d(TAG, "messageType: " + messageType);
                                switch (messageType) {
                                    case Constants.MESSAGE_TYPE_PUSH:
                                        if (application.getAllowReceivePush()) {
                                            Log.i(TAG, "Allow message");
                                            // Try add flags?
                                            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                                            EventDB.insertEvent(target_package, Event.Type.RECEIVE_PUSH,
                                                    Event.ResultType.OK, context);
                                            return true;
                                        } else {
                                            Log.w(TAG, "Not allow message");
                                            EventDB.insertEvent(target_package, Event.Type.RECEIVE_PUSH,
                                                    Event.ResultType.DENY_USER, context);
                                            return false;
                                        }
                                    case Constants.MESSAGE_TYPE_REGISTER_RESULT:
                                        if (application.getAllowReceiveRegisterResult()) {
                                            Log.i(TAG, "Allow callback register result");
                                            // Try add flags?
                                            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                                            return true;
                                        } else {
                                            Log.w(TAG, "Not allow callback register result");
                                            return false;
                                        }
                                }
                                Log.e(TAG, "Not allowed broadcast: " + intent);
                                return false;
                            }
                        }

                        // Deny something will crash...
                        Log.w(TAG, "Allowed: " + intent + ", pkg=" + target_package);
                        return true;
                    }
                });
        return sOptions;
    }
}
