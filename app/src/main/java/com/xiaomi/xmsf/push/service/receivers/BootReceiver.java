package com.xiaomi.xmsf.push.service.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.xiaomi.push.service.ClientEventDispatcher;

/**
 * Created by Trumeet on 2017/8/25.
 * @author Trumeet
 */

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            new ClientEventDispatcher().notifyServiceStarted(context);
        }
    }
}
