package top.trumeet.mipushframework.push;

import android.app.ActivityManager;
import android.app.job.JobScheduler;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Process;
import android.preference.PreferenceManager;
import android.util.Log;

import com.xiaomi.mipush.sdk.MiPushClient;
import com.xiaomi.push.service.XMPushService;

import java.util.List;

import top.trumeet.mipushframework.Constants;

import static android.content.Context.ACTIVITY_SERVICE;
import static top.trumeet.mipushframework.Constants.APP_ID;
import static top.trumeet.mipushframework.Constants.APP_KEY;
import static top.trumeet.mipushframework.Constants.TAG;

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
            MiPushClient.registerPush(context, APP_ID, APP_KEY);
        } else {
            MiPushClient.unregisterPush(context);
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
            Log.d(TAG, "process -> " + info.process);
            Log.d(TAG, "package -> " + pkg);
            Log.d(TAG, "className -> " + clz);
            Log.d(TAG, "started -> "
                    + info.started);
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
}
