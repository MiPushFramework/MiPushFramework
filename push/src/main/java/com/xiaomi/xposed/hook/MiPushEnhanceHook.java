package com.xiaomi.xposed.hook;

import android.util.Log;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import top.trumeet.common.override.UserHandleOverride;

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
        blackList.add("com.google.android");
        blackList.add("de.robv.android.xposed.installer");
        blackList.add("com.xiaomi.xmsf");
        blackList.add("com.tencent.mm");
        blackList.add("top.trumeet.mipush");
    }

    static {
        FAKE_VARS.put("ro.miui.ui.version.name", "V9");
        FAKE_VARS.put("ro.miui.ui.version.code", "7");
        FAKE_VARS.put("ro.miui.version.code_time", "1527550858");
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
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
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
                    Log.d(TAG, "using global fake config for " + packageName);
                }

            } catch (Throwable e) {
                XposedBridge.log(TAG + ": get config: " + Log.getStackTraceString(e));
            }

            if (inBlackList(packageName)) {
                Log.d(TAG, "hit blacklist when fake build for " + packageName);
                return;
            }

            XposedBridge.hookAllMethods(XposedHelpers.findClass("android.os.SystemProperties", lpparam.classLoader),
                    "get", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
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
