package com.xiaomi.xmsf.push.service;

import android.util.Log;

import static top.trumeet.mipushframework.Constants.TAG;

public abstract class MyLog {

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
