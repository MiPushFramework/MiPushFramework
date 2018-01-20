package com.android.internal.app;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;

public interface IAppOpsService extends IInterface {
    public abstract static class Stub extends Binder implements IAppOpsService {
        public static IAppOpsService asInterface (IBinder obj) {
            throw new RuntimeException("Stub!");
        }
    }
}