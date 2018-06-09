package top.trumeet.common.utils;

import android.support.annotation.NonNull;

/**
 * @author Trumeet
 * @date 2018/1/30
 */

public class NotificationUtils {
    public static String getChannelIdByPkg(@NonNull String packageName) {
        // update version 2
        return "ch_" + packageName;
    }

    public static String getGroupIdByPkg(@NonNull String packageName) {
        return "gp_" + packageName;
    }
}
