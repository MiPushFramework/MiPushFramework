package com.xiaomi.xmsf.push.control;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.oasisfeng.condom.CondomOptions;
import com.oasisfeng.condom.OutboundJudge;
import com.oasisfeng.condom.OutboundType;
import com.oasisfeng.condom.kit.NullDeviceIdKit;

import me.pqpo.librarylog4a.Log4a;

/**
 * Created by Trumeet on 2018/1/19.
 */

public class XMOutbound implements OutboundJudge {
    private final String TAG;
    private final Context context;

    private XMOutbound (Context context, String tag) {
        this.context = context;
        this.TAG = tag;
    }

    public static CondomOptions create (Context context, String tag,
                                        boolean enableKit) {
        CondomOptions options = new CondomOptions()
                .preventBroadcastToBackgroundPackages(false)
                .setOutboundJudge(new XMOutbound(context, tag));
        if (enableKit)
            options.addKit(new NullDeviceIdKit())
                    .addKit(new AppOpsKit())
                .addKit(new NotificationManagerKit())
                    ;
        return options;
    }

    public static CondomOptions create (Context context, String tag) {
        return create(context, tag, true);
    }



    @Override
    public boolean shouldAllow(OutboundType type, @Nullable Intent intent, String target_package) {
        Log4a.d(TAG, "shouldAllow ->" + type.toString());
        if (type == OutboundType.START_SERVICE ||
                type == OutboundType.BIND_SERVICE) {
            Log4a.i(TAG, "Allowed start or bind service: " + intent);
            return true;
        }
        if (type == OutboundType.BROADCAST) {
            if (intent == null) {
                Log4a.e(TAG,  "Not allowed broadcast with null intent: " + target_package);
                return false;
            }

            /*
            if (intent.getAction().equals(Constants.ACTION_MESSAGE_ARRIVED) ||
                    intent.getAction().equals(Constants.ACTION_ERROR) ||
                    intent.getAction().equals(Constants.ACTION_RECEIVE_MESSAGE)) {
                Log4a.d(TAG, "Handle message broadcast: " + intent + ", " +
                        target_package);
                RegisteredApplication application = RegisteredApplicationDb
                        .registerApplication(target_package,
                                false, context, null);
                if (application == null) {
                    Log4a.w(TAG, "Not registered application: " + target_package);
                    return true;
                }
                if (BuildConfig.DEBUG) {
                    // TODO: Always false?
                    Log4a.d(TAG, "hasExtra: " +
                            intent.hasExtra(Constants.EXTRA_MESSAGE_TYPE));
                }
                int messageType = intent.getIntExtra(Constants.EXTRA_MESSAGE_TYPE
                        , Constants.MESSAGE_TYPE_PUSH);
                Log4a.d(TAG, "messageType: " + messageType);
                switch (messageType) {
                    case Constants.MESSAGE_TYPE_PUSH:
                        if (application.getAllowReceivePush()) {
                            Log4a.i(TAG, "Allow message");
                            // Try add flags?
                            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                            intent.setPackage(target_package);
                            EventDb.insertEvent(target_package, Event.Type.RECEIVE_PUSH,
                                    Event.ResultType.OK, context);
                            return true;
                        } else {
                            Log4a.w(TAG, "Not allow message");
                            EventDb.insertEvent(target_package, Event.Type.RECEIVE_PUSH,
                                    Event.ResultType.DENY_USER, context);
                            return false;
                        }
                    case Constants.MESSAGE_TYPE_REGISTER_RESULT:
                        if (application.getAllowReceiveRegisterResult()) {
                            Log4a.i(TAG, "Allow callback register result");
                            // Try add flags?
                            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                            return true;
                        } else {
                            Log4a.w(TAG, "Not allow callback register result");
                            return false;
                        }
                }
                Log4a.e(TAG,  "Not allowed broadcast: " + intent);
                return false;
            }
            */
        }

        // Deny something will crash...
        Log4a.w(TAG, "Allowed: " + intent + ", pkg=" + target_package);
        return true;
    }
}
