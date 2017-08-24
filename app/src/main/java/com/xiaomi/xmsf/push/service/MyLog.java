package com.xiaomi.xmsf.push.service;

import android.util.Log;

public abstract class MyLog {
    //private static final Logger LOGGER = Logger.getLogger("PushService");
    // 不用 Logger，用 android.util.Log
    private static final String TAG = "PushService";

    public static void m17e(String str) {
        Log.e(TAG, str);
    }

    public static void m18v(String str) {
        Log.d(TAG, str);
    }

    public static void m19w(String str) {
        Log.w(TAG, str);
    }

    public static void m20w(String str, String str2, Throwable th) {
        Log.w(TAG, str2, th);
    }
}
