package com.xiaomi.xmsf.push.hooks.impl;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.elvishew.xlog.Logger;
import com.elvishew.xlog.XLog;
import com.taobao.android.dexposed.DexposedBridge;
import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.xiaomi.push.service.PushConstants;
import com.xiaomi.push.service.PushServiceConstants;
import com.xiaomi.push.service.PushServiceMain;
import com.xiaomi.push.service.timers.Alarm;
import com.xiaomi.xmsf.push.hooks.IHook;

import java.util.Collections;
import java.util.List;



/**
 * Created by zts1993 on 2018/4/16.
 */

public class HookPingReceiver implements IHook {
    private final Logger logger = XLog.tag(HookPingReceiver.class.getSimpleName()).build();

    @Override
    public List<XC_MethodHook.Unhook> fetchHook() throws Exception {
        return Collections.singletonList(DexposedBridge.findAndHookMethod(Class.forName("com.xiaomi.push.service.receivers.PingReceiver"),
                "onReceive", Context.class, Intent.class, new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                        Context context = (Context) param.args[0];
                        Intent intent = (Intent) param.args[1];
                        Alarm.registerPing(false);
                        logger.v(intent.getPackage() + " is the package name");
                        if (!PushConstants.ACTION_PING_TIMER.equals(intent.getAction())) {
                            logger.w("cancel the old ping timer");
                            Alarm.stop();
                        } else if (TextUtils.equals(context.getPackageName(), intent.getPackage())) {
                            logger.v("Ping hooked XMChannelService on timer");
                            try {
                                Intent serviceIntent = new Intent(context, PushServiceMain.class);
                                serviceIntent.putExtra(PushServiceConstants.EXTRA_TIME_STAMP, System.currentTimeMillis());
                                serviceIntent.setAction(PushServiceConstants.ACTION_TIMER);
                                context.startService(serviceIntent);
                            } catch (Throwable e) {
                                logger.e(e);
                            }
                        }
                        return null;
                    }
                }));
    }
}
