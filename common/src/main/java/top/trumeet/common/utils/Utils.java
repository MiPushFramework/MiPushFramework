package top.trumeet.common.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.AppGlobals;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Process;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.text.Html;
import android.view.View;

import java.util.Calendar;
import java.util.Date;

import top.trumeet.common.Constants;
import top.trumeet.common.R;

public final class Utils {
    public static int myUid () {
        return Process.myUserHandle().hashCode();
    }

    public static boolean isServiceInstalled () {
        return isAppInstalled(Constants.SERVICE_APP_NAME);
    }

    public static Application getApplication () {
        return AppGlobals.getInitialApplication();
    }

    public static PackageManager getPackageManager () {
        return AppGlobals.getInitialApplication().getPackageManager();
    }

    public static boolean isAppOpsInstalled () {
        return isAppInstalled("rikka.appops");
    }

    public static boolean isAppInstalled (String packageName) {
        try {
            return getPackageManager()
                    .getPackageInfo(packageName, 0)
                    != null;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static final String getXiaomiUserId(Context context) {
        @SuppressLint("MissingPermission") Account[] accounts = AccountManager.get(context).getAccounts();
        for (int i = 0; i < accounts.length; i++) {
            if (accounts[i].type.equals("com.xiaomi")) {
                String str = accounts[i].name;
                if (!str.trim().isEmpty()) {
                    return str;
                }
            }
        }
        return null;
    }

    @ColorInt
    public static int getColorAttr(Context context, int attr) {
        TypedArray ta = context.obtainStyledAttributes(new int[]{attr});
        @ColorInt int colorAccent = ta.getColor(0, 0);
        ta.recycle();
        return colorAccent;
    }

    public static void forceCustomPadding(View view, boolean additive) {
        final Resources res = view.getResources();
        final int paddingSide = res.getDimensionPixelSize(R.dimen.settings_side_margin);

        final int paddingStart = paddingSide + (additive ? view.getPaddingStart() : 0);
        final int paddingEnd = paddingSide + (additive ? view.getPaddingEnd() : 0);
        final int paddingBottom = res.getDimensionPixelSize(
                R.dimen.preference_fragment_padding_bottom);

        view.setPaddingRelative(paddingStart, 0, paddingEnd, paddingBottom);
    }


    public static Date getUTC (Date date) {
        Calendar cal = Calendar.getInstance() ;
        cal.setTime(date);
        int zoneOffset = cal.get(java.util.Calendar.ZONE_OFFSET);
        int dstOffset = cal.get(java.util.Calendar.DST_OFFSET);
        cal.add(java.util.Calendar.MILLISECOND, -(zoneOffset + dstOffset));
        return cal.getTime();
    }

    public static Date getUTC () {
        return getUTC(new Date());
    }

    public static CharSequence getString (@StringRes int id,
                                          @NonNull Context context,
                                          Object... formatArgs) {
        return Html.fromHtml(context.getString(id, formatArgs));
    }
}
