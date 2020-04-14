package com.xiaomi.xmsf.push.hooks.impl;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.ContextCompat;

import com.elvishew.xlog.Logger;
import com.elvishew.xlog.XLog;
import com.taobao.android.dexposed.DexposedBridge;
import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.xiaomi.push.service.GeoFenceUtils;
import com.xiaomi.push.service.PushServiceConstants;
import com.xiaomi.push.service.PushServiceMain;
import com.xiaomi.xmsf.push.hooks.IHook;

import java.util.Collections;
import java.util.List;



/**
 * Created by zts1993 on 2018/4/16.
 */

public class HookPkgUninstallReceiver implements IHook {
    private final Logger logger = XLog.tag(HookPkgUninstallReceiver.class.getSimpleName()).build();

    @Override
    public List<XC_MethodHook.Unhook> fetchHook() throws Exception {
        return Collections.singletonList(DexposedBridge.findAndHookMethod(Class.forName("com.xiaomi.push.service.receivers.PkgUninstallReceiver"),
                "onReceive", Context.class, Intent.class, new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                        Context context = (Context) param.args[0];
                        Intent intent = (Intent) param.args[1];
                        if (intent != null && intent.getExtras() !=null) {
                            if ("android.intent.action.PACKAGE_REMOVED".equals(intent.getAction())) {
                                boolean isReplaced = intent.getExtras().getBoolean("android.intent.extra.REPLACING");
                                Uri data = intent.getData();
                                if (data != null && !isReplaced) {
                                    try {
                                        Intent serviceIntent = new Intent(context, PushServiceMain.class);
                                        serviceIntent.setAction(PushServiceConstants.ACTION_UNINSTALL);
                                        serviceIntent.putExtra(PushServiceConstants.EXTRA_UNINSTALL_PKG_NAME, data.getEncodedSchemeSpecificPart());
                                        ContextCompat.startForegroundService(context, serviceIntent);
                                        GeoFenceUtils.appIsUninstalled(context.getApplicationContext(), data.getEncodedSchemeSpecificPart());
                                    } catch (Throwable e) {
                                        logger.e("Hook", e);
                                    }
                                }
                            }
                        }
                        return null;
                    }
                }));
    }
}
