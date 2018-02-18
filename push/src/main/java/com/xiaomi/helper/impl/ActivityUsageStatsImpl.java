package com.xiaomi.helper.impl;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.provider.Settings;

import com.xiaomi.channel.commonutils.reflect.JavaCalls;
import com.xiaomi.helper.ITopActivity;

import me.pqpo.librarylog4a.Log4a;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * Created by zts1993 on 2018/2/18.
 */

public class ActivityUsageStatsImpl implements ITopActivity {
    static final String TAG = "ActivityUsageStatsImpl";

    @Override
    public boolean isEnabled(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            return appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    applicationInfo.uid, applicationInfo.packageName) == AppOpsManager.MODE_ALLOWED;
        } catch (Exception e) {
            //ignore
        }

        return false;
    }

    @Override
    public void guideToEnable(Context context) {
        context.startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
    }

    @Override
    public boolean isAppForeground(Context context, String packageName) {
        ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        boolean foreground = false;
        try {
            int importance = JavaCalls.callMethod(am, "getPackageImportance", packageName);
            if (importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) { //TODO more importance can be supported
                foreground = true;
            }
        } catch (Exception e) {
            //ignore
            Log4a.e(TAG, e.getMessage(), e);
        }
        return foreground;
    }

}
