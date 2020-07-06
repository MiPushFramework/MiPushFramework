package top.trumeet.common.override;

import android.app.ActivityManager;
import androidx.annotation.RequiresPermission;

/**
 * Created by Trumeet on 2018/2/18.
 */

public class ActivityManagerOverride {
    @RequiresPermission("android.permission.PACKAGE_USAGE_STATS")
    public static int getPackageImportance(String packageName, ActivityManager manager) {
        return manager.getPackageImportance(packageName);
    }
}
