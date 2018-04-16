package com.xiaomi.xmsf.push.hooks;

import com.taobao.android.dexposed.XC_MethodHook;

/**
 * Created by zts1993 on 2018/4/16.
 */

public interface IHook {
    XC_MethodHook.Unhook fetchHook() throws Exception;
}
