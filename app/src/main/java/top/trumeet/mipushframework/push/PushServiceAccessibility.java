package top.trumeet.mipushframework.push;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

import com.xiaomi.push.service.XMPushService;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * Created by Trumeet on 2017/8/25.
 * A util class to check XMPush accessibility
 */

public class PushServiceAccessibility {
    private static final String TAG = "Accessibility";

    /**
     * Check push service is running
     * @see com.xiaomi.push.service.XMPushService
     * @param context context param
     * @return is running
     */
    public static boolean isRunning (Context context) {
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

    /**
     * Check this app is in system doze whitelist.
     * @param context Context param
     * @return is in whitelist, always true when pre-marshmallow
     */
    @TargetApi(Build.VERSION_CODES.M)
    public static boolean isInDozeWhiteList (Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true;
        PowerManager powerManager = context.getSystemService(PowerManager.class);
        return powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
    }

    /**
     * Check this app is in allowed background activity.
     * @param context Context param
     * @return allowed status, always true when pre-oreo
     */
    @TargetApi(Build.VERSION_CODES.O)
    public static boolean checkAllowRunInBackground (Context context) {
        Log.d(TAG, "Check allow run in background");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return true;
        try {
            Field field = AppOpsManager.class.getField("OP_RUN_IN_BACKGROUND");
            field.setAccessible(true);
            Method checkOpNoThrow = AppOpsManager.class.getMethod("checkOpNoThrow",
                    int.class,
                    int.class, String.class);
            int mode = (int)checkOpNoThrow.invoke(context.getSystemService(AppOpsManager.class),
                    field.getInt(AppOpsManager.class)
                    , context.getPackageManager()
                            .getPackageUid(context.getPackageName(),
                                    PackageManager.GET_DISABLED_COMPONENTS), context.getPackageName());
            if (mode == AppOpsManager.MODE_ERRORED) {
                Log.e(TAG, "ERRORED");
                return true;
            } else {
                Log.d(TAG, "Mode: " + mode);
                return mode != AppOpsManager.MODE_IGNORED;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }
}
