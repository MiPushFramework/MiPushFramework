package com.xiaomi.xmsf.push.service.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import androidx.core.content.ContextCompat;
import com.xiaomi.channel.commonutils.logger.MyLog;
import com.xiaomi.push.service.PushConstants;
import com.xiaomi.push.service.PushServiceConstants;
import com.xiaomi.push.service.PushServiceMain;
import com.xiaomi.push.service.timers.Alarm;

public class MiPushPingReceiver extends BroadcastReceiver {

    public MiPushPingReceiver() {
    }

    public void onReceive(Context paramContext, Intent paramIntent) {
        Alarm.registerPing(false);
        MyLog.v(paramIntent.getPackage() + " is the package name");
        if (PushConstants.ACTION_PING_TIMER.equals(paramIntent.getAction())) {
            if (TextUtils.equals(paramContext.getPackageName(), paramIntent.getPackage())) {
                MyLog.v("Ping XMChannelService on timer");

                try {
                    Intent localIntent = new Intent(paramContext, PushServiceMain.class);
                    localIntent.putExtra(PushServiceConstants.EXTRA_TIME_STAMP, System.currentTimeMillis());
                    localIntent.setAction(PushServiceConstants.ACTION_TIMER);
                    ContextCompat.startForegroundService(paramContext, localIntent);
                } catch (Exception localException) {
                    MyLog.e(localException);
                }

            } else {
                MyLog.w("cancel the old ping timer");
                Alarm.stop();
            }
        }
    }
}
