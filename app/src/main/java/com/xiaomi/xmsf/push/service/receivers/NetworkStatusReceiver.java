package com.xiaomi.xmsf.push.service.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.xiaomi.push.service.XMPushService;

public class NetworkStatusReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        Intent intent2 = new Intent(context, XMPushService.class);
        intent2.setAction("com.xiaomi.push.network_status_changed");
        context.startService(intent2);
        /*
        TrafficUtils.notifyNetworkChanage(context);
        if (Network.hasNetwork(context) && PushServiceClient.getInstance(context).isProvisioned()) {
            PushServiceClient.getInstance(context).processRegisterTask();
        }
        */
    }
}
