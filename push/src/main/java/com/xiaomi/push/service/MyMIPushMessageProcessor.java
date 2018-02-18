package com.xiaomi.push.service;

import android.accounts.Account;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;

import com.xiaomi.channel.commonutils.android.AppInfoUtils;
import com.xiaomi.channel.commonutils.android.MIIDUtils;
import com.xiaomi.channel.commonutils.logger.MyLog;
import com.xiaomi.channel.commonutils.reflect.JavaCalls;
import com.xiaomi.smack.XMPPException;
import com.xiaomi.xmpush.thrift.ActionType;
import com.xiaomi.xmpush.thrift.PushMetaInfo;
import com.xiaomi.xmpush.thrift.XmPushActionContainer;

import java.util.List;
import java.util.Map;

import me.pqpo.librarylog4a.Log4a;


/**
 * Created by zts1993 on 2018/2/8.
 */

public class MyMIPushMessageProcessor {
    private static final String TAG = "MyMIPushMessageProcessor";

    public static void process(XMPushService paramXMPushService, XmPushActionContainer buildContainer, byte[] paramArrayOfByte, long var2, Intent localIntent) {
        try {
            String targetPackage = MIPushNotificationHelper.getTargetPackage(buildContainer);
            Long current = System.currentTimeMillis();
//            Intent var6 = MIPushEventProcessor.buildIntent(var1, current);
            PushMetaInfo localPushMetaInfo = buildContainer.getMetaInfo();
            if (localPushMetaInfo != null) {
                localPushMetaInfo.putToExtra("mrt", Long.toString(current));
            }
            if (ActionType.SendMessage == buildContainer.getAction() && MIPushAppInfo.getInstance(paramXMPushService).isUnRegistered(buildContainer.packageName) && !MIPushNotificationHelper.isBusinessMessage(buildContainer)) {
                String var20 = "";
                if (localPushMetaInfo != null) {
                    var20 = localPushMetaInfo.getId();
                }

                Log4a.w(TAG, "Drop a message for unregistered, msgid=" + var20);
                sendAppAbsentAck(paramXMPushService, buildContainer, buildContainer.packageName);
            } else if (ActionType.SendMessage == buildContainer.getAction() && MIPushAppInfo.getInstance(paramXMPushService).isPushDisabled4User(buildContainer.packageName) && !MIPushNotificationHelper.isBusinessMessage(buildContainer)) {
                String var19 = "";
                if (localPushMetaInfo != null) {
                    var19 = localPushMetaInfo.getId();
                }

                Log4a.w(TAG, "Drop a message for push closed, msgid=" + var19);
                sendAppAbsentAck(paramXMPushService, buildContainer, buildContainer.packageName);
            } else if (ActionType.SendMessage == buildContainer.getAction() && !TextUtils.equals(paramXMPushService.getPackageName(), "com.xiaomi.xmsf") && !TextUtils.equals(paramXMPushService.getPackageName(), buildContainer.packageName)) {
                Log4a.w(TAG, "Receive a message with wrong package name, expect " + paramXMPushService.getPackageName() + ", received " + buildContainer.packageName);
                sendErrorAck(paramXMPushService, buildContainer, "unmatched_package", "package should be " + paramXMPushService.getPackageName() + ", but got " + buildContainer.packageName);
            } else {
                if (localPushMetaInfo != null && localPushMetaInfo.getId() != null) {
                    Log4a.i(TAG, String.format("receive a message, appid=%s, msgid= %s", buildContainer.getAppid(), localPushMetaInfo.getId()));
                }

                if (localPushMetaInfo != null) {
                    Map<String, String> var17 = localPushMetaInfo.getExtra();
                    if (var17 != null && var17.containsKey("hide") && "true".equalsIgnoreCase(var17.get("hide"))) {
                        Log4a.i(TAG, String.format("hide a message, appid=%s, msgid= %s", buildContainer.getAppid(), localPushMetaInfo.getId()));
                        sendAckMessage(paramXMPushService, buildContainer);
                        return;
                    }
                }

                if ((localPushMetaInfo != null) && (localPushMetaInfo.getExtra() != null) && (localPushMetaInfo.getExtra().containsKey("__miid"))) {
                    String str2 = localPushMetaInfo.getExtra().get("__miid");
                    Account localAccount = MIIDUtils.getXiaomiAccount(paramXMPushService);
                    String oldAccount = "";
                    if (localAccount == null) {
                        // xiaomi account login ?
                        oldAccount = "nothing";
                    } else {
                        if (TextUtils.equals(str2, localAccount.name)) {

                        } else {
                            oldAccount = localAccount.name;
                            Log4a.w(TAG, str2 + " should be login, but got " + localAccount);
                        }
                    }

                    if (!oldAccount.isEmpty()) {
                        Log4a.w(TAG, "miid already logout or anther already login :" + oldAccount);
                        sendErrorAck(paramXMPushService, buildContainer, "miid already logout or anther already login", oldAccount);
                    }
                }

                boolean isGeoMessage = (localPushMetaInfo != null && verifyGeoMessage(localPushMetaInfo.getExtra()));
                if (isGeoMessage) {
                    if (!geoMessageIsValidated(paramXMPushService, buildContainer)) {
                        return;
                    }

                    boolean var10 = processGeoMessage(paramXMPushService, localPushMetaInfo, paramArrayOfByte);
                    MIPushEventProcessor.sendGeoAck(paramXMPushService, buildContainer, true, false, false);
                    if (!var10) {
                        return;
                    }
                }

                userProcessMIPushMessage(paramXMPushService, buildContainer, paramArrayOfByte, var2, localIntent, isGeoMessage);
            }


        } catch (RuntimeException e2) {
            Log4a.e(TAG, "fallbackProcessMIPushMessage failed at" + System.currentTimeMillis(), e2);
        }
    }


    private static void userProcessMIPushMessage(XMPushService paramXMPushService, XmPushActionContainer buildContainer, byte[] paramArrayOfByte, long var2, Intent paramIntent, boolean isGeoMessage) {
        //var5 buildContainer
        //var6 metaInfo

//        if ((!MIPushNotificationHelper.isBusinessMessage(buildContainer) || !AppInfoUtils.isPkgInstalled(paramXMPushService, buildContainer.packageName))) {
//            if (!AppInfoUtils.isPkgInstalled(paramXMPushService, buildContainer.packageName)) {
//                sendAppNotInstallNotification(paramXMPushService, buildContainer);
//            } else {
//                Log4a.w(TAG, "receive a mipush message, we can see the app " + buildContainer.packageName+ ", but we can't see the receiver.");
//            }
//        }

        String paramString = MIPushNotificationHelper.getTargetPackage(buildContainer);

        if (MIPushNotificationHelper.isBusinessMessage(buildContainer) && AppInfoUtils.isPkgInstalled(paramXMPushService, buildContainer.packageName)) {
            if (ActionType.Registration == buildContainer.getAction()) {
                String str2 = buildContainer.getPackageName();
                SharedPreferences.Editor localEditor = paramXMPushService.getSharedPreferences("pref_registered_pkg_names", 0).edit();
                localEditor.putString(str2, buildContainer.appid);
                localEditor.commit();
                com.xiaomi.tinyData.TinyDataManager.getInstance(paramXMPushService).processPendingData("Register Success, package name is " + str2);
            }
        }

        PushMetaInfo metaInfo = buildContainer.getMetaInfo();

        if ("com.xiaomi.xmsf".contains(buildContainer.packageName) && !buildContainer.isEncryptAction() &&
                metaInfo != null && metaInfo.getExtra() != null && metaInfo.getExtra().containsKey("ab")) {
            sendAckMessage(paramXMPushService, buildContainer);
            MyLog.v("receive abtest message. ack it." + metaInfo.getId());
        }

        if (shouldSendBroadcast(paramXMPushService, paramString, buildContainer, metaInfo)) {
            paramXMPushService.sendBroadcast(paramIntent, ClientEventDispatcher.getReceiverPermission(buildContainer.packageName));
        }

        String title = metaInfo.getTitle();
        String description = metaInfo.getDescription();

        if (TextUtils.isEmpty(title)) {
            return;
//            metaInfo.setTitle(buildContainer.packageName);
        }

        if (TextUtils.isEmpty(description)) {
            metaInfo.setDescription("穿透消息，请打开APP查看");
        }


//        MIPushNotificationHelper.isNotifyForeground(metaInfo.getExtra());
//        MIPushNotificationHelper.isApplicationForeground(paramXMPushService, buildContainer.packageName);

        String var8 = null;
        if (metaInfo.extra != null) {
            var8 = metaInfo.extra.get("jobkey");
        }
        if (TextUtils.isEmpty(var8)) {
            var8 = metaInfo.getId();
        }
        boolean var7 = MiPushMessageDuplicate.isDuplicateMessage(paramXMPushService, buildContainer.packageName, var8);
        if (var7) {
            Log4a.w(TAG, "drop a duplicate message, key=" + var8);
        } else {
            if (isGeoMessage) {
                MIPushEventProcessor.sendGeoAck(paramXMPushService, buildContainer, false, true, false);
            }

            //NotifyPushMessageInfo var9 = MIPushNotificationHelper.notifyPushMessage(paramXMPushService, var5, var2);
            MyMIPushNotificationHelper.notifyPushMessage(paramXMPushService, buildContainer, paramArrayOfByte, var2);
            if (!MIPushNotificationHelper.isBusinessMessage(buildContainer)) {
                Intent localIntent = new Intent("com.xiaomi.mipush.MESSAGE_ARRIVED");
                localIntent.putExtra("mipush_payload", paramArrayOfByte);
                localIntent.setPackage(buildContainer.packageName);
                try {
                    List<ResolveInfo> localList = paramXMPushService.getPackageManager().queryBroadcastReceivers(localIntent, 0);
                    if ((localList != null) && (!localList.isEmpty())) {
                        paramXMPushService.sendBroadcast(localIntent, ClientEventDispatcher.getReceiverPermission(buildContainer.getPackageName()));
                    }
                } catch (Exception localException) {
                    paramXMPushService.sendBroadcast(localIntent, ClientEventDispatcher.getReceiverPermission(buildContainer.getPackageName()));
                }
            }

        }

        sendAckMessage(paramXMPushService, buildContainer);
    }


    private static void sendAckMessage(final XMPushService var0, final XmPushActionContainer var1) {
        var0.executeJob(new XMPushService.Job(4) {
            public String getDesc() {
                return "send ack message for message.";
            }

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

    private static void sendAppAbsentAck(final XMPushService var0, final XmPushActionContainer var1, final String var2) {
        try {
            JavaCalls.callStaticMethodOrThrow(MIPushEventProcessor.class, "sendAppAbsentAck", var0, var1, var2);
        } catch (Exception e) {
            Log4a.e(TAG, e.getMessage(), e);
        }
    }

    private static void sendAppNotInstallNotification(final XMPushService var0, final XmPushActionContainer var1) {
        try {
            JavaCalls.callStaticMethodOrThrow(MIPushEventProcessor.class, "sendAppNotInstallNotification", var0, var1);
        } catch (Exception e) {
            Log4a.e(TAG, e.getMessage(), e);
        }
    }

    private static void sendErrorAck(final XMPushService var0, final XmPushActionContainer var1, final String var2, final String var3) {
        try {
            JavaCalls.callStaticMethodOrThrow(MIPushEventProcessor.class, "sendErrorAck", var0, var1, var2, var3);
        } catch (Exception e) {
            Log4a.e(TAG, e.getMessage(), e);
        }
    }

    private static boolean verifyGeoMessage(Map<String, String> var0) {
        return var0 != null && var0.containsKey("__geo_ids");
    }


    private static boolean geoMessageIsValidated(XMPushService var0, XmPushActionContainer var1) {
        try {
//            MIPushEventProcessor.geoMessageIsValidated(var0, var1);
            return JavaCalls.callStaticMethodOrThrow(MIPushEventProcessor.class, "geoMessageIsValidated", var0, var1);
        } catch (Exception e) {
            Log4a.e(TAG, e.getMessage(), e);
        }
        return false;
    }


    private static boolean processGeoMessage(XMPushService var0, PushMetaInfo var1, byte[] var2) {
        try {
//            MIPushEventProcessor.processGeoMessage(var0, var1, var2);
            return JavaCalls.callStaticMethodOrThrow(MIPushEventProcessor.class, "processGeoMessage", var0, var1, var2);
        } catch (Exception e) {
            Log4a.e(TAG, e.getMessage(), e);
        }
        return false;
    }


    private static boolean shouldSendBroadcast(XMPushService paramXMPushService, String paramString, XmPushActionContainer paramXmPushActionContainer, PushMetaInfo paramPushMetaInfo) {
        try {
//            MIPushEventProcessor.shouldSendBroadcast(paramXMPushService, paramString, paramXmPushActionContainer, paramPushMetaInfo);
            return JavaCalls.callStaticMethodOrThrow(MIPushEventProcessor.class, "shouldSendBroadcast", paramXMPushService, paramString, paramXmPushActionContainer, paramPushMetaInfo);
        } catch (Exception e) {
            Log4a.e(TAG, e.getMessage(), e);
        }
        return false;
    }

}
