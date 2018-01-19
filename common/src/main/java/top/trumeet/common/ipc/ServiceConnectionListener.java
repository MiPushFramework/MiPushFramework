package top.trumeet.common.ipc;

import android.os.IBinder;

/**
 * Created by Trumeet on 2017/12/22.
 */

public abstract class ServiceConnectionListener {
    public abstract void onReady (IBinder binder);

    public void onDisconnected () {}
}
