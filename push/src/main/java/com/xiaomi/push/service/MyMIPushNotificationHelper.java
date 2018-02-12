package com.xiaomi.push.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.xiaomi.channel.commonutils.reflect.JavaCalls;
import com.xiaomi.xmpush.thrift.PushMetaInfo;
import com.xiaomi.xmpush.thrift.XmPushActionContainer;
import com.xiaomi.xmsf.R;
import com.xiaomi.xmsf.push.notification.OreoNotificationManager;

import java.util.ArrayList;

import me.pqpo.librarylog4a.Log4a;

import static com.xiaomi.push.service.MIPushNotificationHelper.isBusinessMessage;

/**
 * Created by zts1993 on 2018/2/8.
 */

public class MyMIPushNotificationHelper {
    private static final String TAG = "MyMIPushNotificationHelper";

    static void notifyPushMessage(XMPushService var0, XmPushActionContainer buildContainer, byte[] var1, long var2) {
        PushMetaInfo metaInfo = buildContainer.getMetaInfo();

        String title = metaInfo.getTitle();
        String description = metaInfo.getDescription();

        int id = MyClientEventDispatcher.getNotificationId(buildContainer);

        Notification.Builder localBuilder = new Notification.Builder(var0);
        Log4a.i(TAG, "title:" + title + "  description:" + description);

        PendingIntent localPendingIntent = getClickedPendingIntent(var0, buildContainer, metaInfo, var1);
        if (localPendingIntent != null) {
            localBuilder.setContentIntent(localPendingIntent);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            try {
                RemoteViews localRemoteViews = JavaCalls.callStaticMethodOrThrow(MIPushNotificationHelper.class, "getNotificationForCustomLayout", var0.getApplicationContext(), buildContainer, var1);
                if (localRemoteViews != null) {
                        localBuilder.setCustomContentView(localRemoteViews);
                }
            } catch(Exception e){
                Log4a.e(TAG, e.getLocalizedMessage(), e);
            }

            int i = R.drawable.ic_notifications_black_24dp;
            ArrayList<Notification.Action> actions = new ArrayList<>();
            {
                PendingIntent pendingIntent = openActivityPendingIntent(var0, buildContainer, metaInfo, var1);
                if (pendingIntent != null) {
                    actions.add(new Notification.Action(i, "开启应用", pendingIntent));
                }
            }
            {
                PendingIntent pendingIntent = startServicePendingIntent(var0, buildContainer, metaInfo, var1);
                if (pendingIntent != null) {
                    actions.add(new Notification.Action(i, "跳转", pendingIntent));
                }
            }

            Notification.Action[] actions1 = {};
            localBuilder.setActions(actions.toArray(actions1));
        }

        if (Build.VERSION.SDK_INT >= 24) {
            localBuilder.setWhen(System.currentTimeMillis());
            localBuilder.setShowWhen(true);
        }

        //localBuilder.setContent(paramRemoteViews); //not supported
        localBuilder.setContentTitle(title);
        localBuilder.setContentText(description);
        localBuilder.setGroup(buildContainer.getPackageName());

        try {
            Drawable icon = var0.getPackageManager().getApplicationIcon(buildContainer.getPackageName());
            Bitmap bitmap = MIPushNotificationHelper.drawableToBitmap(icon);
            localBuilder.setLargeIcon(bitmap);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                localBuilder.setSmallIcon(Icon.createWithBitmap(bitmap)); //but non-pic in notification detail
            } else {
                localBuilder.setSmallIcon(R.drawable.ic_notifications_black_24dp);
            }
        } catch (Exception e) {
            localBuilder.setSmallIcon(R.drawable.ic_notifications_black_24dp);
        }

        OreoNotificationManager manager = ((PushServiceMain) var0).getNotificationManager();
        Notification notification = localBuilder.build();
        manager.notify(id, notification);

    }


    private static PendingIntent openActivityPendingIntent(Context paramContext, XmPushActionContainer paramXmPushActionContainer, PushMetaInfo paramPushMetaInfo, byte[] paramArrayOfByte) {
        String package_name = paramXmPushActionContainer.getPackageName();
        PackageManager packageManager = paramContext.getPackageManager();
        Intent localIntent1 = packageManager.getLaunchIntentForPackage(package_name);
        if (localIntent1 != null) {
            localIntent1.addCategory(String.valueOf(paramPushMetaInfo.getNotifyId()));
            return PendingIntent.getActivity(paramContext, 0, localIntent1, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        return null;
    }

    private static PendingIntent getClickedPendingIntent(Context paramContext, XmPushActionContainer paramXmPushActionContainer, PushMetaInfo paramPushMetaInfo, byte[] paramArrayOfByte) {
        if (paramPushMetaInfo == null) {
            return null;
        }
        PendingIntent localPendingIntent;

        int id = MyClientEventDispatcher.getNotificationId(paramXmPushActionContainer);

        {
            //Jump web
            String urlJump = null;
            if (!TextUtils.isEmpty(paramPushMetaInfo.url)) {
                urlJump = paramPushMetaInfo.url;
            } else if (paramPushMetaInfo.getExtra() != null) {
                urlJump = paramPushMetaInfo.getExtra().get(PushConstants.EXTRA_PARAM_WEB_URI);
            }

            if (!TextUtils.isEmpty(urlJump)) {
                Intent localIntent3 = new Intent("android.intent.action.VIEW");
                localIntent3.setData(Uri.parse(urlJump));
                localIntent3.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
                localPendingIntent = PendingIntent.getActivity(paramContext, id, localIntent3, PendingIntent.FLAG_UPDATE_CURRENT);
                return localPendingIntent;
            }
        }

        if (isBusinessMessage(paramXmPushActionContainer)) {
            Intent localIntent = new Intent();
            localIntent.setComponent(new ComponentName("com.xiaomi.xmsf", "com.xiaomi.mipush.sdk.PushMessageHandler"));
            localIntent.putExtra("mipush_payload", paramArrayOfByte);
            localIntent.putExtra("mipush_notified", true);
            localIntent.addCategory(String.valueOf(paramPushMetaInfo.getNotifyId()));
            localPendingIntent = PendingIntent.getService(paramContext, id, localIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            Intent localIntent = new Intent();
            localIntent.setComponent(new ComponentName("com.xiaomi.xmsf", "com.xiaomi.push.sdk.MyPushMessageHandler"));
//            Intent localIntent = new Intent("com.xiaomi.mipush.RECEIVE_MESSAGE");
//            localIntent.setComponent(new ComponentName(paramXmPushActionContainer.packageName, "com.xiaomi.mipush.sdk.PushMessageHandler"));
            localIntent.putExtra("mipush_payload", paramArrayOfByte);
            localIntent.putExtra("mipush_notified", true);
            localIntent.addCategory(String.valueOf(paramPushMetaInfo.getNotifyId()));
            localPendingIntent = PendingIntent.getService(paramContext, id, localIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        }
        return localPendingIntent;
    }

    private static PendingIntent startServicePendingIntent(Context paramContext, XmPushActionContainer paramXmPushActionContainer, PushMetaInfo paramPushMetaInfo, byte[] paramArrayOfByte) {
        if (paramPushMetaInfo == null) {
            return null;
        }
        PendingIntent localPendingIntent;

        if (isBusinessMessage(paramXmPushActionContainer)) {
            Intent localIntent = new Intent();
            localIntent.setComponent(new ComponentName("com.xiaomi.xmsf", "com.xiaomi.mipush.sdk.PushMessageHandler"));
            localIntent.putExtra("mipush_payload", paramArrayOfByte);
            localIntent.putExtra("mipush_notified", true);
            localIntent.addCategory(String.valueOf(paramPushMetaInfo.getNotifyId()));
            localPendingIntent = PendingIntent.getService(paramContext, 0, localIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            Intent localIntent = new Intent("com.xiaomi.mipush.RECEIVE_MESSAGE");
            localIntent.setComponent(new ComponentName(paramXmPushActionContainer.packageName, "com.xiaomi.mipush.sdk.PushMessageHandler"));
            localIntent.putExtra("mipush_payload", paramArrayOfByte);
            localIntent.putExtra("mipush_notified", true);
            localIntent.addCategory(String.valueOf(paramPushMetaInfo.getNotifyId()));
            localPendingIntent = PendingIntent.getService(paramContext, 0, localIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        return localPendingIntent;
    }


}
