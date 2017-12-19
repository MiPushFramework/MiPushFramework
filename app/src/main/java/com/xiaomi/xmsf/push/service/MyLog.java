package com.xiaomi.xmsf.push.service;

import me.pqpo.librarylog4a.Log4a;

import static top.trumeet.mipushframework.Constants.TAG;

public abstract class MyLog {


    public static void e(String paramString)
    {
        Log4a.e("", paramString);
    }

    public static void v(String paramString)
    {
        Log4a.v("", paramString);
    }

    public static void w(String paramString)
    {
        Log4a.w("", paramString);
    }

    public static void w(String paramString1, String paramString2, Throwable paramThrowable)
    {
        Log4a.w(paramString1, paramString2, paramThrowable);
    }

    public static void m17e(String str) {
        Log4a.e(TAG, str);
    }

    public static void m18v(String str) {
        Log4a.d(TAG, str);
    }

    public static void m19w(String str) {
        Log4a.w(TAG, str);
    }

    public static void m20w(String str, String str2, Throwable th) {
        Log4a.w(TAG, str2, th);
    }
}
