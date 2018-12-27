package com.xiaomi.xmsf.push.service.receivers;

import android.content.Context;
import android.content.Intent;

import com.elvishew.xlog.Logger;
import com.elvishew.xlog.XLog;
import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.MiPushMessage;
import com.xiaomi.mipush.sdk.PushMessageReceiver;
import com.xiaomi.xmsf.push.service.XMAccountManager;



public class MiuiPushMessageReceiver extends PushMessageReceiver {
    private final Logger logger = XLog.tag(MiuiPushMessageReceiver.class.getSimpleName()).build();
    public void onCommandResult(Context context, MiPushCommandMessage miPushCommandMessage) {
        logger.d("onCommandResult");
        logger.d(miPushCommandMessage.toString());
        if (miPushCommandMessage.getResultCode() == 0) {
            String command = miPushCommandMessage.getCommand();
            if (miPushCommandMessage.getCommandArguments().size() > 0 && "register".equals(command)) {
                XMAccountManager.getInstance(context).setAccountAsAlias();
                return;
            }
            return;
        }
        logger.e(miPushCommandMessage.toString());
    }

    public void onReceiveMessage(Context context, MiPushMessage miPushMessage) {
        logger.i("onReceiveMessage -> " + miPushMessage.toString());
        String str = (String) miPushMessage.getExtra().get("miui_package_name");
        if (str != null && !str.trim().isEmpty()) {
            logger.d("not empty");
            Intent intent = new Intent();
            intent.setPackage(str);
            intent.putExtras(miPushMessage.toBundle());
            if (miPushMessage.isNotified()) {
                logger.d("isNotified -> true");
                intent.setAction("com.xiaomi.mipush.miui.CLICK_MESSAGE");
                context.startService(intent);
                return;
            }
            logger.d("send broadcast");
            intent.setAction("com.xiaomi.mipush.miui.RECEIVE_MESSAGE");
            context.sendBroadcast(intent);
        }
    }
}
