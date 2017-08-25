package com.xiaomi.xmsf.push.service;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;

import top.trumeet.mipushframework.push.PushController;

import static top.trumeet.mipushframework.Constants.TAG;

public class XMPushService extends IntentService {
    public XMPushService() {
        super("XMPushService Bridge");
    }

    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent -> A application want to register push");
        if (!PushController.isPrefsEnable(this)) {
            Log.e(TAG, "Not allowed in SP! Just return!");
            return;
        }
        Intent intent2 = new Intent();
        intent2.setComponent(new ComponentName(getPackageName(), "com.xiaomi.push.service.XMPushService"));
        intent2.setAction(intent.getAction());
        intent2.putExtras(intent);
        startService(intent2);
    }
}
