package com.xiaomi.xmsf.push.hooks;

import android.os.Build;

import com.taobao.android.dexposed.XC_MethodHook;
import com.xiaomi.xmsf.push.hooks.impl.HookMIUIPushSdk;
import com.xiaomi.xmsf.push.hooks.impl.HookPingReceiver;
import com.xiaomi.xmsf.push.hooks.impl.HookPkgUninstallReceiver;

import java.util.ArrayList;
import java.util.List;

import me.pqpo.librarylog4a.Log4a;
import me.weishu.epic.art.EpicNative;

/**
 * Created by zts1993 on 2018/4/16.
 */
public class PushSdkHooks {
    private static final String TAG = PushSdkHooks.class.getSimpleName();

    private static List<IHook> registeredHooks = new ArrayList<>();

    static {
        //register
        registeredHooks.add(new HookMIUIPushSdk());
        registeredHooks.add(new HookPkgUninstallReceiver());
        registeredHooks.add(new HookPingReceiver());
    }


    public XC_MethodHook.Unhook[] getHooks() {
        List<XC_MethodHook.Unhook> unhooks = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= 27) {
            // TODO support Android P
            return unhooks.toArray(new XC_MethodHook.Unhook[unhooks.size()]);
        }
        // 判断 Epic 资瓷 abi 与否，very bad
        try {
            EpicNative.getMethodAddress(PushSdkHooks.class.getDeclaredMethod("getHooks"));
        } catch (UnsatisfiedLinkError | NoSuchMethodException ignored) {
            Log4a.e(TAG, "dexposed not supported");
            return unhooks.toArray(new XC_MethodHook.Unhook[unhooks.size()]);
        }

        for (IHook unhook : registeredHooks) {
            try {
                Log4a.d(TAG, "init hook : " + unhook.getClass().getSimpleName());
                unhooks.addAll(unhook.fetchHook());
            } catch (Exception e) {
                Log4a.e(TAG, "Hook failed", e);
            }
        }
        return unhooks.toArray(new XC_MethodHook.Unhook[]{});
    }

}
