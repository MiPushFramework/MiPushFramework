package com.xiaomi.xmsf.crash;

import android.annotation.NonNull;

import me.pqpo.librarylog4a.Log4a;

/**
 * Created by Trumeet on 2018/2/4.
 *
 * Log crashes
 */

public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private final Thread.UncaughtExceptionHandler mBase;

    public CrashHandler(@NonNull Thread.UncaughtExceptionHandler mBase) {
        this.mBase = mBase;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        Log4a.e("Crash", t.toString() + ": Application crashed", e);
        if (mBase != null) mBase.uncaughtException(t, e);
    }
}
