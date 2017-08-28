package com.xiaomi.xmsf.push.service.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.xiaomi.xmsf.push.service.MiuiPushActivateService;
import com.xiaomi.xmsf.push.service.XMAccountManager;

public class AccountChangedReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction().equals("android.accounts.LOGIN_ACCOUNTS_CHANGED")) {
            XMAccountManager.getInstance(context).setAccountAsAlias();
            MiuiPushActivateService.awakePushActivateService(context, "com.xiaomi.xmsf.push.ACCOUNT_CHANGE");
        }
    }
}
