package com.xiaomi.xmsf.push.service;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import com.elvishew.xlog.Logger;
import com.elvishew.xlog.XLog;
import com.xiaomi.push.service.PushServiceMain;
import com.xiaomi.xmsf.R;
import com.xiaomi.xmsf.push.auth.AuthActivity;
import com.xiaomi.xmsf.push.control.PushControllerUtils;
import com.xiaomi.xmsf.push.notification.NotificationController;
import com.xiaomi.xmsf.push.utils.RemoveTremblingUtils;
import com.xiaomi.xmsf.utils.ConfigCenter;
import top.trumeet.common.Constants;
import top.trumeet.common.cache.ApplicationNameCache;
import top.trumeet.common.db.EventDb;
import top.trumeet.common.db.RegisteredApplicationDb;
import top.trumeet.common.event.Event;
import top.trumeet.common.register.RegisteredApplication;

public class XMPushService extends IntentService {
    private static final String TAG = "XMPushService Bridge";
    private final Logger logger = XLog.tag(TAG).build();

    public XMPushService() {
        super("XMPushService Bridge");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            logger.d("onHandleIntent -> A application want to register push");
            String pkg = intent.getStringExtra(Constants.EXTRA_MI_PUSH_PACKAGE);
            if (pkg == null) {
                logger.e("Package name is NULL!");
                return;
            }
            int result;
            boolean register = true;
            // Check multi request
            if (!RemoveTremblingUtils.getInstance().onCallRegister(pkg)) {
                logger.d("Don't register multi request " + pkg);
                register = false;
            }
            NotificationController.registerChannelIfNeeded(this, pkg);
            RegisteredApplication application = RegisteredApplicationDb
                    .registerApplication(pkg, true, this, null);
            if (!PushControllerUtils.isPrefsEnable(this)) {
                logger.e("Not allowed in SP! Just return!");
                result = Event.ResultType.DENY_DISABLED;
            } else {
                if (application == null) {
                    logger.w("registerApplication failed " + pkg);
                    return;
                }
                if (application.getType() == RegisteredApplication.Type.DENY) {
                    logger.w("Denied register request: " + pkg);
                    result = Event.ResultType.DENY_USER;
                } else {
                    if (ConfigCenter.getInstance().isAutoRegister(this) && application.getType() == RegisteredApplication.Type.ASK) {
                        application.setType(RegisteredApplication.Type.ALLOW);
                        RegisteredApplicationDb.update(application, this);
                    }

                    if (application.getType() == RegisteredApplication.Type.ASK) {
                        if (!register) {
                            return;
                        }
                        logger.d("Starting auth");
                        Intent authIntent = new Intent(this, AuthActivity.class);
                        authIntent.putExtra(AuthActivity.EXTRA_REGISTERED_APPLICATION,
                                application);
                        authIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(authIntent);
                        // Don't save event there, auth activity will call back.
                        return;
                    } else {
                        logger.d("Allowed register request: " + pkg);
                        Intent intent2 = new Intent();
                        intent2.setComponent(new ComponentName(this, PushServiceMain.class));
                        intent2.setAction(intent.getAction());
                        intent2.putExtras(intent);
	                    ContextCompat.startForegroundService(this, intent2);
                        if (application.getType() == RegisteredApplication.Type.ALLOW_ONCE) {
                            logger.w("Return once to ask");
                            application.setType(RegisteredApplication.Type.ASK);
                            RegisteredApplicationDb.update(application, this);
                        }
                        result = Event.ResultType.OK;
                    }
                }
            }
            if (register) {
                boolean notificationOnRegister = ConfigCenter.getInstance().isNotificationOnRegister(this);
                notificationOnRegister = notificationOnRegister && (application != null && application.isNotificationOnRegister());
                if (notificationOnRegister) {
                    CharSequence appName = ApplicationNameCache.getInstance().getAppName(this, pkg);
                    CharSequence usedString;
                    switch (result) {
                        case Event.ResultType.OK:
                            usedString = getString(R.string.notification_registerAllowed, appName);
                            break;
                        case Event.ResultType.DENY_DISABLED:
                            usedString = null; // Should not be notified?
                            break;
                        case Event.ResultType.DENY_USER:
                            usedString = getString(R.string.notification_registerRejected, appName);
                            break;
                        default:
                            usedString = null;
                            break;
                    }
                    if (usedString != null) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            try {
                                Toast.makeText(this, usedString,
                                        Toast.LENGTH_SHORT)
                                        .show();
                            } catch (Throwable ignored) {
                                // TODO: It's a bad way to switch to main thread.
                                // Ignored service death
                            }
                        });
                    }
                } else {
                    Log.e("XMPushService Bridge", "Notification disabled");
                }
                EventDb.insertEvent(result, new top.trumeet.common.event.type.RegistrationType(null, pkg), this);
            }
        } catch (RuntimeException e) {
            logger.e("XMPushService::onHandleIntent: ", e);
            Toast.makeText(this, getString(R.string.common_err, e.getMessage()), Toast.LENGTH_SHORT).show();
        }
    }

}
