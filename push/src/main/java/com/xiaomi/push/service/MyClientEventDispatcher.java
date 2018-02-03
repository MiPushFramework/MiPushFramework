package com.xiaomi.push.service;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.xiaomi.slim.Blob;
import com.xiaomi.smack.packet.CommonPacketExtension;
import com.xiaomi.smack.packet.Message;
import com.xiaomi.smack.packet.Packet;
import com.xiaomi.smack.util.TrafficUtils;
import com.xiaomi.xmpush.thrift.XmPushActionContainer;
import com.xiaomi.xmsf.push.notification.OreoNotificationManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import me.pqpo.librarylog4a.Log4a;
import top.trumeet.common.BuildConfig;
import top.trumeet.common.db.EventDb;
import top.trumeet.common.db.RegisteredApplicationDb;
import top.trumeet.common.event.Event;
import top.trumeet.common.register.RegisteredApplication;

/**
 * Created by Trumeet on 2018/1/22.
 * 修改过的 ClientEventDispatcher，用于修改包接收处理逻辑
 *
 * 消息的处理：
 * 发送方（framework）：
 *
 * 广播 1： {@link PushConstants#MIPUSH_ACTION_MESSAGE_ARRIVED}
 * {@link MIPushEventProcessor} 负责将序列化后的消息广播/发送通知。
 * 具体可以看到 {@link MIPushEventProcessor#postProcessMIPushMessage(XMPushService, String, byte[], Intent, boolean)}
 * 里面的 170 行。它发送了 {@link PushConstants#MIPUSH_ACTION_MESSAGE_ARRIVED} 广播给客户端。
 *
 * 广播 2： {@link PushConstants#MIPUSH_ACTION_NEW_MESSAGE}；
 * 同样由 {@link MIPushEventProcessor} 发送。最初是在 {@link MIPushEventProcessor#buildIntent(byte[], long)} 中生成，由
 * {@link MIPushEventProcessor#postProcessMIPushMessage(XMPushService, String, byte[], Intent, boolean)} 中 192 行发送。
 *
 * 广播 3： {@link PushConstants#MIPUSH_ACTION_ERROR}
 * 由 {@link MIPushClientManager#notifyError} 发送。
 *
 * 客户端（接收方）：
 * 消息 intent 统一由 {@link com.xiaomi.mipush.sdk.PushMessageProcessor#processIntent} 处理。
 */

public class MyClientEventDispatcher extends ClientEventDispatcher {
    private static final String TAG = "MyClientEventDispatcher";
    public MyClientEventDispatcher () {
        try {
            // Patch mPushEventProcessor
            Field mPushEventProcessorField = ClientEventDispatcher.class
                    .getDeclaredField("mPushEventProcessor");
            mPushEventProcessorField.setAccessible(true);
            Object original = mPushEventProcessorField.get(this);
            if (original == null) {
                Log4a.e(TAG, "original is null, patch may not work.");
            }
            Log4a.d(TAG, "original: " + original);
            mPushEventProcessorField.set(this, new EventProcessor());
            Log4a.d(TAG, "Patch success.");
        } catch (Exception e) {
            Log4a.e(TAG, "*** Patch failed, core functions may not work.");
        }
    }

    @Override
    public void notifyPacketArrival(XMPushService xMPushService, String str, Packet packet) {
        Log4a.d(TAG, "packet arrival: " + str + "; " + packet.toXML());
        super.notifyPacketArrival(xMPushService, str, packet);
    }

    @Override
    public void notifyPacketArrival(XMPushService xMPushService, String str, Blob blob) {
        Log4a.d(TAG, "blob arrival: " + str + "; " + blob.toString());
        super.notifyPacketArrival(xMPushService, str, blob);
    }

    /**
     * 处理收到的消息
     */
    private static class MessageProcessor {
        private static boolean shouldAllow (String packageName, int type, String notificationTitle,
                String notificationSummary, Context context) {
            RegisteredApplication application = RegisteredApplicationDb.registerApplication(packageName,
                    false, context, null);
            if (application == null) {
                return false;
            }
            boolean allow;
            switch (type) {
                case Event.Type.RECEIVE_COMMAND:
                    allow = application.isAllowReceiveCommand();
                    break;
                case Event.Type.RECEIVE_PUSH:
                    allow = application.getAllowReceivePush();
                    break;
                default:
                    Log4a.e(TAG, "Unknown type: " + type);
                    return true;
            }
            EventDb.insertEvent(packageName, type, allow ? Event.ResultType.OK : Event.ResultType.DENY_USER
                    , notificationTitle, notificationSummary, context);
            return allow;
        }
    }

    private static class EventProcessor extends MIPushEventProcessor {
        private static void runProcessMIPushMessage(XMPushService var0, byte[] var1, long var2) {
            XmPushActionContainer buildContainer = buildContainer(var1);
            Log4a.i(TAG, "buildContainer: " + buildContainer.toString());
            //String targetPackage = MIPushNotificationHelper.getTargetPackage(buildContainer);
            if (BuildConfig.DEBUG) Log4a.d(TAG, "packageName: " + buildContainer.packageName
                    /*+ ", targetPackage: " +
            targetPackage*/);
            // 是否是透传
            boolean isPassThrough = buildContainer.getMetaInfo().passThrough == 1;
            if (PushConstants.PUSH_SERVICE_PACKAGE_NAME.equals(buildContainer.packageName) ||
                    MessageProcessor.shouldAllow(buildContainer.packageName, isPassThrough ? Event.Type.RECEIVE_COMMAND : Event.Type.RECEIVE_PUSH,
                    buildContainer.getMetaInfo().getTitle()
                            , buildContainer.getMetaInfo().getDescription()
                            , var0)) {

                if (Build.VERSION.SDK_INT >= 26 && !isPassThrough) {
                    XmPushActionContainer container = MIPushEventProcessor.buildContainer(var1);

                    // 获得通知的 id 来区分通知。
                    int id = container.getMetaInfo().getNotifyId()
                            + ((MIPushNotificationHelper.getTargetPackage(container).hashCode() / 10) * 10);
                    OreoNotificationManager manager = ((PushServiceMain) var0)
                            .getNotificationManager();
                    manager.register(id, buildContainer.packageName);
                }
                if (BuildConfig.DEBUG) Log4a.d(TAG, "invoke original method");
                doProcessMIPushMessage(var0, var1, var2);
            } else {
                if (BuildConfig.DEBUG) Log4a.d(TAG, "denied.");
            }
        }

        private static void doProcessMIPushMessage(XMPushService var0, byte[] var1, long var2) {
            try {
                Method method = MIPushEventProcessor.class.getDeclaredMethod("processMIPushMessage"
                        , XMPushService.class,
                        byte[].class, long.class);
                method.setAccessible(true);
                method.invoke(null, var0, var1, var2);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void processNewPacket(XMPushService xMPushService, Blob blob, PushClientsManager.ClientLoginInfo clientLoginInfo) {
            try {
                runProcessMIPushMessage(xMPushService, blob.getDecryptedPayload(clientLoginInfo.security), (long) blob.getSerializedSize());
            } catch (Throwable e) {
                Log4a.e(TAG, "", e);
            }
        }

        @Override
        public void processNewPacket(XMPushService xMPushService, Packet packet, PushClientsManager.ClientLoginInfo clientLoginInfo) {
            if (packet instanceof Message) {
                Message message = (Message) packet;
                CommonPacketExtension extension = message.getExtension("s");
                if (extension != null) {
                    try {
                        runProcessMIPushMessage(xMPushService, RC4Cryption.decrypt(RC4Cryption.generateKeyForRC4(clientLoginInfo.security, message.getPacketID()), extension.getText()), (long) TrafficUtils.getTrafficFlow(packet.toXML()));
                        return;
                    } catch (Throwable e) {
                        Log4a.e(TAG, "", e);
                        return;
                    }
                }
                return;
            }
            Log4a.e(TAG, "not a mipush message");
        }
    }
}
