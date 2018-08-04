package com.xiaomi.xposed.hook;

import android.util.Log;

import com.xiaomi.xposed.util.CommonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * XiaoMi fake build hook for xposed
 * @author zts1993
 * @date 2018/3/20
 */
public class MiPushEnhanceHook implements IXposedHookLoadPackage {

    private static final String TAG = "MiPushEnhanceHook";
    private static final String STORAGE_PATH = "/sdcard/";

    private static Set<String> blackList = new HashSet<>();
    private static List<String> blackList_in = new ArrayList<>();
    private static Map<String, String> fakeMap = new HashMap<>();

    private static String BRAND = "Xiaomi";

    static {
        blackList.add("android");
        blackList.add("de.robv.android.xposed.installer");
        blackList.add("com.xiaomi.xmsf");
        blackList.add("com.tencent.mm");
        blackList.add("top.trumeet.mipush");
        blackList_in.add("com.google.android");

        fakeMap.put("ro.miui.ui.version.name", "V9");
        fakeMap.put("ro.miui.ui.version.code", "7");
        fakeMap.put("ro.miui.version.code_time", "1527550858");
        fakeMap.put("ro.miui.internal.storage", STORAGE_PATH);
        fakeMap.put("ro.product.manufacturer", BRAND);
        fakeMap.put("ro.product.brand", BRAND);
        fakeMap.put("ro.product.name", BRAND);

    }

    private boolean inBlackList(String pkgName) {
        return blackList.contains(pkgName.toLowerCase());
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        try {
            if (!CommonUtil.isUserApplication(lpparam.appInfo)) {
                return;
            }

            if (inBlackList(lpparam.appInfo.packageName)) {
                return;
            }

            for (String s : blackList_in) {
                if (lpparam.appInfo.packageName.startsWith(s)) {
                    return;
                }
            }


            XposedBridge.hookAllMethods(XposedHelpers.findClass("android.os.SystemProperties", lpparam.classLoader),
                    "get", new XC_MethodHook() {
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
