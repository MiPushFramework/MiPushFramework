package com.xiaomi.xmsf.push.service;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.xiaomi.push.service.PushServiceMain;
import com.xiaomi.xmsf.R;
import com.xiaomi.xmsf.push.auth.AuthActivity;
import com.xiaomi.xmsf.push.control.PushControllerUtils;
import com.xiaomi.xmsf.push.notification.NotificationController;
import com.xiaomi.xmsf.push.utils.RemoveTremblingUtils;
import com.xiaomi.xmsf.utils.ConfigCenter;

import me.pqpo.librarylog4a.Log4a;
import top.trumeet.common.Constants;
import top.trumeet.common.db.EventDb;
import top.trumeet.common.db.RegisteredApplicationDb;
import top.trumeet.common.event.Event;
import top.trumeet.common.register.RegisteredApplication;

import static com.xiaomi.xmsf.BuildConfig.DEBUG;

public class XMPushService extends IntentService {
    private static final String TAG = "XMPushService Bridge";

    public XMPushService() {
        super("XMPushService Bridge");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            Log4a.d(TAG, "onHandleIntent -> A application want to register push");
            String pkg = intent.getStringExtra(Constants.EXTRA_MI_PUSH_PACKAGE);
            if (pkg == null) {
                Log4a.e(TAG, "Package name is NULL!");
                return;
            }
            int result;
            boolean register = true;
            // Check multi request
            if (!RemoveTremblingUtils.getInstance().onCallRegister(pkg)) {
                Log4a.d(TAG, "Don't register multi request " + pkg);
                register = false;
            }
            NotificationController.registerChannelIfNeeded(this, pkg);
            RegisteredApplication application = RegisteredApplicationDb
                    .registerApplication(pkg, true, this, null);
            if (!PushControllerUtils.isPrefsEnable(this)) {
                Log4a.e(TAG, "Not allowed in SP! Just return!");
                result = Event.ResultType.DENY_DISABLED;
            } else {
                if (application == null) {
                    if (!DEBUG) Crashlytics.log(Log.WARN, TAG, "registerApplication failed " + pkg);
                    Log4a.w(TAG, "registerApplication failed " + pkg);
                    return;
                }
                if (application.getType() == RegisteredApplication.Type.DENY) {
                    Log4a.w(TAG, "Denied register request: " + pkg);
                    result = Event.ResultType.DENY_USER;
                } else {
                    if (ConfigCenter.isAutoRegister(this) && application.getType() == RegisteredApplication.Type.ASK) {
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
                if (application != null && application.isNotificationOnRegister()) {
                    try {
                        CharSequence appName = getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(pkg, 0));
                        CharSequence typeString;
                        switch (result) {
                            case Event.ResultType.OK:
                            case Event.ResultType.DENY_DISABLED:
                                typeString = getString(R.string.allow);
                                break;
                            case Event.ResultType.DENY_USER:
                                typeString = getString(R.string.deny);
                                break;
                            default:
                                typeString = null;
                                break;
                        }
                        if (typeString != null) {
                            new Handler(Looper.getMainLooper()).post(() -> {
                                try {
                                    Toast.makeText(this, getString(R.string.notification_register,
                                            appName, typeString),
                                            Toast.LENGTH_SHORT)
                                            .show();
                                } catch (Throwable ignored) {
                                    // TODO: It's a bad way to switch to main thread.
                                    // Ignored service death
                                }
                            });
                        }
                    } catch (PackageManager.NameNotFoundException ignored) {}
                } else {
                    Log.e("XMPushService Bridge", "Notification disabled");
                }
                EventDb.insertEvent(result, new top.trumeet.common.event.type.RegistrationType(null, pkg), this);
            }
        } catch (RuntimeException e) {
            Log4a.e(TAG, "XMPushService::onHandleIntent: ", e);
            Toast.makeText(this, getString(R.string.common_err, e.getMessage()), Toast.LENGTH_SHORT).show();
        }
    }

}
