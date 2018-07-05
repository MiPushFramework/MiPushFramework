package com.xiaomi.xposed.hook;

import android.content.Context;
import android.util.Log;

import com.xiaomi.xmsf.BuildConfig;
import com.xiaomi.xposed.util.CommonUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MiPushEnhanceHook implements IXposedHookLoadPackage {

    private static final String TAG = "MiPushEnhanceHook";

    private Set<String> blackList = new CopyOnWriteArraySet<>();
    private Map<String, String> fakeMap = new HashMap<>();

    private static String BRAND = "Xiaomi";

    {
        blackList.add("android");
        blackList.add("de.robv.android.xposed.installer");
        blackList.add("com.xiaomi.xmsf");
        blackList.add("com.tencent.mm");

        fakeMap.put("ro.miui.ui.version.name", "V9");
        fakeMap.put("ro.miui.ui.version.code", "7");
        fakeMap.put("ro.miui.version.code_time", "1527550858");
        fakeMap.put("ro.miui.internal.storage", "/sdcard/");
        fakeMap.put("ro.product.manufacturer", BRAND);
        fakeMap.put("ro.product.brand", BRAND);
        fakeMap.put("ro.product.name", BRAND);

    }

    private boolean inBlackList(String pkgName) {
        return blackList.contains(pkgName.toLowerCase());
    }

    private volatile Context applicationContext = null;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        try {
            if (lpparam.packageName.equals(BuildConfig.APPLICATION_ID)) {
                XposedHelpers.findAndHookMethod("top.trumeet.mipushframework.MiPushFramework", lpparam.classLoader, "isXposedWork",
                        new XC_MethodReplacement() {
                            @Override
                            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                                return true;
                            }
                        });
                return;
            }

            if (!CommonUtil.isUserApplication(lpparam.appInfo)) {
                return;
            }

            if (inBlackList(lpparam.appInfo.packageName)) {
                return;
            }


            XposedBridge.hookAllMethods(XposedHelpers.findClass("android.os.SystemProperties", lpparam.classLoader), "get", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (fakeMap.containsKey(param.args[0].toString())) {
                        param.setResult(fakeMap.get(param.args[0].toString()));
                    }
                }
            });

            XposedHelpers.setStaticObjectField(android.os.Build.class, "MANUFACTURER", BRAND);
            XposedHelpers.setStaticObjectField(android.os.Build.class, "BRAND", BRAND);
            XposedHelpers.setStaticObjectField(android.os.Build.class, "PRODUCT", BRAND);

        } catch (Throwable throwable) {
            Log.e(TAG, "hook meet exception : " + throwable.getLocalizedMessage(), throwable);
            XposedBridge.log(throwable);
        }

    }


}
