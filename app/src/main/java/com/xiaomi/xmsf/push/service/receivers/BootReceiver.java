package com.xiaomi.xmsf.push.service.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.xiaomi.push.service.ClientEventDispatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Trumeet on 2017/8/25.
 * @author Trumeet
 */

public class BootReceiver extends BroadcastReceiver {
    private Logger logger = LoggerFactory.getLogger(BootReceiver.class);
    @Override
    public void onReceive(Context context, Intent intent) {
        logger.debug("boot");
        if (intent != null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            new ClientEventDispatcher().notifyServiceStarted(context);
        }
    }
}
