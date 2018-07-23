package com.xiaomi.xposed.util;

import android.content.pm.ApplicationInfo;

/**
 * common util
 * @author zts1993
 * @date 2018/3/20
 */

public class CommonUtil {

    public static boolean isUserApplication(ApplicationInfo applicationInfo) {
        return (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0;
    }




}
