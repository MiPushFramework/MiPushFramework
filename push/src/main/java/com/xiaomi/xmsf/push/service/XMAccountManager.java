package com.xiaomi.xmsf.push.service;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.text.TextUtils;

import com.xiaomi.mipush.sdk.MiPushClient;

public class XMAccountManager {
    private static XMAccountManager sInstance = null;
    private Context mAppCtx;
    private String mUid;

    private XMAccountManager(Context context) {
        this.mAppCtx = context.getApplicationContext();
        if (this.mAppCtx == null) {
            this.mAppCtx = context;
        }
        this.mUid = "";
    }

    public static XMAccountManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new XMAccountManager(context);
        }
        return sInstance;
    }

    public void setAccountAsAlias() {
        String xiaomiUserId = getXiaomiUserId(this.mAppCtx);
        if ((TextUtils.isEmpty(this.mUid) && !TextUtils.isEmpty(xiaomiUserId)) || (!TextUtils.isEmpty(this.mUid) && !this.mUid.equals(xiaomiUserId))) {
            if (TextUtils.isEmpty(this.mUid)) {
                MiPushClient.setAlias(this.mAppCtx, xiaomiUserId, null);
            } else {
                MiPushClient.unsetAlias(this.mAppCtx, this.mUid, null);
            }
            this.mUid = xiaomiUserId;
        }
    }

    private String getXiaomiUserId(Context context) {
        Account[] accounts = AccountManager.get(context).getAccounts();
        for (Account account : accounts) {
            if (account.type.equals("com.xiaomi")) {
                String str = account.name;
                if (!str.trim().isEmpty()) {
                    return str;
                }
            }
        }
        return null;
    }

}
