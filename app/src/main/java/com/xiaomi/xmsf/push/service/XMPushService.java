package com.xiaomi.xmsf.push.service;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Intent;

import com.xiaomi.xmsf.XmsfApp;

import me.pqpo.librarylog4a.Log4a;
import top.trumeet.mipushframework.Constants;
import top.trumeet.mipushframework.auth.AuthActivity;
import top.trumeet.mipushframework.event.Event;
import top.trumeet.mipushframework.event.EventDB;
import top.trumeet.mipushframework.push.PushController;
import top.trumeet.mipushframework.register.RegisterDB;
import top.trumeet.mipushframework.register.RegisteredApplication;

public class XMPushService extends IntentService {
    private static final String TAG = "XMPushService Bridge";
    
    public XMPushService() {
        super("XMPushService Bridge");
    }

    protected void onHandleIntent(Intent intent) {
        Log4a.d(TAG, "onHandleIntent -> A application want to register push");
        String pkg = intent.getStringExtra(Constants.EXTRA_MI_PUSH_PACKAGE);
        if (pkg == null) {
            Log4a.e(TAG, "Package name is NULL!");
            return;
        }
        int result;
        if (!PushController.isPrefsEnable(this)) {
            Log4a.e(TAG, "Not allowed in SP! Just return!");
            result = Event.ResultType.DENY_DISABLED;
        } else {
            RegisteredApplication application = RegisterDB.registerApplication(pkg,
                    true, this);
            if (application.getType() == RegisteredApplication.Type.DENY) {
                Log4a.w(TAG, "Denied register request: " + pkg);
                result = Event.ResultType.DENY_USER;
            } else {
                // Check multi request
                if (!XmsfApp.getSession(this)
                        .getRemoveTremblingInstance()
                        .onCallRegister(pkg)) {
                    Log4a.w(TAG, "Not allowed multi request");
                    return;
                }
                if (application.getType() == RegisteredApplication.Type.ASK) {
                    Log4a.d(TAG, "Starting auth");
                    Intent authIntent = new Intent(this, AuthActivity.class);
                    authIntent.putExtra(AuthActivity.EXTRA_REGISTERED_APPLICATION,
                            application);
                    authIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(authIntent);
                    // Don't save event there, auth activity will call back.
                    return;
                } else {
                    Log4a.d(TAG, "Allowed register request: " + pkg);
                    Intent intent2 = new Intent();
                    intent2.setComponent(new ComponentName(getPackageName(), "com.xiaomi.push.service.XMPushService"));
                    intent2.setAction(intent.getAction());
                    intent2.putExtras(intent);
                    startService(intent2);
                    if (application.getType() == RegisteredApplication.Type.ALLOW_ONCE) {
                        Log4a.w(TAG, "Return once to ask");
                        application.setType(RegisteredApplication.Type.ASK);
                        RegisterDB.update(application, this);
                    }
                    result = Event.ResultType.OK;
                }
            }
        }
        EventDB.insertEvent(pkg, Event.Type.REGISTER, result, this);
    }
}
