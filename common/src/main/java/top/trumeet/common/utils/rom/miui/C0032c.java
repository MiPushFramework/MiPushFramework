package top.trumeet.common.utils.rom.miui;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

import java.io.File;

class C0032c {
    private static PackageInfo m40a(Context context, String str) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(str, 128);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageInfo;
    }

    private static String m41a(String str) {
        return C0032c.m43a(new String[]{"/data/app/" + str + "-1.apk", "/data/app/" + str + "-2.apk", "/data/app/" + str + "-1/base.apk", "/data/app/" + str + "-2/base.apk"});
    }

    private static String m42a(String str, String str2) {
        String a = C0032c.m41a(str);
        return a == null ? C0032c.m44b(str2) : a;
    }

    private static String m43a(String[] strArr) {
        for (String str : strArr) {
            if (new File(str).exists()) {
                return str;
            }
        }
        return null;
    }

    private static String m44b(String str) {
        return C0032c.m43a(new String[]{"/system/app/" + str + ".apk", "/system/priv-app/" + str + ".apk", "/system/app/" + str + "/" + str + ".apk", "/system/priv-app/" + str + "/" + str + ".apk"});
    }

    private static String m45c(String str) {
        return "/data/data/" + str + "/lib/";
    }

    public static String getApkPath(Context context, String str, String str2) {
        if (context == null) {
            return C0032c.m42a(str, str2);
        }
        PackageInfo a = C0032c.m40a(context, str);
        return a != null ? a.applicationInfo.publicSourceDir : null;
    }

    public static String getLibPath(Context context, String str) {
        if (context == null) {
            return C0032c.m45c(str);
        }
        PackageInfo a = C0032c.m40a(context, str);
        return a != null ? a.applicationInfo.nativeLibraryDir : null;
    }

    public static boolean isMiuiSystem() {
        return C0032c.m44b("miui") != null;
    }
}
