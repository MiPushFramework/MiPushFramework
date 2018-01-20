package com.xiaomi.xmsf.push.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.oasisfeng.condom.CondomContext;
import com.xiaomi.push.service.PushServiceConstants;
import com.xiaomi.push.service.XMPushService;
import com.xiaomi.xmsf.BuildConfig;
import com.xiaomi.xmsf.push.control.XMOutbound;

import me.pqpo.librarylog4a.Log4a;

import static top.trumeet.common.Constants.TAG_CONDOM;

/**
 * Created by Trumeet on 2018/1/19.
 */

public class PushServiceMain extends XMPushService {
    private static final String TAG = "PushService";
    @Override
    public void attachBaseContext (Context base) {
        Log4a.d(TAG, "attachBaseContext");
        super.attachBaseContext(CondomContext.wrap(base, TAG_CONDOM, XMOutbound.create(base,
                TAG)));
    }

    @Override
    public void onStart(Intent intent, int i) {
        //Log4a.d(TAG, "onStart");
        Log4a.d(TAG, "action: " + ((intent == null || intent.getAction() == null) ?
        "(Null)" : intent.getAction()));
        if (BuildConfig.DEBUG && intent != null) {
            Log4a.d(TAG, intent.toString());
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                for (String key : bundle.keySet()) {
                    Object value = bundle.get(key);
                    Log4a.d(TAG, String.format("%s %s (%s)", key,
                            value != null ? value.toString() :
                            "(Null)", value != null ? value.getClass().getName()
                    : "(Null)"));
                }
            }
        }
        if (intent != null && intent.getAction() != null &&
                !PushServiceConstants.ACTION_NETWORK_STATUS_CHANGED.equalsIgnoreCase(intent.getAction())
                && !PushServiceConstants.ACTION_CHECK_ALIVE.equalsIgnoreCase(intent.getAction())
                && PushServiceConstants.ACTION_TIMER.equalsIgnoreCase(intent.getAction())) {
            Log4a.d(TAG, "Handle intent: " + intent.toString());
            return;
        }
        super.onStart(intent, i);
    }

    @Override
    public int onStartCommand(Intent intent, int i, int i2) {
        onStart(intent, i2);
        return Service.START_STICKY;
    }
}
