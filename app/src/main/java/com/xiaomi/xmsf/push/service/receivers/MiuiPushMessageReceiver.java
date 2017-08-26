package com.xiaomi.xmsf.push.service.receivers;

import com.xiaomi.mipush.sdk.PushMessageReceiver;

public class MiuiPushMessageReceiver extends PushMessageReceiver {
    /*
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
        String str = miPushMessage.getExtra().get("miui_package_name");
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
    */
}
