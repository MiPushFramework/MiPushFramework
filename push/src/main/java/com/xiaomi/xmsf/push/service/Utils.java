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


}
