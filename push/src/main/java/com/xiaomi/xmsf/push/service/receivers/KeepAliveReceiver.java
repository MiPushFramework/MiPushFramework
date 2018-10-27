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

    public KeepAliveReceiver(){
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Log4a.i(TAG, "start service when " + intent.getAction());
            Intent localIntent = new Intent(context, PushServiceMain.class);
            localIntent.putExtra(PushServiceConstants.EXTRA_TIME_STAMP, System.currentTimeMillis());
            localIntent.setAction(PushServiceConstants.ACTION_CHECK_ALIVE);
            context.startService(localIntent);
        } catch (Exception localException) {
            MyLog.e(localException);
        }
    }
}
