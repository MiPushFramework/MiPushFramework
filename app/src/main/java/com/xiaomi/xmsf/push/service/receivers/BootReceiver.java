package com.xiaomi.xmsf.push.service.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static top.trumeet.mipushframework.Constants.TAG;

/**
 * Created by Trumeet on 2017/8/25.
 * @author Trumeet
 */

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Only wake up app
        Log.i(TAG, "boot");
    }
}
