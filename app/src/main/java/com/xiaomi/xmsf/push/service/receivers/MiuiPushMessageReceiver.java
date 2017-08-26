package com.xiaomi.xmsf.push.service.receivers;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.MiPushMessage;
import com.xiaomi.mipush.sdk.PushMessageReceiver;
import com.xiaomi.xmsf.push.service.MyLog;
import com.xiaomi.xmsf.push.service.XMAccountManager;

import top.trumeet.mipushframework.event.Event;
import top.trumeet.mipushframework.event.EventDB;
import top.trumeet.mipushframework.event.notification.NotificationInfo;

import static top.trumeet.mipushframework.Constants.TAG;

// TODO: May not working? XMPush sdk will send broadcast to target application..
public class MiuiPushMessageReceiver extends PushMessageReceiver {
    public void onCommandResult(Context context, MiPushCommandMessage miPushCommandMessage) {
        Log.d(TAG, "onCommandResult");
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
        Log.d(TAG, "onReceiveMessage -> " + str);
        if (str != null && !str.trim().isEmpty()) {
            EventDB.insertEvent(str, Event.Type.PUSH_MESSAGE,
                    Event.ResultType.OK,
                    new NotificationInfo(null, miPushMessage.getTitle(),
                            miPushMessage.getContent(),
                            false), context);
            Intent intent = new Intent();
            intent.setPackage(str);
            intent.putExtras(miPushMessage.toBundle());
            if (miPushMessage.isNotified()) {
                intent.setAction("com.xiaomi.mipush.miui.CLICK_MESSAGE");
                context.startService(intent);
                return;
            }
            intent.setAction("com.xiaomi.mipush.miui.RECEIVE_MESSAGE");
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            context.sendBroadcast(intent);
        }
    }
}
