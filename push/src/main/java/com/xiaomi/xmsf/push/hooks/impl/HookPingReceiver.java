package com.xiaomi.xmsf.push.hooks.impl;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.taobao.android.dexposed.DexposedBridge;
import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.xiaomi.push.service.PushConstants;
import com.xiaomi.push.service.PushServiceConstants;
import com.xiaomi.push.service.PushServiceMain;
import com.xiaomi.push.service.timers.Alarm;
import com.xiaomi.xmsf.push.hooks.IHook;

import me.pqpo.librarylog4a.Log4a;

/**
 * Created by zts1993 on 2018/4/16.
 */

public class HookPingReceiver implements IHook {
    private static final String TAG = HookPingReceiver.class.getSimpleName();

    @Override
    public XC_MethodHook.Unhook fetchHook() throws Exception {
        return DexposedBridge.findAndHookMethod(Class.forName("com.xiaomi.push.service.receivers.PingReceiver"),
                "onReceive", Context.class, Intent.class, new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                        Context context = (Context) param.args[0];
                        Intent intent = (Intent) param.args[1];
                        Alarm.registerPing(false);
                        Log4a.v(TAG, intent.getPackage() + " is the package name");
                        if (!PushConstants.ACTION_PING_TIMER.equals(intent.getAction())) {
                            Log4a.w(TAG, "cancel the old ping timer");
                            Alarm.stop();
                        } else if (TextUtils.equals(context.getPackageName(), intent.getPackage())) {
                            Log4a.v(TAG, "Ping hooked XMChannelService on timer");
                            try {
                                Intent serviceIntent = new Intent(context, PushServiceMain.class);
                                serviceIntent.putExtra(PushServiceConstants.EXTRA_TIME_STAMP, System.currentTimeMillis());
                                serviceIntent.setAction(PushServiceConstants.ACTION_TIMER);
                                context.startService(serviceIntent);
                            } catch (Throwable e) {
                                Log4a.e(TAG, e);
                            }
                        }
                        return null;
                    }
                });
    }
}
