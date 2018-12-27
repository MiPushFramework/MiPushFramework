package com.xiaomi.xmsf.push.service.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.elvishew.xlog.Logger;
import com.elvishew.xlog.XLog;
import com.xiaomi.channel.commonutils.logger.MyLog;
import com.xiaomi.push.service.PushServiceConstants;
import com.xiaomi.push.service.PushServiceMain;



/**
 * @author zts
 */
public class KeepAliveReceiver extends BroadcastReceiver {
    private final Logger logger = XLog.tag(KeepAliveReceiver.class.getSimpleName()).build();

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

            logger.d("start service when " + intent.getAction());
            Intent localIntent = new Intent(context, PushServiceMain.class);
            localIntent.putExtra(PushServiceConstants.EXTRA_TIME_STAMP, now);
            localIntent.setAction(PushServiceConstants.ACTION_CHECK_ALIVE);
            context.startService(localIntent);
        } catch (Exception localException) {
            MyLog.e(localException);
        }
    }
}
