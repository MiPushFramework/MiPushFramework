package com.xiaomi.xmsf.crash;

import android.support.annotation.NonNull;

import me.pqpo.librarylog4a.Log4a;

/**
 *
 * @author Trumeet
 * @date 2018/2/4
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
        Log4a.flush();
        mBase.uncaughtException(t, e);
    }
}
