package top.trumeet.common.ipc;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * Created by Trumeet on 2017/12/22.
 */

public class IPCUtils {
    public static Disconnectable connectService (@NonNull Intent service,
                                                                       @NonNull final Context context,
                                                                       int flags,
                                                                       @NonNull final ServiceConnectionListener listener) {
        final ServiceConnection connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, final IBinder service) {
                try {
                    service.linkToDeath(new IBinder.DeathRecipient() {
                        @Override
                        public void binderDied() {
                            if (!service.isBinderAlive() &&
                                    !service.pingBinder()) {
                                Log.w("IPCUtils", "Binder died!");
                                listener.onDisconnected();
                            }
                        }
                    }, 0);
                } catch (Exception e) {
                    Log.e("IPCUtils", "Unable to link to death", e);
                }
                listener.onReady(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                listener.onDisconnected();
            }
        };
        context.bindService(service, connection, flags);
        return new Disconnectable() {
            @Override
            public void disconnect() {
                context.unbindService(connection);
            }
        };
    }
}
