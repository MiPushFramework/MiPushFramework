package com.xiaomi.xmsf.push.service;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;

import top.trumeet.mipushframework.Constants;
import top.trumeet.mipushframework.event.Event;
import top.trumeet.mipushframework.event.EventDB;
import top.trumeet.mipushframework.push.PushController;
import top.trumeet.mipushframework.register.RegisterDB;
import top.trumeet.mipushframework.register.RegisteredApplication;

import static top.trumeet.mipushframework.Constants.TAG;

public class XMPushService extends IntentService {
    public XMPushService() {
        super("XMPushService Bridge");
    }

    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent -> A application want to register push");
        String pkg = intent.getStringExtra(Constants.EXTRA_MI_PUSH_PACKAGE);
        if (pkg == null) {
            Log.e(TAG, "Package name is NULL!");
            return;
        }
        int result;
        if (!PushController.isPrefsEnable(this)) {
            Log.e(TAG, "Not allowed in SP! Just return!");
            result = Event.ResultType.DENY_DISABLED;
        } else {
            RegisteredApplication application = RegisterDB.registerApplication(pkg,
                    true, this);
            if (application.getType() == RegisteredApplication.Type.DENY) {
                Log.w(TAG, "Denied register request: " + pkg);
                result = Event.ResultType.DENY_USER;
            } else {
                // TODO: Ask mode
                Log.d(TAG, "Allowed register request: " + pkg);
                Intent intent2 = new Intent();
                intent2.setComponent(new ComponentName(getPackageName(), "com.xiaomi.push.service.XMPushService"));
                intent2.setAction(intent.getAction());
                intent2.putExtras(intent);
                startService(intent2);
                result = Event.ResultType.OK;
            }
        }
        EventDB.insertEvent(pkg, Event.Type.REGISTER, result, this);
    }
}
