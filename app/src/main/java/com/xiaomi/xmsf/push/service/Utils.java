package com.xiaomi.xmsf.push.service;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

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
}
