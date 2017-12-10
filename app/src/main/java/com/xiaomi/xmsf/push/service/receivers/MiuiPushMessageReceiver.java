package com.xiaomi.xmsf.push.service.receivers;

import android.content.Context;
import android.content.Intent;

import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.MiPushMessage;
import com.xiaomi.mipush.sdk.PushMessageReceiver;
import com.xiaomi.xmsf.push.service.MyLog;
import com.xiaomi.xmsf.push.service.XMAccountManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MiuiPushMessageReceiver extends PushMessageReceiver {
    private final Logger logger = LoggerFactory.getLogger(MiuiPushMessageReceiver.class);

    public void onCommandResult(Context context, MiPushCommandMessage miPushCommandMessage) {
        logger.debug("onCommandResult");
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
        logger.info("onReceiveMessage -> " + miPushMessage.toString());
        String str = (String) miPushMessage.getExtra().get("miui_package_name");
        if (str != null && !str.trim().isEmpty()) {
            logger.debug("not empty");
            Intent intent = new Intent();
            intent.setPackage(str);
            intent.putExtras(miPushMessage.toBundle());
            if (miPushMessage.isNotified()) {
                logger.debug("isNotified -> true");
                intent.setAction("com.xiaomi.mipush.miui.CLICK_MESSAGE");
                context.startService(intent);
                return;
            }
            logger.debug("send broadcast");
            intent.setAction("com.xiaomi.mipush.miui.RECEIVE_MESSAGE");
            context.sendBroadcast(intent);
        }
    }
}
