package top.trumeet.mipushframework.push;

import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.PowerManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import me.pqpo.librarylog4a.Log4a;

/**
 * Created by Trumeet on 2017/8/25.
 * A util class to check XMPush accessibility
 */

public class PushServiceAccessibility {
    private static final String TAG = "Accessibility";

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
        Log4a.d(TAG, "Check allow run in background");
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
                Log4a.e(TAG, "ERRORED");
                return true;
            } else {
                Log4a.d(TAG, "Mode: " + mode);
                return mode != AppOpsManager.MODE_IGNORED;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }
}
