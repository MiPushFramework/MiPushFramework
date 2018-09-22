package top.trumeet.mipushframework.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import top.trumeet.common.Constants;

@SuppressWarnings("unchecked")
public class MiPushManifestChecker {
    private static final String TAG = MiPushManifestChecker.class.getSimpleName();

    private final Context context;
    private final Class manifestChecker;

    private MiPushManifestChecker(Class manifestChecker, Context context) {
        this.manifestChecker = manifestChecker;
        this.context = context;
    }

    /**
     * Make sure com.xiaomi.xmsf was installed.
     */
    public static MiPushManifestChecker create (@NonNull Context context) throws PackageManager.NameNotFoundException, ClassNotFoundException {
        Class manifestChecker = context.createPackageContext(Constants.SERVICE_APP_NAME, Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY)
                .getClassLoader().loadClass("com.xiaomi.mipush.sdk.ManifestChecker");
        return new MiPushManifestChecker(manifestChecker, context);
    }

    public boolean checkPermissions(PackageInfo packageInfo) {
        try {
            Method method = manifestChecker.getDeclaredMethod("checkPermissions", Context.class, PackageInfo.class);
            method.setAccessible(true);
            method.invoke(null, context, packageInfo);
            return true;
        } catch (Throwable e) {
            if (!isIllegalManifestException(e)) {
                Log.e(TAG, "checkPermissions", e);
            } else {
                Log.e(TAG, "checkPermissions: " + packageInfo.packageName + "," + ((InvocationTargetException) e).getCause().getMessage());
            }
            return false;
        }
    }

    public boolean checkReceivers (String packageName) {
        boolean result;
        try {
             Context appCtx = context.createPackageContext(packageName, Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
             Method method = manifestChecker.getDeclaredMethod("checkReceivers", Context.class);
             method.setAccessible(true);
             method.invoke(null, appCtx);
             result = true;
        } catch (Throwable e) {
            if (!isIllegalManifestException(e)) {
                Log.e(TAG, "checkReceivers", e);
            }
            result = false;
        }
        return result;
    }

    public boolean checkServices (PackageInfo packageInfo) {
        try {
            Method method = manifestChecker.getDeclaredMethod("checkServices", Context.class, PackageInfo.class);
            method.setAccessible(true);
            method.invoke(null, context, packageInfo);
            return true;
        } catch (Throwable e) {
            if (!isIllegalManifestException(e)) {
                Log.e(TAG, "checkServices", e);
            } else {
                Log.e(TAG, "checkServices: " + packageInfo.packageName + "," + ((InvocationTargetException) e).getCause().getMessage());
            }
            return false;
        }
    }
    
    private static boolean isIllegalManifestException (Throwable e) {
        if (e instanceof InvocationTargetException) {
            e = ((InvocationTargetException) e).getTargetException();
        }
        return e.getClass().getName().equals("com.xiaomi.mipush.sdk.ManifestChecker$IllegalManifestException");
    }
}
