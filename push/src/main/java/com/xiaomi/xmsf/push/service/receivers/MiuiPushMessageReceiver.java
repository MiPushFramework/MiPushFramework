package com.xiaomi.xmsf.push.service.receivers;

import android.content.Context;
import android.content.Intent;

import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.MiPushMessage;
import com.xiaomi.mipush.sdk.PushMessageReceiver;
import com.xiaomi.xmsf.push.service.MyLog;
import com.xiaomi.xmsf.push.service.XMAccountManager;

import me.pqpo.librarylog4a.Log4a;

public class MiuiPushMessageReceiver extends PushMessageReceiver {
    private static final String TAG = MiuiPushMessageReceiver.class.getSimpleName();
    public void onCommandResult(Context context, MiPushCommandMessage miPushCommandMessage) {
        Log4a.d(TAG, "onCommandResult");
        Log4a.d(TAG, miPushCommandMessage.toString());
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
        Log4a.i(TAG, "onReceiveMessage -> " + miPushMessage.toString());
        String str = (String) miPushMessage.getExtra().get("miui_package_name");
        if (str != null && !str.trim().isEmpty()) {
            Log4a.d(TAG, "not empty");
            Intent intent = new Intent();
            intent.setPackage(str);
            intent.putExtras(miPushMessage.toBundle());
            if (miPushMessage.isNotified()) {
                Log4a.d(TAG, "isNotified -> true");
                intent.setAction("com.xiaomi.mipush.miui.CLICK_MESSAGE");
                context.startService(intent);
                return;
            }
            Log4a.d(TAG, "send broadcast");
            intent.setAction("com.xiaomi.mipush.miui.RECEIVE_MESSAGE");
            context.sendBroadcast(intent);
        }
    }
}
