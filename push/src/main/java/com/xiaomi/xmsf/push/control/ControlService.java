package com.xiaomi.xmsf.push.control;

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
import top.trumeet.common.override.AppOpsManagerOverride;
import top.trumeet.common.override.ManifestOverride;
import top.trumeet.common.utils.Utils;

/**
 * Created by Trumeet on 2017/12/22.
 */

public class ControlService extends Service {

    private static void enforcePermission (String permName) {
        if (!checkPermission(permName)) {
            throw new SecurityException("Uid " + Binder.getCallingUid() + " doesn't have permission " + permName);
        }
    }

    private static boolean checkPermission (String permName) {
        return Utils.getApplication().checkPermission(permName, Binder.getCallingPid(),
                Binder.getCallingUid()) == PackageManager.PERMISSION_GRANTED;
    }

    private IPushController.Stub stub = new IPushController.Stub() {

        @Override
        public boolean isEnable(boolean strict) throws RemoteException {
            enforcePermission(Constants.permissions.READ_SETTINGS);
            return strict ?
                    PushControllerUtils.isAllEnable(ControlService.this)
                    :
                    (PushControllerUtils.isPrefsEnable(ControlService.this)
                    && PushControllerUtils.isServiceRunning(ControlService.this)
                    && PushControllerUtils.isBootReceiverEnable(ControlService.this));
        }

        @Override
        public void setEnable(boolean enable) throws RemoteException {
            enforcePermission(Constants.permissions.WRITE_SETTINGS);
            PushControllerUtils.setAllEnable(enable, ControlService.this);
        }

        @Override
        public int getVersionCode() throws RemoteException {
            enforcePermission(Constants.permissions.READ_SETTINGS);
            return Constants.PUSH_SERVICE_VERSION_CODE;
        }

        @Override
        public int checkOp(int op) throws RemoteException {
            if (!checkPermission(Constants.permissions.READ_SETTINGS)) {
                enforcePermission(ManifestOverride.permission.GET_APP_OPS_STATS);
            }
            int mode = AppOpsManagerOverride.checkOpNoThrow(
                    op
                    , Process.myUid(), getPackageName(), ((AppOpsManager)getSystemService(APP_OPS_SERVICE)));
            return mode;
        }
    };

    @Override
    public IBinder onBind (Intent intent) {
        return stub;
    }
}
