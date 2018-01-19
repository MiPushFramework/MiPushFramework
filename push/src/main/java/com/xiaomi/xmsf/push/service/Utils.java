package com.xiaomi.xmsf.push.service;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.support.annotation.ColorInt;
import android.view.View;

import com.xiaomi.xmsf.R;

public final class Utils {
    public static final String getXiaomiUserId(Context context) {
        Account[] accounts = AccountManager.get(context).getAccounts();
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
}
