package com.xiaomi.push.service;

import com.xiaomi.channel.commonutils.reflect.JavaCalls;
import com.xiaomi.smack.XMPPException;
import com.xiaomi.xmpush.thrift.PushMetaInfo;
import com.xiaomi.xmpush.thrift.XmPushActionContainer;

import java.util.Map;

import me.pqpo.librarylog4a.Log4a;

/**
 * @author zts
 */
class MiPushMsgAck {
    private static final String TAG = "MiPushMsgAck";


    static void sendAckMessage(final XMPushService var0, final XmPushActionContainer var1) {
        var0.executeJob(new XMPushService.Job(4) {
            @Override
            public String getDesc() {
                return "send ack message for message.";
            }

            @Override
            public void process() {
                try {
                    XmPushActionContainer var2 = MIPushEventProcessor.constructAckMessage(var0, var1);
                    MIPushHelper.sendPacket(var0, var2);
                } catch (XMPPException var3) {
                    Log4a.e(TAG, var3);
                    var0.disconnect(10, var3);
                }

            }
        });
    }

    /**
     * @see MIPushEventProcessor#sendAppAbsentAck
     */
    static void sendAppAbsentAck(final XMPushService var0, final XmPushActionContainer var1, final String var2) {
        try {
            JavaCalls.callStaticMethodOrThrow(MIPushEventProcessor.class, "sendAppAbsentAck", var0, var1, var2);
        } catch (Exception e) {
            Log4a.e(TAG, e.getMessage(), e);
        }
    }

    /**
     * @see MIPushEventProcessor#sendAppNotInstallNotification
     */
    static void sendAppNotInstallNotification(final XMPushService var0, final XmPushActionContainer var1) {
        try {
            JavaCalls.callStaticMethodOrThrow(MIPushEventProcessor.class, "sendAppNotInstallNotification", var0, var1);
        } catch (Exception e) {
            Log4a.e(TAG, e.getMessage(), e);
        }
    }

    /**
     * @see MIPushEventProcessor#sendErrorAck
     */
    static void sendErrorAck(final XMPushService var0, final XmPushActionContainer var1, final String var2, final String var3) {
        try {
            JavaCalls.callStaticMethodOrThrow(MIPushEventProcessor.class, "sendErrorAck", var0, var1, var2, var3);
        } catch (Exception e) {
            Log4a.e(TAG, e.getMessage(), e);
        }
    }

    static boolean verifyGeoMessage(Map<String, String> var0) {
        return var0 != null && var0.containsKey("__geo_ids");
    }

    /**
     * @see MIPushEventProcessor#geoMessageIsValidated
     */
    static boolean geoMessageIsValidated(XMPushService var0, XmPushActionContainer var1) {
        try {
            return JavaCalls.callStaticMethodOrThrow(MIPushEventProcessor.class, "geoMessageIsValidated", var0, var1);
        } catch (Exception e) {
            Log4a.e(TAG, e.getMessage(), e);
        }
        return false;
    }

    /**
     * @see MIPushEventProcessor#processGeoMessage
     */

    static boolean processGeoMessage(XMPushService var0, PushMetaInfo var1, byte[] var2) {
        try {
            return JavaCalls.callStaticMethodOrThrow(MIPushEventProcessor.class, "processGeoMessage", var0, var1, var2);
        } catch (Exception e) {
            Log4a.e(TAG, e.getMessage(), e);
        }
        return false;
    }

    /**
     * @see MIPushEventProcessor#shouldSendBroadcast
     */
    static boolean shouldSendBroadcast(XMPushService paramXMPushService, String paramString, XmPushActionContainer paramXmPushActionContainer, PushMetaInfo paramPushMetaInfo) {
        try {
            return JavaCalls.callStaticMethodOrThrow(MIPushEventProcessor.class, "shouldSendBroadcast", paramXMPushService, paramString, paramXmPushActionContainer, paramPushMetaInfo);
        } catch (Exception e) {
            Log4a.e(TAG, e.getMessage(), e);
        }
        return false;
    }
}
