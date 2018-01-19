package top.trumeet.common.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

/**
 * Created by Trumeet on 2017/12/29.
 */

public class ServiceRunningChecker {
    public static boolean isServiceRunning (Context context, Class<?> service) {
        try {
            final boolean bind = context.bindService(new Intent(context, service), emptyConn, 0);
            if (bind) context.unbindService(emptyConn);
            return bind;
        } catch (SecurityException e) {
            return false;
        }
    }

    private static final ServiceConnection emptyConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };
}
