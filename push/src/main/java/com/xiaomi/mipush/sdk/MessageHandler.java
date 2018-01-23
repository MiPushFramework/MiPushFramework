package com.xiaomi.mipush.sdk;

import android.annotation.Nullable;
import android.content.Intent;
import android.os.Bundle;

import com.xiaomi.push.service.PushConstants;
import com.xiaomi.xmsf.BuildConfig;

import me.pqpo.librarylog4a.Log4a;
import top.trumeet.common.db.EventDb;
import top.trumeet.common.db.RegisteredApplicationDb;
import top.trumeet.common.event.Event;
import top.trumeet.common.register.RegisteredApplication;

/**
 * Created by Trumeet on 2018/1/21.
 */

public class MessageHandler extends PushMessageHandler {
    private static final String TAG = "MessageHandler";

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Log4a.d(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
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
        if (ACTION_WAKEUP.equals(intent.getAction()))
            super.onHandleIntent(intent);
        else if (PushConstants.MIPUSH_ACTION_SEND_TINYDATA.equals(intent.getAction()))
            super.onHandleIntent(intent);
        else if (1 != PushMessageHelper.getPushMode(this))
            super.onHandleIntent(intent);
        else if (isCallbackEmpty())
            super.onHandleIntent(intent);
        else {
            // Add my custom process
            PushMessageInterface processIntent = PushMessageProcessor
                    .getInstance(this).processIntent(intent);
            if (processIntent != null) {
                final String targetPackage = intent.getStringExtra(PushConstants.MIPUSH_EXTRA_APP_PACKAGE);
                Log4a.d(TAG, "Get a message, pkg: " + targetPackage);
                // 模仿 processMessageForCallback 中的实现进行检测 / 拦截
                RegisteredApplication application = RegisteredApplicationDb
                        .registerApplication(targetPackage,
                                false, this, null);
                if (application == null) {
                    Log4a.w(TAG, "Not registered application: " + targetPackage);
                    return;
                }

                if(processIntent instanceof MiPushMessage) {
                    Log4a.i(TAG, "Notification message received: " + processIntent);
                    if (application.getAllowReceivePush()) {
                        processMessageForCallback(this, processIntent);
                        EventDb.insertEvent(targetPackage, Event.Type.RECEIVE_PUSH,
                                Event.ResultType.OK, this);
                    } else {
                        EventDb.insertEvent(targetPackage, Event.Type.RECEIVE_PUSH,
                                Event.ResultType.DENY_USER, this);
                    }
                } else if(processIntent instanceof MiPushCommandMessage) {
                    MiPushCommandMessage var2 = (MiPushCommandMessage)processIntent;
                    String var3 = var2.getCommand();
                    if("register".equals(var3)) {
                        // 注册结果回掉，不记录 Event
                        if (application.getAllowReceiveRegisterResult())
                            processMessageForCallback(this, processIntent);
                    } else {
                        // 普通命令
                        if (application.isAllowReceiveCommand()) {
                            processMessageForCallback(this, processIntent);
                            EventDb.insertEvent(targetPackage, Event.Type.RECEIVE_COMMAND,
                                    Event.ResultType.OK, this);
                        } else {
                            EventDb.insertEvent(targetPackage, Event.Type.RECEIVE_COMMAND,
                                    Event.ResultType.DENY_USER, this);
                        }
                    }
                } else {
                    Log4a.e(TAG, "Unknown push type: " + processIntent.getClass());
                }
                // processMessageForCallback(this, processIntent);
            }
        }
    }
}
