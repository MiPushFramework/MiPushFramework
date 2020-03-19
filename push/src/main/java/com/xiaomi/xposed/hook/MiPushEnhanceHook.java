package com.xiaomi.xposed.hook;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import top.trumeet.common.override.UserHandleOverride;

import static org.meowcat.notaichi.NoTaiChi.checkTC;
import static top.trumeet.common.Constants.FAKE_CONFIGURATION_GLOBAL;
import static top.trumeet.common.Constants.FAKE_CONFIGURATION_NAME_TEMPLATE;

/**
 * XiaoMi fake build hook for xposed
 * @author zts1993
 * @date 2018/3/20
 */
public class MiPushEnhanceHook implements IXposedHookLoadPackage {

    private static final String TAG = "MiPushEnhanceHook";
    private static final String STORAGE_PATH = "/sdcard/";

    private Set<String> blackList = new CopyOnWriteArraySet<>();

    private static final Map<String, String> FAKE_VARS = new HashMap<>();

    private static final String BRAND = "Xiaomi";


    {
        String[] blackApps = {"com.xiaomi.xmsf",
                "top.trumeet.mipush",
                // Android
                "com.google.android",
                "android",
                "com.android",
                // Samsung
                "com.bst",
                "com.sec",
                "com.sem",
                "com.sgmc",
                "com.dsi.ant",
                "com.wsomacp",
                "com.samsung",
                "com.diotek.sec",
                "com.enhance.gameservice",
                // XDA
                "com.xda",
                // Wechat
                "com.tencent.mm",
                // Quickpay
                "com.example",
                // Magisk
                "com.topjohnwu.magisk",
                // Xposed
                "org.meowcat.edxposed.manager",
                "de.robv.android.xposed.installer"};
        blackList.addAll(Arrays.asList(blackApps));
    }

    static {
        FAKE_VARS.put("ro.miui.ui.version.name", "V11");
        FAKE_VARS.put("ro.miui.ui.version.code", "9");
        FAKE_VARS.put("ro.miui.version.code_time", "1570636800");
        FAKE_VARS.put("ro.miui.internal.storage", STORAGE_PATH);
        FAKE_VARS.put("ro.product.manufacturer", BRAND);
        FAKE_VARS.put("ro.product.brand", BRAND);
        FAKE_VARS.put("ro.product.name", BRAND);

    }


    private boolean inBlackList(String pkgName) {
        for (String b : blackList) {
            if (pkgName.contains(b)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            String packageName = lpparam.packageName;

            try {

                if (!new File(FAKE_CONFIGURATION_GLOBAL).exists()){

                    // TODO: Remove hidden api usage
                    if (!new File(String.format(FAKE_CONFIGURATION_NAME_TEMPLATE,
                            UserHandleOverride.getUserHandleForUid(lpparam.appInfo.uid).hashCode(),
                            packageName)).exists())
                        // Skipped according user's settings
                        return;

                } else {
                    XposedBridge.log( TAG + ": using global fake config for " + packageName);
                }

            } catch (Throwable e) {
                XposedBridge.log(TAG + ": get config: " + Log.getStackTraceString(e));
            }

            if (inBlackList(packageName)) {
                XposedBridge.log( TAG + ": hit blacklist when fake build for " + packageName);
                return;
            }

            XposedBridge.hookAllMethods(XposedHelpers.findClass("android.os.SystemProperties", lpparam.classLoader),
                    "get", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    checkTC(param);
                    if (FAKE_VARS.containsKey(param.args[0].toString())) {
                        param.setResult(FAKE_VARS.get(param.args[0].toString()));
                    }
                }
            });

            XposedHelpers.setStaticObjectField(android.os.Build.class, "MANUFACTURER", BRAND);
            XposedHelpers.setStaticObjectField(android.os.Build.class, "BRAND", BRAND);
            XposedHelpers.setStaticObjectField(android.os.Build.class, "PRODUCT", BRAND);

        } catch (Throwable throwable) {
            Log.e(TAG, "hook meet exception : " + throwable.getLocalizedMessage(), throwable);
            XposedBridge.log(TAG + ": hook: " + throwable);
        }
    }
}
