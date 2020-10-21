package com.xiaomi.push.service;

import android.accounts.Account;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;

import com.elvishew.xlog.Logger;
import com.elvishew.xlog.XLog;
import com.xiaomi.channel.commonutils.android.AppInfoUtils;
import com.xiaomi.channel.commonutils.logger.MyLog;
import com.xiaomi.xmpush.thrift.ActionType;
import com.xiaomi.xmpush.thrift.PushMetaInfo;
import com.xiaomi.xmpush.thrift.XmPushActionContainer;
import com.xiaomi.xmsf.BuildConfig;
import com.xiaomi.xmsf.R;

import java.util.List;
import java.util.Map;

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
 *
 * @author zts1993
 * @date 2018/2/8
 */

public class MyMIPushMessageProcessor {
    private static Logger logger = XLog.tag("MyMIPushMessageProcessor").build();

    public static void process(XMPushService paramXMPushService, XmPushActionContainer buildContainer, byte[] paramArrayOfByte, long var2, Intent localIntent, PushClientsManager.ClientLoginInfo clientLoginInfo) {
        try {
            long current = System.currentTimeMillis();
            PushMetaInfo localPushMetaInfo = buildContainer.getMetaInfo();
            if (localPushMetaInfo != null) {
                localPushMetaInfo.putToExtra("mrt", Long.toString(current));
            }

            boolean isSendMessage = ActionType.SendMessage == buildContainer.getAction();
            boolean isBusinessMessage = MIPushNotificationHelper.isBusinessMessage(buildContainer);

            if (isSendMessage && MIPushAppInfo.getInstance(paramXMPushService).isUnRegistered(buildContainer.packageName) && !isBusinessMessage) {
                String var20 = "";
                if (localPushMetaInfo != null) {
                    var20 = localPushMetaInfo.getId();
                }

                logger.w("Drop a message for unregistered, msgid=" + var20);
                sendAppAbsentAck(paramXMPushService, buildContainer, buildContainer.packageName);
            } else if (isSendMessage && MIPushAppInfo.getInstance(paramXMPushService).isPushDisabled4User(buildContainer.packageName) && !isBusinessMessage) {
                String var19 = "";
                if (localPushMetaInfo != null) {
                    var19 = localPushMetaInfo.getId();
                }

                logger.w("Drop a message for push closed, msgid=" + var19);
                sendAppAbsentAck(paramXMPushService, buildContainer, buildContainer.packageName);
            } else if (isSendMessage && !TextUtils.equals(paramXMPushService.getPackageName(), PushConstants.PUSH_SERVICE_PACKAGE_NAME) && !TextUtils.equals(paramXMPushService.getPackageName(), buildContainer.packageName)) {
                logger.w("Receive a message with wrong package name, expect " + paramXMPushService.getPackageName() + ", received " + buildContainer.packageName);
                sendErrorAck(paramXMPushService, buildContainer, "unmatched_package", "package should be " + paramXMPushService.getPackageName() + ", but got " + buildContainer.packageName);
            } else {
                if (localPushMetaInfo != null && localPushMetaInfo.getId() != null) {
                    logger.i(String.format("receive a message, appid=%s, msgid= %s", buildContainer.getAppid(), localPushMetaInfo.getId()));
                }

                if (localPushMetaInfo != null) {
                    Map<String, String> var17 = localPushMetaInfo.getExtra();
                    if (var17 != null && var17.containsKey("hide") && "true".equalsIgnoreCase(var17.get("hide"))) {
                        logger.i(String.format("hide a message, appid=%s, msgid= %s", buildContainer.getAppid(), localPushMetaInfo.getId()));
                        sendAckMessage(paramXMPushService, buildContainer);
                        return;
                    }
                }

                userProcessMIPushMessage(paramXMPushService, buildContainer, paramArrayOfByte, var2, localIntent, clientLoginInfo);
            }


        } catch (RuntimeException e2) {
            logger.e("fallbackProcessMIPushMessage failed at" + System.currentTimeMillis(), e2);
        }
    }


    /**
     * @see MIPushEventProcessor#postProcessMIPushMessage
     */
    private static void userProcessMIPushMessage(XMPushService paramXMPushService, XmPushActionContainer buildContainer, byte[] paramArrayOfByte, long var2, Intent paramIntent, PushClientsManager.ClientLoginInfo clientLoginInfo) {
        //var5 buildContainer
        //var6 metaInfo
        boolean shouldNotify = true;

        boolean pkgInstalled = AppInfoUtils.isPkgInstalled(paramXMPushService, buildContainer.packageName);
        if (!pkgInstalled) {
            sendAppNotInstallNotification(paramXMPushService, buildContainer);
            return;
        }

        String targetPackage = MIPushNotificationHelper.getTargetPackage(buildContainer);

        boolean isBusinessMessage = MIPushNotificationHelper.isBusinessMessage(buildContainer);
        if (isBusinessMessage) {
            if (ActionType.Registration == buildContainer.getAction()) {
                String str2 = buildContainer.getPackageName();
                SharedPreferences.Editor localEditor = paramXMPushService.getSharedPreferences(PREF_KEY_REGISTERED_PKGS, 0).edit();
                localEditor.putString(str2, buildContainer.appid);
                localEditor.apply();
//                com.xiaomi.tinyData.TinyDataManager.getInstance(paramXMPushService).processPendingData("Register Success, package name is " + str2);
            }
        }

        PushMetaInfo metaInfo = buildContainer.getMetaInfo();

        if (metaInfo == null) {
            return;
        }

        //abtest
        if (BuildConfig.APPLICATION_ID.contains(targetPackage) && !buildContainer.isEncryptAction() &&
                metaInfo.getExtra() != null && metaInfo.getExtra().containsKey("ab")) {
            sendAckMessage(paramXMPushService, buildContainer);
            MyLog.i("receive abtest message. ack it." + metaInfo.getId());
            return;
        }

        String title = metaInfo.getTitle();
        String description = metaInfo.getDescription();

        if (TextUtils.isEmpty(title) && TextUtils.isEmpty(description)) {
        } else {

            if (TextUtils.isEmpty(title)) {
                CharSequence appName = ApplicationNameCache.getInstance().getAppName(paramXMPushService, targetPackage);
                metaInfo.setTitle(appName.toString());
            }

            if (TextUtils.isEmpty(description)) {
                metaInfo.setDescription(paramXMPushService.getString(R.string.see_pass_though_msg));
            }

        }

        int uniqueHashCode = targetPackage.hashCode();
        if (!TextUtils.isEmpty(title)) {
            uniqueHashCode += title.hashCode();
        }
        if (!TextUtils.isEmpty(description)) {
            uniqueHashCode += description.hashCode();
        }
        shouldNotify = !MiPushMessageDuplicate.isDuplicateMessage(paramXMPushService, targetPackage, uniqueHashCode + "");

        String idKey = null;
        if (metaInfo.extra != null) {
            idKey = metaInfo.extra.get("jobkey");
        }
        if (TextUtils.isEmpty(idKey)) {
            idKey = metaInfo.getId();
        }
        boolean isDuplicateMessage = MiPushMessageDuplicate.isDuplicateMessage(paramXMPushService, targetPackage, idKey);

        if (!TextUtils.isEmpty(metaInfo.getTitle()) && !TextUtils.isEmpty(metaInfo.getDescription())) {

            if (isDuplicateMessage) {
                logger.w("drop a duplicate message, key=" + idKey);
            } else {

                if (shouldNotify) {
                    MyMIPushNotificationHelper.notifyPushMessage(paramXMPushService, buildContainer, paramArrayOfByte, var2);
                }

                //send broadcast
                if (!isBusinessMessage) {

                    Intent localIntent = new Intent(PushConstants.MIPUSH_ACTION_MESSAGE_ARRIVED);
                    localIntent.putExtra(PushConstants.MIPUSH_EXTRA_PAYLOAD, paramArrayOfByte);
                    localIntent.putExtra(MIPushNotificationHelper.FROM_NOTIFICATION, true);
                    localIntent.setPackage(targetPackage);

                    try {
                        List<ResolveInfo> localList = paramXMPushService.getPackageManager().queryBroadcastReceivers(localIntent, 0);
                        if ((localList != null) && (!localList.isEmpty())) {
                            paramXMPushService.sendBroadcast(localIntent, ClientEventDispatcher.getReceiverPermission(clientLoginInfo));
                        }
                    } catch (Exception ignore) {
                    }

                }

            }

            sendAckMessage(paramXMPushService, buildContainer);

        } else if (shouldSendBroadcast(paramXMPushService, targetPackage, buildContainer, metaInfo)) {

            paramXMPushService.sendBroadcast(paramIntent, ClientEventDispatcher.getReceiverPermission(clientLoginInfo));

        }

    }


}
