package com.xiaomi.xmsf.push.service;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;

import com.crossbowffs.remotepreferences.RemotePreferenceAccessException;
import com.xiaomi.push.service.PushServiceMain;
import com.xiaomi.xmsf.XmsfApp;
import com.xiaomi.xmsf.push.auth.AuthActivity;
import com.xiaomi.xmsf.push.control.PushControllerUtils;
import com.xiaomi.xmsf.push.notification.NotificationController;

import me.pqpo.librarylog4a.Log4a;
import top.trumeet.common.Constants;
import top.trumeet.common.db.EventDb;
import top.trumeet.common.db.RegisteredApplicationDb;
import top.trumeet.common.event.Event;
import top.trumeet.common.register.RegisteredApplication;
import top.trumeet.common.utils.PreferencesUtils;

import static com.xiaomi.xmsf.XmsfApp.conf;

public class XMPushService extends IntentService {
    private static final String TAG = "XMPushService Bridge";

    public XMPushService() {
        super("XMPushService Bridge");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log4a.d(TAG, "onHandleIntent -> A application want to register push");
        String pkg = intent.getStringExtra(Constants.EXTRA_MI_PUSH_PACKAGE);
        if (pkg == null) {
            Log4a.e(TAG, "Package name is NULL!");
            return;
        }
        int result;
        boolean register = true;
        // Check multi request
        if (!XmsfApp.getSession(this)
                .getRemoveTremblingInstance()
                .onCallRegister(pkg)) {
            Log4a.w(TAG, "Don't register multi request " + pkg);
            register = false;
        }
        NotificationController.registerChannelIfNeeded(this, pkg);
        if (!PushControllerUtils.isPrefsEnable(this)) {
            Log4a.e(TAG, "Not allowed in SP! Just return!");
            result = Event.ResultType.DENY_DISABLED;
        } else {
            RegisteredApplication application = RegisteredApplicationDb
                    .registerApplication(pkg,
                            true, this, null);
            if (application.getType() == RegisteredApplication.Type.DENY) {
                Log4a.w(TAG, "Denied register request: " + pkg);
                result = Event.ResultType.DENY_USER;
            } else {
                if (XmsfApp.conf.autoRegister && application.getType() == RegisteredApplication.Type.ASK) {
                    application.setType(RegisteredApplication.Type.ALLOW);
                    RegisteredApplicationDb.update(application, this);
                }

                if (application.getType() == RegisteredApplication.Type.ASK) {
                    if (!register) {
                        return;
                    }
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
                    intent2.setComponent(new ComponentName(this, PushServiceMain.class));
                    intent2.setAction(intent.getAction());
                    intent2.putExtras(intent);
                    startService(intent2);
                    if (application.getType() == RegisteredApplication.Type.ALLOW_ONCE) {
                        Log4a.w(TAG, "Return once to ask");
                        application.setType(RegisteredApplication.Type.ASK);
                        RegisteredApplicationDb.update(application, this);
                    }
                    result = Event.ResultType.OK;
                }
            }
        }
        if (register) {
            EventDb.insertEvent(result, new top.trumeet.common.event.type.RegistrationType(null, pkg), this);
        }
    }

}
