package top.trumeet.common.push;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.annotation.RestrictTo;
import top.trumeet.common.Constants;
import top.trumeet.common.IPushController;
import top.trumeet.common.ipc.Disconnectable;
import top.trumeet.common.ipc.IPCUtils;
import top.trumeet.common.ipc.ServiceConnectionListener;
import top.trumeet.common.utils.Utils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 使用 Binder 作为 IPC 方式（旧版兼容）
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
class LegacyImpl extends PushController {

    LegacyImpl(@NonNull Context context) {
        super(context);
    }

    private IPushController mService;
    private Disconnectable mConnection;

    @Override
    @RequiresPermission(Constants.permissions.BIND)
    public void connect(@Nullable final AbstractConnectionStatusListener listener) {
        if (!Utils.isServiceInstalled()) {
            Log.e("PushController", "Service not installed.");
            return;
        }
        mConnection = IPCUtils.connectService(new Intent()
                        .setComponent(new ComponentName(Constants.SERVICE_APP_NAME,
                                Constants.CONTROLLER_SERVICE_NAME)),
                getContext(), Context.BIND_AUTO_CREATE,
                new ServiceConnectionListener() {
                    @Override
                    public void onReady(IBinder binder) {
                        mService = IPushController.Stub.asInterface(binder);
                        if (listener != null) {
                            listener.onReady();
                        }
                    }

                    @Override
                    public void onDisconnected() {
                        Log.e("PushController", "disconnected");
                        mService = null;
                        mConnection = null;
                        if (listener != null) {
                            listener.onDisconnected();
                        }
                    }
                });
    }


    /**
     * Get instance and connect sync
     *
     * @return Instance
     */
    @NonNull
    @RequiresPermission(Constants.permissions.BIND)
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static PushController getConnected(@NonNull Context context, @Nullable final AbstractConnectionStatusListener disconnectListener) {
        PushController controller = new LegacyImpl(context);
        if (!Utils.isServiceInstalled()) {
            Log.e("PushController", "Service not installed.");
            return controller;
        }
        final CountDownLatch latch = new CountDownLatch(1);
        try {
            controller.connect(new AbstractConnectionStatusListener() {
                        @Override
                        public void onReady() {
                            Log.i("PushController", "Service ready.");
                            latch.countDown();
                        }

                        @Override
                        public void onDisconnected() {
                            if (disconnectListener != null) {
                                disconnectListener.onDisconnected();
                            }
                        }
                    });
            latch.await(10, TimeUnit.SECONDS);
            return controller;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void disconnect() {
        try {
            if (mConnection != null) {
                mConnection.disconnect();
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // Too bad, 不支持服务版本之后点重试就会 not registered
            e.printStackTrace();
        }
    }

    private void enforceConnected() {
        if (mConnection == null ||
                getService() == null) {
            throw new NullPointerException("Service not ready yet, please call \"connect\".");
        }
    }

    /**
     * Get enable status of service
     *
     * @param strict check all status
     **/
    @Override
    @RequiresPermission(value = Constants.permissions.READ_SETTINGS)
    public boolean isEnable(boolean strict) {
        enforceConnected();
        try {
            return getService().isEnable(strict);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @RequiresPermission(anyOf = {Constants.permissions.READ_SETTINGS, Constants.permissions_old.GET_VERSION})
    public int getVersionCode() {
        enforceConnected();
        try {
            return getService().getVersionCode();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @RequiresPermission(value = Constants.permissions.WRITE_SETTINGS)
    public void setEnable(boolean enable) {
        enforceConnected();
        try {
            getService().setEnable(enable);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @RequiresPermission(value = Constants.permissions.READ_SETTINGS)
    public int checkOp(int op) {
        enforceConnected();
        try {
            return getService().checkOp(op);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isConnected() {
        return getService() != null && mConnection != null;
    }

    private synchronized IPushController getService() {
        return mService;
    }

    @Override
    public void disconnectIfNeeded() {
        if (isConnected()) {
            disconnect();
        }
    }
}
