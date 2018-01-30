package top.trumeet.common.utils;

import android.annotation.NonNull;

/**
 * Created by Trumeet on 2018/1/30.
 */

public class NotificationUtils {
    public static String getChannelIdByPkg (@NonNull String packageName) {
        return "app_" + packageName;
    }
}
