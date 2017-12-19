package com.xiaomi.xmsf.push.service;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;

import com.xiaomi.mistatistic.sdk.controller.LocalEventRecorder;
import com.xiaomi.mistatistic.sdk.data.AbstractEvent;

public class StatService extends Service
{
    private final IStatService.Stub mBinder = new IStatService.Stub()
    {
        public void insertEvent(String paramAnonymousString)
                throws RemoteException
        {
            if (isSystemApp())
            {
                AbstractEvent localAbstractEvent = AbstractEvent.jsonToEvent(paramAnonymousString);
                if (localAbstractEvent != null)
                    LocalEventRecorder.insertEvent(localAbstractEvent);
            }
        }

        public boolean isSystemApp()
        {
            boolean bool = false;
            try
            {
                int i = Binder.getCallingUid();
                PackageManager localPackageManager = StatService.this.getPackageManager();
                String str = localPackageManager.getPackagesForUid(i)[0];
                int j = 0x1 & localPackageManager.getApplicationInfo(str, 0).flags;
                bool = false;
                if (j != 0)
                {
                    MyLog.v(str + "is system app. StatService");
                    bool = true;
                }
                else
                {
                    MyLog.v(str + "is not system app. StatService");
                    bool = false;
                }
            }
            catch (Throwable localThrowable)
            {
                MyLog.e(localThrowable.getMessage());
            }
            return bool;
        }
    };

    public IBinder onBind(Intent paramIntent)
    {
        return this.mBinder;
    }
}