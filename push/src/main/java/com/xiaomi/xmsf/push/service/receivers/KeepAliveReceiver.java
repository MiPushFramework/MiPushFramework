package com.xiaomi.xmsf.push.service.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.xiaomi.channel.commonutils.logger.MyLog;
import com.xiaomi.push.service.PushServiceConstants;
import com.xiaomi.push.service.PushServiceMain;

import me.pqpo.librarylog4a.Log4a;

/**
 * @author zts
 */
public class KeepAliveReceiver extends BroadcastReceiver {
    private static final String TAG = KeepAliveReceiver.class.getSimpleName();

    private long lastActive = System.currentTimeMillis();

    public KeepAliveReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            long now = System.currentTimeMillis();

            if ((now - lastActive) < (1000 * 60 * 2)) {
                return;
            }

            lastActive = now;

            Log4a.d(TAG, "start service when " + intent.getAction());
            Intent localIntent = new Intent(context, PushServiceMain.class);
            localIntent.putExtra(PushServiceConstants.EXTRA_TIME_STAMP, now);
            localIntent.setAction(PushServiceConstants.ACTION_CHECK_ALIVE);
            context.startService(localIntent);
        } catch (Exception localException) {
            MyLog.e(localException);
        }
    }
}
