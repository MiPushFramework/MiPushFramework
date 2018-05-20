package com.xiaomi.xmsf.push.hooks.impl;

import android.content.Context;

import com.taobao.android.dexposed.DexposedBridge;
import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.xiaomi.channel.commonutils.android.MIUIUtils;
import com.xiaomi.xmsf.push.hooks.IHook;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zts1993 on 2018/4/16.
 */

public class HookMIUIPushSdk implements IHook {

    @Override
    public List<XC_MethodHook.Unhook> fetchHook() throws Exception {
        List<XC_MethodHook.Unhook> list = new ArrayList<>();
        list.add(DexposedBridge.findAndHookMethod(Class.forName("com.xiaomi.channel.commonutils.android.MIUIUtils"),
                "getIsMIUI", new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                        return MIUIUtils.IS_MIUI;
                    }
                }));


        list.add(DexposedBridge.findAndHookMethod(Class.forName("com.xiaomi.channel.commonutils.android.DeviceInfo"),
                "quicklyGetIMEI", Context.class, new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                        return "";
                    }
                }));

        return list;
    }
}
