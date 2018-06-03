package top.trumeet.common.utils;

import android.support.annotation.NonNull;

/**
 * @author Trumeet
 * @date 2018/1/30
 */

public class NotificationUtils {
    public static String getChannelIdByPkg(@NonNull String packageName) {
        return "app_" + packageName;
    }
}
