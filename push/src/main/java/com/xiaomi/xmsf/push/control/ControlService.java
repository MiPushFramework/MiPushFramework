package com.xiaomi.xmsf.push.control;

import android.Manifest;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;

import top.trumeet.common.Constants;
import top.trumeet.common.IPushController;

/**
 * Created by Trumeet on 2017/12/22.
 */

public class ControlService extends Service {

    private static void enforcePermission (String permName) {
        if (!checkPermission(permName))
            throw new SecurityException("Uid " + Binder.getCallingUid() + " doesn't have permission " + permName);
    }

    private static boolean checkPermission (String permName) {
        try {
            if (AppGlobals.getPackageManager()
                    .checkUidPermission(permName,
                            Binder.getCallingUid()) != PackageManager.PERMISSION_GRANTED)
                return false;
            return true;
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    private IPushController.Stub stub = new IPushController.Stub() {

        @Override
        public boolean isEnable(boolean strict) throws RemoteException {
            enforcePermission(Constants.permissions.GET_PUSH_ENABLE_SETTING);
            return strict ?
                    PushControllerUtils.isAllEnable(ControlService.this)
                    :
                    (PushControllerUtils.isPrefsEnable(ControlService.this)
                    && PushControllerUtils.isServiceRunning(ControlService.this)
                    && PushControllerUtils.isBootReceiverEnable(ControlService.this));
        }

        @Override
        public void setEnable(boolean enable) throws RemoteException {
            enforcePermission(Constants.permissions.CHANGE_PUSH_ENABLE_SETTING);
            PushControllerUtils.setAllEnable(enable, ControlService.this);
        }

        @Override
        public int getVersionCode() throws RemoteException {
            enforcePermission(Constants.permissions.GET_VERSION);
            return Constants.PUSH_SERVICE_VERSION_CODE;
        }

        @Override
        public int checkOp(int op) throws RemoteException {
            if (!checkPermission(Constants.permissions.CHECK_APP_OPS_STATUS))
            enforcePermission(Manifest.permission.GET_APP_OPS_STATS);
            int mode = ((AppOpsManager)getSystemService(APP_OPS_SERVICE)).checkOpNoThrow(
                    op
                    , Process.myUid(), getPackageName());
            return mode;
        }
    };

    @Override
    public IBinder onBind (Intent intent) {
        return stub;
    }
}
