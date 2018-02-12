package com.xiaomi.push.service;

import android.content.ContentValues;
import android.content.Intent;
import android.text.TextUtils;

import com.xiaomi.channel.commonutils.android.AppInfoUtils;
import com.xiaomi.channel.commonutils.logger.MyLog;
import com.xiaomi.smack.XMPPException;
import com.xiaomi.xmpush.thrift.ActionType;
import com.xiaomi.xmpush.thrift.PushMetaInfo;
import com.xiaomi.xmpush.thrift.XmPushActionContainer;

import java.util.ArrayList;
import java.util.Map;

import me.pqpo.librarylog4a.Log4a;


/**
 * Created by zts1993 on 2018/2/8.
 */

public class MyMIPushMessageProcessor {
    private static final String TAG = "MyMIPushMessageProcessor";

    public static void process(XMPushService var0, XmPushActionContainer var4, byte[] var1, long var2, Intent localIntent) {
        try {
            String targetPackage = MIPushNotificationHelper.getTargetPackage(var4);
            Long current = System.currentTimeMillis();
//            Intent var6 = MIPushEventProcessor.buildIntent(var1, current);
            PushMetaInfo var8 = var4.getMetaInfo();
            if (var8 != null) {
                var8.putToExtra("mrt", Long.toString(current));
            }
            if (ActionType.SendMessage == var4.getAction() && MIPushAppInfo.getInstance(var0).isUnRegistered(var4.packageName) && !MIPushNotificationHelper.isBusinessMessage(var4)) {
                String var20 = "";
                if (var8 != null) {
                    var20 = var8.getId();
                }

                Log4a.w(TAG, "Drop a message for unregistered, msgid=" + var20);
                sendAppAbsentAck(var0, var4, var4.packageName);
            } else if (ActionType.SendMessage == var4.getAction() && MIPushAppInfo.getInstance(var0).isPushDisabled4User(var4.packageName) && !MIPushNotificationHelper.isBusinessMessage(var4)) {
                String var19 = "";
                if (var8 != null) {
                    var19 = var8.getId();
                }

                Log4a.w(TAG, "Drop a message for push closed, msgid=" + var19);
                sendAppAbsentAck(var0, var4, var4.packageName);
            } else if (ActionType.SendMessage == var4.getAction() && !TextUtils.equals(var0.getPackageName(), "com.xiaomi.xmsf") && !TextUtils.equals(var0.getPackageName(), var4.packageName)) {
                Log4a.w(TAG, "Receive a message with wrong package name, expect " + var0.getPackageName() + ", received " + var4.packageName);
                sendErrorAck(var0, var4, "unmatched_package", "package should be " + var0.getPackageName() + ", but got " + var4.packageName);
            } else {
                if (var8 != null && var8.getId() != null) {
                    Log4a.i(TAG, String.format("receive a message, appid=%s, msgid= %s", var4.getAppid(), var8.getId()));
                }

                if (var8 != null) {
                    Map<String, String> var17 = var8.getExtra();
                    if (var17 != null && var17.containsKey("hide") && "true".equalsIgnoreCase(var17.get("hide"))) {
                        Log4a.i(TAG, String.format("hide a message, appid=%s, msgid= %s", var4.getAppid(), var8.getId()));
                        sendAckMessage(var0, var4);
                        return;
                    }
                }

                //SKIP xiaomi account login

                boolean var9 = (var8 != null && verifyGeoMessage(var8.getExtra()));
                if (var9) {
                    if (!geoMessageIsValidated(var0, var4)) {
                        return;
                    }

                    boolean var10 = processGeoMessage(var0, var8, var1);
                    MIPushEventProcessor.sendGeoAck(var0, var4, true, false, false);
                    if (!var10) {
                        return;
                    }
                }

                userProcessMIPushMessage(var0, var4, var1, var2, localIntent, var9);
            }


        } catch (RuntimeException e2) {
            Log4a.e(TAG, "fallbackProcessMIPushMessage failed at" + System.currentTimeMillis(), e2);
        }
    }


    private static void userProcessMIPushMessage(XMPushService var0, XmPushActionContainer buildContainer, byte[] var1, long var2, Intent localIntent, boolean var4) {
        //var5 buildContainer
        //var6 metaInfo

//        if ((!MIPushNotificationHelper.isBusinessMessage(buildContainer) || !AppInfoUtils.isPkgInstalled(var0, buildContainer.packageName))) {
//            if (!AppInfoUtils.isPkgInstalled(var0, buildContainer.packageName)) {
//                sendAppNotInstallNotification(var0, buildContainer);
//            } else {
//                Log4a.w(TAG, "receive a mipush message, we can see the app " + buildContainer.packageName+ ", but we can't see the receiver.");
//            }
//        }

        if (MIPushNotificationHelper.isBusinessMessage(buildContainer) && AppInfoUtils.isPkgInstalled(var0, buildContainer.packageName)) {
            if (ActionType.Registration == buildContainer.getAction()) {
                String str2 = buildContainer.getPackageName();
                com.xiaomi.tinyData.TinyDataManager.getInstance(var0).processPendingData("Register Success, package name is " + str2);
            }
        }

        PushMetaInfo metaInfo = buildContainer.getMetaInfo();

        if ("com.xiaomi.xmsf".contains(buildContainer.packageName) && !buildContainer.isEncryptAction() &&
                metaInfo != null && metaInfo.getExtra() != null && metaInfo.getExtra().containsKey("ab")) {
            sendAckMessage(var0, buildContainer);
            MyLog.v("receive abtest message. ack it." + metaInfo.getId());
            return;
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
//        MIPushNotificationHelper.isApplicationForeground(var0, buildContainer.packageName);

        String var8 = null;
        if (metaInfo.extra != null) {
            var8 = metaInfo.extra.get("jobkey");
        }
        if (TextUtils.isEmpty(var8)) {
            var8 = metaInfo.getId();
        }
        boolean var7 = MiPushMessageDuplicate.isDuplicateMessage(var0, buildContainer.packageName, var8);
        if (var7) {
            Log4a.w(TAG, "drop a duplicate message, key=" + var8);
        } else {
            //NotifyPushMessageInfo var9 = MIPushNotificationHelper.notifyPushMessage(var0, var5, var2);
            MyMIPushNotificationHelper.notifyPushMessage(var0, buildContainer, var1, var2);
        }

        if (var4) {
            MIPushEventProcessor.sendGeoAck(var0, buildContainer, false, true, false);
        } else {
            sendAckMessage(var0, buildContainer);
        }
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
        var0.executeJob(new XMPushService.Job(4) {
            public String getDesc() {
                return "send app absent ack message for message.";
            }

            public void process() {
                try {
                    XmPushActionContainer var2x = MIPushEventProcessor.constructAckMessage(var0, var1);
                    var2x.getMetaInfo().putToExtra("absent_target_package", var2);
                    MIPushHelper.sendPacket(var0, var2x);
                } catch (XMPPException var3) {
                    Log4a.e(TAG, var3);
                    var0.disconnect(10, var3);
                }

            }
        });
    }

    private static void sendAppNotInstallNotification(final XMPushService var0, final XmPushActionContainer var1) {
        var0.executeJob(new XMPushService.Job(4) {
            public String getDesc() {
                return "send app absent message.";
            }

            public void process() {
                try {
                    MIPushHelper.sendPacket(var0, MIPushHelper.contructAppAbsentMessage(var1.getPackageName(), var1.getAppid()));
                } catch (XMPPException var2) {
                    Log4a.e(TAG, var2);
                    var0.disconnect(10, var2);
                }

            }
        });
    }

    private static void sendErrorAck(final XMPushService var0, final XmPushActionContainer var1, final String var2, final String var3) {
        var0.executeJob(new XMPushService.Job(4) {
            public String getDesc() {
                return "send wrong message ack for message.";
            }

            public void process() {
                try {
                    XmPushActionContainer var2x = MIPushEventProcessor.constructAckMessage(var0, var1);
                    var2x.metaInfo.putToExtra("error", var2);
                    var2x.metaInfo.putToExtra("reason", var3);
                    MIPushHelper.sendPacket(var0, var2x);
                } catch (XMPPException var3x) {
                    Log4a.e(TAG, var3x);
                    var0.disconnect(10, var3x);
                }

            }
        });
    }

    private static boolean verifyGeoMessage(Map<String, String> var0) {
        return var0 != null && var0.containsKey("__geo_ids");
    }


    private static boolean geoMessageIsValidated(XMPushService var0, XmPushActionContainer var1) {
        boolean var2;
        if (!GeoFenceUtils.checkMetoknlpVersionAbove(var0)) {
            var2 = false;
        } else if (!GeoFenceUtils.verifyGeoChannel(var0)) {
            var2 = false;
        } else if (!AppInfoUtils.isPkgInstalled(var0, var1.packageName)) {
            sendAppNotInstallNotification(var0, var1);
            var2 = false;
        } else {
            Map var3 = var1.getMetaInfo().getExtra();
            if (var3 == null) {
                var2 = false;
            } else if (!"12".contains((CharSequence) var3.get("__geo_action"))) {
                var2 = false;
            } else if (TextUtils.isEmpty((CharSequence) var3.get("__geo_ids"))) {
                var2 = false;
            } else {
                var2 = true;
            }
        }
        return var2;
    }


    private static boolean processGeoMessage(XMPushService var0, PushMetaInfo var1, byte[] var2) {
        Map var3 = var1.getExtra();
        String[] var4 = ((String) var3.get("__geo_ids")).split(",");
        ArrayList<android.content.ContentValues> var5 = new ArrayList<android.content.ContentValues>();
        int var6 = var4.length;
        int var7 = 0;

        boolean var8;
        while (true) {
            if (var7 >= var6) {
                if (!GeoFenceMessageDao.getInstance(var0).insertGeoMessages(var5)) {
                    MyLog.v("geofence added some new geofence message failed messagi_id:" + var1.getId());
                }
                var8 = false;
                break;
            }

            String var9 = var4[var7];
            ContentValues var10 = new ContentValues();
            var10.put("geo_id", var9);
            var10.put("message_id", var1.getId());
            int var11 = Integer.parseInt((String) var3.get("__geo_action"));
            var10.put("action", var11);
            var10.put("content", var2);
            var10.put("deadline", Long.parseLong((String) var3.get("__geo_deadline")));
            if (TextUtils.equals(GeoFenceDao.getInstance(var0).findGeoStatueByGeoId(var9), "Enter") && var11 == 1) {
                var8 = true;
                break;
            }

            var5.add(var10);
            ++var7;
        }

        return var8;
    }

}
