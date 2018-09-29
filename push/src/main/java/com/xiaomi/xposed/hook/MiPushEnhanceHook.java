package com.xiaomi.xposed.hook;

import android.util.Log;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import top.trumeet.common.override.UserHandleOverride;

import static top.trumeet.common.Constants.FAKE_CONFIGURATION_NAME_TEMPLATE;

/**
 * XiaoMi fake build hook for xposed
 * @author zts1993
 * @date 2018/3/20
 */
public class MiPushEnhanceHook implements IXposedHookLoadPackage {

    private static final String TAG = "MiPushEnhanceHook";
    private static final String STORAGE_PATH = "/sdcard/";

    private static final Map<String, String> FAKE_VARS = new HashMap<>();

    private static final String BRAND = "Xiaomi";

    static {
        FAKE_VARS.put("ro.miui.ui.version.name", "V9");
        FAKE_VARS.put("ro.miui.ui.version.code", "7");
        FAKE_VARS.put("ro.miui.version.code_time", "1527550858");
        FAKE_VARS.put("ro.miui.internal.storage", STORAGE_PATH);
        FAKE_VARS.put("ro.product.manufacturer", BRAND);
        FAKE_VARS.put("ro.product.brand", BRAND);
        FAKE_VARS.put("ro.product.name", BRAND);

    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        try {
            try {
                // TODO: Remove hidden api usage
                if (!new File(String.format(FAKE_CONFIGURATION_NAME_TEMPLATE,
                        UserHandleOverride.getUserHandleForUid(lpparam.appInfo.uid).hashCode(),
                        lpparam.packageName)).exists())
                    // Skipped according user's settings
                    return;
            } catch (Throwable e) {
                // TODO: Not tested
                XposedBridge.log(TAG + ": get config: " + Log.getStackTraceString(e));
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
