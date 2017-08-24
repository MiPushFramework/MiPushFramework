package com.xiaomi.xmsf.push.service;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;

public class XMPushService extends IntentService {
    public XMPushService() {
        super("XMPushService Bridge");
    }

    protected void onHandleIntent(Intent intent) {
        Log.d("XMPushService Bridge", "onHandleIntent");
        Intent intent2 = new Intent();
        intent2.setComponent(new ComponentName(getPackageName(), "com.xiaomi.push.service.XMPushService"));
        intent2.setAction(intent.getAction());
        intent2.putExtras(intent);
        startService(intent2);
    }
}
