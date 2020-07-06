package com.xiaomi.xmsf.push.hooks;

import android.os.Build;
import com.elvishew.xlog.Logger;
import com.elvishew.xlog.XLog;
import com.xiaomi.xmsf.push.hooks.impl.HookMIUIPushSdk;
import com.xiaomi.xmsf.push.hooks.impl.HookPingReceiver;
import com.xiaomi.xmsf.push.hooks.impl.HookPkgUninstallReceiver;
import de.robv.android.xposed.XC_MethodHook;
import me.weishu.epic.art.EpicNative;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author zts1993
 * @date 2018/4/16
 */
public class PushSdkHooks {
    private final Logger logger = XLog.tag(PushSdkHooks.class.getSimpleName()).build();

    private static List<IHook> registeredHooks = new ArrayList<>();

    static {
        //register
        registeredHooks.add(new HookMIUIPushSdk());
        registeredHooks.add(new HookPkgUninstallReceiver());
        registeredHooks.add(new HookPingReceiver());
    }


    public XC_MethodHook.Unhook[] getHooks() {
        List<XC_MethodHook.Unhook> unhooks = new ArrayList<>();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1) {
            // TODO support Android P
            return unhooks.toArray(new XC_MethodHook.Unhook[unhooks.size()]);
        }
        // 判断 Epic 资瓷 abi 与否，very bad
        try {
            EpicNative.getMethodAddress(PushSdkHooks.class.getDeclaredMethod("getHooks"));
        } catch (UnsatisfiedLinkError | NoSuchMethodException ignored) {
            logger.e("dexposed not supported");
            return unhooks.toArray(new XC_MethodHook.Unhook[unhooks.size()]);
        }

        for (IHook unhook : registeredHooks) {
            try {
                logger.d("init hook : " + unhook.getClass().getSimpleName());
                unhooks.addAll(unhook.fetchHook());
            } catch (Exception e) {
                logger.e("Hook failed", e);
            }
        }
        return unhooks.toArray(new XC_MethodHook.Unhook[]{});
    }

}
