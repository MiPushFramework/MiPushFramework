package top.trumeet.common.utils;

import android.app.AppGlobals;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.os.Process;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.text.Html;

import java.util.Calendar;
import java.util.Date;

import top.trumeet.common.Constants;

public final class Utils {
    public static int myUid() {
        return Process.myUserHandle().hashCode();
    }

    public static boolean isServiceInstalled() {
        return isAppInstalled(Constants.SERVICE_APP_NAME);
    }

    public static Application getApplication() {
        return AppGlobals.getInitialApplication();
    }

    public static PackageManager getPackageManager() {
        return AppGlobals.getInitialApplication().getPackageManager();
    }

    public static boolean isAppOpsInstalled() {
        return isAppInstalled("rikka.appops");
    }

    public static boolean isAppInstalled(String packageName) {
        try {
            return getPackageManager().getPackageInfo(packageName, 0) != null;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @ColorInt
    public static int getColorAttr(Context context, int attr) {
        TypedArray ta = context.obtainStyledAttributes(new int[]{attr});
        @ColorInt int colorAccent = ta.getColor(0, 0);
        ta.recycle();
        return colorAccent;
    }

    public static Date getUTC(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int zoneOffset = cal.get(java.util.Calendar.ZONE_OFFSET);
        int dstOffset = cal.get(java.util.Calendar.DST_OFFSET);
        cal.add(java.util.Calendar.MILLISECOND, -(zoneOffset + dstOffset));
        return cal.getTime();
    }

    public static Date getUTC() {
        return getUTC(new Date());
    }

    public static CharSequence getString(@StringRes int id,
                                         @NonNull Context context,
                                         Object... formatArgs) {
        return toHtml(context.getString(id, formatArgs));
    }

    public static CharSequence toHtml(String str) {
        return Html.fromHtml(str);
    }

    public static boolean isUserApplication(ApplicationInfo applicationInfo) {
        return (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0;
    }

    public static boolean isUserApplication (String pkg) {
        try {
            return isUserApplication(getApplication().getPackageManager().getApplicationInfo(pkg, PackageManager.GET_UNINSTALLED_PACKAGES));
        } catch (PackageManager.NameNotFoundException ignored) {
            return false;
        }
    }
}
