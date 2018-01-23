package com.xiaomi.mipush.sdk;

import android.annotation.Nullable;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.xiaomi.xmsf.BuildConfig;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import me.pqpo.librarylog4a.Log4a;

/**
 * Created by Trumeet on 2018/1/22.
 */

public class MyMessageHandleService extends MessageHandleService {
    private static final String TAG = "MyMessageHandleService";

    @Nullable
    private static MessageHandleJob poll () {
        try {
            Field queue = MessageHandleService.class.getDeclaredField("jobQueue");
            queue.setAccessible(true);
            Object queueObj = queue.get(null);
            Method pollMethod = queueObj.getClass().getDeclaredMethod("poll");
            return (MessageHandleJob) pollMethod.invoke(queueObj);
        } catch (Exception e) {
            Log4a.e(TAG, "Unable to poll", e);
            return null;
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log4a.d(TAG, "action: " + ((intent == null || intent.getAction() == null) ?
                "(Null)" : intent.getAction()));
        if (BuildConfig.DEBUG && intent != null) {
            Log4a.d(TAG, intent.toString());
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                for (String key : bundle.keySet()) {
                    Object value = bundle.get(key);
                    Log4a.d(TAG, String.format("%s %s (%s)", key,
                            value != null ? value.toString() :
                                    "(Null)", value != null ? value.getClass().getName()
                                    : "(Null)"));
                }
            }
        }
        if (intent != null) {
            MessageHandleJob messageHandleJob = poll();
            if (messageHandleJob != null) {
                try {
                    PushMessageReceiver receiver = messageHandleJob.getReceiver();
                    Intent intent2 = messageHandleJob.getIntent();
                    MiPushCommandMessage miPushCommandMessage;
                    switch (intent2.getIntExtra(PushMessageHelper.MESSAGE_TYPE, 1)) {
                        case 1:
                            PushMessageHandler.PushMessageInterface processIntent = PushMessageProcessor.getInstance(this).processIntent(intent2);
                            if (processIntent == null) {
                                return;
                            }
                            if (processIntent instanceof MiPushMessage) {
                                MiPushMessage miPushMessage = (MiPushMessage) processIntent;
                                if (!miPushMessage.isArrivedMessage()) {
                                    receiver.onReceiveMessage(this, miPushMessage);
                                }
                                if (miPushMessage.getPassThrough() == 1) {
                                    receiver.onReceivePassThroughMessage(this, miPushMessage);
                                    return;
                                } else if (miPushMessage.isNotified()) {
                                    receiver.onNotificationMessageClicked(this, miPushMessage);
                                    return;
                                } else {
                                    receiver.onNotificationMessageArrived(this, miPushMessage);
                                    return;
                                }
                            } else if (processIntent instanceof MiPushCommandMessage) {
                                miPushCommandMessage = (MiPushCommandMessage) processIntent;
                                receiver.onCommandResult(this, miPushCommandMessage);
                                if (TextUtils.equals(miPushCommandMessage.getCommand(), MiPushClient.COMMAND_REGISTER)) {
                                    receiver.onReceiveRegisterResult(this, miPushCommandMessage);
                                    return;
                                }
                                return;
                            } else {
                                return;
                            }
                        case 3:
                            miPushCommandMessage = (MiPushCommandMessage) intent2.getSerializableExtra(PushMessageHelper.KEY_COMMAND);
                            receiver.onCommandResult(this, miPushCommandMessage);
                            if (TextUtils.equals(miPushCommandMessage.getCommand(), MiPushClient.COMMAND_REGISTER)) {
                                receiver.onReceiveRegisterResult(this, miPushCommandMessage);
                                return;
                            }
                            return;
                        case 4:
                            return;
                        default:
                            return;
                    }
                } catch (Throwable e) {
                    Log4a.e(TAG, "", e);
                }
            }
        }
    }
}
