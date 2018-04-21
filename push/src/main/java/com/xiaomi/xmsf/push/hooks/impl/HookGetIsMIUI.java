package com.xiaomi.xmsf.push.hooks.impl;

import com.taobao.android.dexposed.DexposedBridge;
import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.xiaomi.channel.commonutils.android.MIUIUtils;
import com.xiaomi.xmsf.push.hooks.IHook;

/**
 * Created by zts1993 on 2018/4/16.
 */

public class HookGetIsMIUI implements IHook {

    @Override
    public XC_MethodHook.Unhook fetchHook() throws Exception {
        return DexposedBridge.findAndHookMethod(Class.forName("com.xiaomi.channel.commonutils.android.MIUIUtils"),
                "getIsMIUI", new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                        return MIUIUtils.IS_MIUI;
                    }
                });
    }
}
