package com.xiaomi.xmsf.push.hooks;

import com.taobao.android.dexposed.XC_MethodHook;
import com.xiaomi.xmsf.push.hooks.impl.HookGetIsMIUI;
import com.xiaomi.xmsf.push.hooks.impl.HookPingReceiver;
import com.xiaomi.xmsf.push.hooks.impl.HookPkgUninstallReceiver;

import java.util.ArrayList;
import java.util.List;

import me.pqpo.librarylog4a.Log4a;

/**
 * Created by zts1993 on 2018/4/16.
 */
public class PushSdkHooks {
    private static final String TAG = PushSdkHooks.class.getSimpleName();

    private static List<IHook> registeredHooks = new ArrayList<>();

    static {
        //register
        registeredHooks.add(new HookGetIsMIUI());
        registeredHooks.add(new HookPkgUninstallReceiver());
        registeredHooks.add(new HookPingReceiver());
    }


    public XC_MethodHook.Unhook[] getHooks() {
        List<XC_MethodHook.Unhook> res = new ArrayList<>();
        for (IHook unhook : registeredHooks) {
            try {
                Log4a.i(TAG, "init hook : " + unhook.getClass().getSimpleName());
                res.add(unhook.fetchHook());
            } catch (Exception e) {
                Log4a.e(TAG, "Hook failed", e);
            }
        }
        return res.toArray(new XC_MethodHook.Unhook[]{});
    }

}
