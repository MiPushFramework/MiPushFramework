package com.xiaomi.push.service;

import android.accounts.Account;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;

import com.xiaomi.channel.commonutils.android.AppInfoUtils;
import com.xiaomi.channel.commonutils.android.MIIDUtils;
import com.xiaomi.channel.commonutils.logger.MyLog;
import com.xiaomi.xmpush.thrift.ActionType;
import com.xiaomi.xmpush.thrift.PushMetaInfo;
import com.xiaomi.xmpush.thrift.XmPushActionContainer;
import com.xiaomi.xmsf.BuildConfig;
import com.xiaomi.xmsf.R;

import java.util.List;
import java.util.Map;

import me.pqpo.librarylog4a.Log4a;
import top.trumeet.common.cache.ApplicationNameCache;

import static com.xiaomi.push.service.MiPushMsgAck.geoMessageIsValidated;
import static com.xiaomi.push.service.MiPushMsgAck.processGeoMessage;
import static com.xiaomi.push.service.MiPushMsgAck.sendAckMessage;
import static com.xiaomi.push.service.MiPushMsgAck.sendAppAbsentAck;
import static com.xiaomi.push.service.MiPushMsgAck.sendAppNotInstallNotification;
import static com.xiaomi.push.service.MiPushMsgAck.sendErrorAck;
import static com.xiaomi.push.service.MiPushMsgAck.shouldSendBroadcast;
import static com.xiaomi.push.service.MiPushMsgAck.verifyGeoMessage;
import static com.xiaomi.push.service.PushServiceConstants.PREF_KEY_REGISTERED_PKGS;


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


    /**
     * @see MIPushEventProcessor#postProcessMIPushMessage
     */
    private static void userProcessMIPushMessage(XMPushService paramXMPushService, XmPushActionContainer buildContainer, byte[] paramArrayOfByte, long var2, Intent paramIntent, boolean isGeoMessage) {
        //var5 buildContainer
        //var6 metaInfo

        boolean pkgInstalled = AppInfoUtils.isPkgInstalled(paramXMPushService, buildContainer.packageName);
        if (!pkgInstalled) {
            sendAppNotInstallNotification(paramXMPushService, buildContainer);
            return;
        }

        String targetPackage = MIPushNotificationHelper.getTargetPackage(buildContainer);

        if (MIPushNotificationHelper.isBusinessMessage(buildContainer)) {
            if (ActionType.Registration == buildContainer.getAction()) {
                String str2 = buildContainer.getPackageName();
                SharedPreferences.Editor localEditor = paramXMPushService.getSharedPreferences(PREF_KEY_REGISTERED_PKGS, 0).edit();
                localEditor.putString(str2, buildContainer.appid);
                localEditor.apply();
                com.xiaomi.tinyData.TinyDataManager.getInstance(paramXMPushService).processPendingData("Register Success, package name is " + str2);
            }
        }

        PushMetaInfo metaInfo = buildContainer.getMetaInfo();

        //abtest
        if (BuildConfig.APPLICATION_ID.contains(buildContainer.packageName) && !buildContainer.isEncryptAction() &&
                metaInfo != null && metaInfo.getExtra() != null && metaInfo.getExtra().containsKey("ab")) {
            sendAckMessage(paramXMPushService, buildContainer);
            MyLog.i("receive abtest message. ack it." + metaInfo.getId());
            return;
        }

        if (metaInfo != null) {
            String title = metaInfo.getTitle();
            String description = metaInfo.getDescription();

            if (TextUtils.isEmpty(title) && TextUtils.isEmpty(description)) {
            } else {

                if (TextUtils.isEmpty(title)) {
                    CharSequence appName = ApplicationNameCache.getInstance().getAppName(paramXMPushService, buildContainer.packageName);
                    if (appName == null) {
                        appName = buildContainer.packageName;
                    }
                    metaInfo.setTitle(appName.toString());
                }

                if (TextUtils.isEmpty(description)) {
                    metaInfo.setDescription(paramXMPushService.getString(R.string.see_pass_though_msg));
                }
            }
        }

        if (metaInfo != null && !TextUtils.isEmpty(metaInfo.getTitle()) && !TextUtils.isEmpty(metaInfo.getDescription())) {

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

                MyMIPushNotificationHelper.notifyPushMessage(paramXMPushService, buildContainer, paramArrayOfByte, var2);

                //send broadcast
                if (!MIPushNotificationHelper.isBusinessMessage(buildContainer)) {

                    Intent localIntent = new Intent(PushConstants.MIPUSH_ACTION_MESSAGE_ARRIVED);
                    localIntent.putExtra(PushConstants.MIPUSH_EXTRA_PAYLOAD, paramArrayOfByte);
                    localIntent.setPackage(buildContainer.packageName);
                    try {
                        List<ResolveInfo> localList = paramXMPushService.getPackageManager().queryBroadcastReceivers(localIntent, 0);
                        if ((localList != null) && (!localList.isEmpty())) {
                            paramXMPushService.sendBroadcast(localIntent, ClientEventDispatcher.getReceiverPermission(buildContainer.getPackageName()));
                        }
                    } catch (Exception ignore) {
                    }

                }

            }

            if (isGeoMessage) {
                MIPushEventProcessor.sendGeoAck(paramXMPushService, buildContainer, false, true, false);
            } else {
                sendAckMessage(paramXMPushService, buildContainer);

            }
        }

        if (shouldSendBroadcast(paramXMPushService, targetPackage, buildContainer, metaInfo)) {
//            if (ConfigCenter.getInstance().enableWakeupTarget) {
            paramXMPushService.sendBroadcast(paramIntent, ClientEventDispatcher.getReceiverPermission(buildContainer.packageName));
//            }
        }

    }

}
