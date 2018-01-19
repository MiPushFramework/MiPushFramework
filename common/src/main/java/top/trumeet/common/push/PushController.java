package top.trumeet.common.push;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import top.trumeet.common.Constants;
import top.trumeet.common.IPushController;
import top.trumeet.common.ipc.Disconnectable;
import top.trumeet.common.ipc.IPCUtils;
import top.trumeet.common.ipc.ServiceConnectionListener;
import top.trumeet.common.utils.Utils;

/**
 * Created by Trumeet on 2017/12/22.
 * The client of PushControllerService
 */

public class PushController {
    public static abstract class OnReadyListener {
        public void onReady () {}
        public void onDisconnected () {}
    }

    private IPushController mService;
    private Disconnectable mConnection;

    public PushController () {
    }

    public void connect (Context context,
                         @Nullable final OnReadyListener listener) {
        if (!Utils.isServiceInstalled()) {
            Log.e("PushController", "Service not installed.");
            return;
        }
        mConnection = IPCUtils.connectService(new Intent()
                .setComponent(new ComponentName(Constants.SERVICE_APP_NAME,
                        Constants.CONTROLLER_SERVICE_NAME)),
                context, Context.BIND_AUTO_CREATE,
                new ServiceConnectionListener() {
                    @Override
                    public void onReady(IBinder binder) {
                        mService = IPushController.Stub.asInterface(binder);
                        if (listener != null)
                            listener.onReady();
                    }

                    @Override
                    public void onDisconnected () {
                        Log.e("PushController", "disconnected");
                        mService = null;
                        mConnection = null;
                        listener.onDisconnected();
                    }
                });
    }

    public interface RetryListener {
        void onRetry (boolean result);
    }

    /**
     * Get instance and connect sync
     * @return Instance
     */
    @NonNull
    public static PushController getConnected (@NonNull Context context, @Nullable final OnReadyListener disconnectListener) {
        PushController controller = new PushController();
        if (!Utils.isServiceInstalled()) {
            Log.e("PushController", "Service not installed.");
            return controller;
        }
        final CountDownLatch latch = new CountDownLatch(1);
        try {
            controller.connect(context,
                    new OnReadyListener() {
                        @Override
                        public void onReady() {
                            Log.i("PushController", "Service ready.");
                            latch.countDown();
                        }

                        @Override
                        public void onDisconnected () {
                            if (disconnectListener != null)
                                disconnectListener.onDisconnected();
                        }
                    });
            latch.await(10, TimeUnit.SECONDS);
            return controller;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void disconnect () {
        if (mConnection != null)
            mConnection.disconnect();
    }

    private void enforceConnected () {
        if (mConnection == null ||
                getService() == null)
            throw new NullPointerException("Service not ready yet, please call \"connect\".");
    }

   /** Get enable status of service
   * @param strict check all status
   **/
    public boolean isEnable (boolean strict) {
        enforceConnected();
        try {
            return getService().isEnable(strict);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public int getVersionCode () {
        enforceConnected();
        try {
            return getService().getVersionCode();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void setEnable (boolean enable) {
        enforceConnected();
        try {
            getService().setEnable(enable);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public int checkOp (int op) {
        enforceConnected();
        try {
            return getService().checkOp(op);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isConnected () {
        return getService() != null && mConnection != null;
    }

    private synchronized IPushController getService () {
        return mService;
    }

    public void disconnectIfNeeded () {
        if (isConnected())
            disconnect();
    }
}
