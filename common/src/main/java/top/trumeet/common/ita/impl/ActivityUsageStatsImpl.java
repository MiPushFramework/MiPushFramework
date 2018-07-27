package top.trumeet.common.ita.impl;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.support.annotation.RequiresPermission;
import android.util.Log;
import android.widget.Toast;

import top.trumeet.common.ita.ITopActivity;

import java.util.Objects;

import top.trumeet.common.override.ActivityManagerOverride;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * Created by zts1993 on 2018/2/18.
 */

public class ActivityUsageStatsImpl implements ITopActivity {
    private static final String TAG = "ActivityUsageStatsImpl";

    @Override
    public boolean isEnabled(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);

            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            return Objects.requireNonNull(appOpsManager).checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    applicationInfo.uid, applicationInfo.packageName) == AppOpsManager.MODE_ALLOWED;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

        return false;
    }

    @Override
    public void guideToEnable(Context context) {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    @RequiresPermission("android.permission.PACKAGE_USAGE_STATS")
    public boolean isAppForeground(Context context, String packageName) {
        try {
            int level = ActivityManagerOverride.getPackageImportance(packageName,
                    ((ActivityManager) Objects.requireNonNull(context.getSystemService(ACTIVITY_SERVICE))));
            return level == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;

        } catch (RuntimeException e) {
            Toast.makeText(context, top.trumeet.common.R.string.error_usage_stats, Toast.LENGTH_LONG)
                    .show();
            return false;
        }
    }

}
