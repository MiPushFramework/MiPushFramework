package com.xiaomi.xmsf.utils;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * @author zts
 */
public class ConfigCenter {

    public static boolean isAutoRegister(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean("AutoRegister", false);
    }

    public static int getAccessMode(Context ctx) {
        String mode = PreferenceManager.getDefaultSharedPreferences(ctx).getString("AccessMode", "0");
        return Integer.valueOf(mode);
    }
}
