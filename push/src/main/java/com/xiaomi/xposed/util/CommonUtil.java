package com.xiaomi.xposed.util;

import android.content.pm.ApplicationInfo;

/**
 * Created by zts1993 on 2018/3/20.
 */

public class CommonUtil {

    public static boolean isUserApplication(ApplicationInfo applicationInfo) {
        return (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0;
    }




}
