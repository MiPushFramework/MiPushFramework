package com.xiaomi.xmsf.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.xiaomi.xmsf.BuildConfig;


/**
 * Push 配置
 * @author zts
 */
public class ConfigCenter {

    private static class LazyHolder {
        volatile static ConfigCenter INSTANCE = new ConfigCenter();
    }


    public static ConfigCenter getInstance() {
        return LazyHolder.INSTANCE;
    }

    private ConfigCenter() {
    }

    //using MODE_MULTI_PROCESS emmm.....
    private SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(BuildConfig.APPLICATION_ID + "_preferences", Context.MODE_MULTI_PROCESS);
    }

    public boolean isNotificationOnRegister(Context ctx) {
        return getSharedPreferences(ctx).getBoolean("NotificationOnRegister", false);
    }

    public boolean isAutoRegister(Context ctx) {
        return getSharedPreferences(ctx).getBoolean("AutoRegister", false);
    }

    public int getAccessMode(Context ctx) {
        String mode = getSharedPreferences(ctx).getString("AccessMode", "0");
        return Integer.valueOf(mode);
    }
}
