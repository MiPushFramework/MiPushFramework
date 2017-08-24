package com.xiaomi.xmsf.push.service.receivers;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.MiPushMessage;
import com.xiaomi.mipush.sdk.PushMessageReceiver;
import com.xiaomi.xmsf.push.service.MyLog;
import com.xiaomi.xmsf.push.service.XMAccountManager;

public class MiuiPushMessageReceiver extends PushMessageReceiver {
    public void onCommandResult(Context context, MiPushCommandMessage miPushCommandMessage) {
        Log.d("MiuiPushMessageReceiver", "onCommandResult");
        if (miPushCommandMessage.getResultCode() == 0) {
            String command = miPushCommandMessage.getCommand();
            if (miPushCommandMessage.getCommandArguments().size() > 0 && "register".equals(command)) {
                XMAccountManager.getInstance(context).setAccountAsAlias();
                return;
            }
            return;
        }
        MyLog.m17e(miPushCommandMessage.toString());
    }

    public void onReceiveMessage(Context context, MiPushMessage miPushMessage) {
        String str = (String) miPushMessage.getExtra().get("miui_package_name");
        Log.d("MiuiPushMessageReceiver", "onReceiveMessage -> " + str);
        if (str != null && !str.trim().isEmpty()) {
            Intent intent = new Intent();
            intent.setPackage(str);
            intent.putExtras(miPushMessage.toBundle());
            if (miPushMessage.isNotified()) {
                intent.setAction("com.xiaomi.mipush.miui.CLICK_MESSAGE");
                context.startService(intent);
                return;
            }
            intent.setAction("com.xiaomi.mipush.miui.RECEIVE_MESSAGE");
            context.sendBroadcast(intent);
        }
    }
}
