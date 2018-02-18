package com.xiaomi.push.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.graphics.ColorUtils;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.widget.RemoteViews;

import com.xiaomi.channel.commonutils.reflect.JavaCalls;
import com.xiaomi.xmpush.thrift.PushMetaInfo;
import com.xiaomi.xmpush.thrift.XmPushActionContainer;
import com.xiaomi.xmsf.R;

import me.pqpo.librarylog4a.Log4a;
import top.trumeet.common.BuildConfig;

import static com.xiaomi.push.service.MIPushNotificationHelper.drawableToBitmap;
import static com.xiaomi.push.service.MIPushNotificationHelper.isBusinessMessage;
import static top.trumeet.common.utils.NotificationUtils.getChannelIdByPkg;

/**
 * Created by zts1993 on 2018/2/8.
 */

public class MyMIPushNotificationHelper {
    private static final String NOTIFICATION_ICON = "mipush_notification";
    private static final String NOTIFICATION_SMALL_ICON = "mipush_small_notification";

    private static final String TAG = "MyMIPushNotificationHelper";

    static void notifyPushMessage(XMPushService var0, XmPushActionContainer buildContainer, byte[] var1, long var2) {
        PushMetaInfo metaInfo = buildContainer.getMetaInfo();

        String title = metaInfo.getTitle();
        String description = metaInfo.getDescription();

        int id = MyClientEventDispatcher.getNotificationId(buildContainer);

        Notification.Builder localBuilder = new Notification.Builder(var0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            localBuilder.setChannelId(getChannelIdByPkg(buildContainer.getPackageName()));
        }
        Log4a.i(TAG, "title:" + title + "  description:" + description);

        // Set small icon
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int iconId = getIconId(var0, buildContainer.getPackageName(), NOTIFICATION_ICON);
            int iconId2 = getIconId(var0, buildContainer.getPackageName(), NOTIFICATION_SMALL_ICON);
            int iconIdFinal;
            Log4a.d(TAG, "id: " + iconId + ", id2: " + iconId2);
            if (iconId <= 0 || iconId2 <= 0) {
                iconIdFinal = getIdForSmallIcon(var0, buildContainer.getPackageName());
                if (iconIdFinal == 0)
                    iconIdFinal = R.drawable.ic_notifications_black_24dp;
            } else {
                localBuilder.setLargeIcon(getBitmapFromId(var0, iconId));
                iconIdFinal = iconId2;
            }
            // TODO: 系统会把 Icon tint 成白块
            localBuilder.setSmallIcon(Icon.createWithResource(buildContainer.getPackageName()
                    , iconIdFinal));
        } else {
            // TODO: Icon 向下兼容
            localBuilder.setSmallIcon(R.drawable.ic_notifications_black_24dp);
        }

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
            } catch (Exception e) {
                Log4a.e(TAG, e.getLocalizedMessage(), e);
            }

            // Debug actions
            if (BuildConfig.DEBUG) {
                int i = R.drawable.ic_notifications_black_24dp;

                PendingIntent pendingIntentOpenActivity = openActivityPendingIntent(var0, buildContainer, metaInfo, var1);
                if (pendingIntentOpenActivity != null) {
                    localBuilder.addAction(new Notification.Action(i, "Open App", pendingIntentOpenActivity));
                }

                PendingIntent pendingIntentJump = startServicePendingIntent(var0, buildContainer, metaInfo, var1);
                if (pendingIntentJump != null) {
                    localBuilder.addAction(new Notification.Action(i, "Jump", pendingIntentJump));
                }
            }

        }

        if (Build.VERSION.SDK_INT >= 24) {
            localBuilder.setWhen(System.currentTimeMillis());
            localBuilder.setShowWhen(true);
        }

        //localBuilder.setContent(paramRemoteViews); //not supported

        String[] titleAndDesp = determineTitleAndDespByDIP(var0, metaInfo);
        localBuilder.setContentTitle(titleAndDesp[0]);
        localBuilder.setContentText(titleAndDesp[1]);

        localBuilder.setGroup(buildContainer.getPackageName());

        Drawable icon = null;
        try {
            icon = var0.getPackageManager().getApplicationIcon(buildContainer.getPackageName());
        } catch (Exception e) {
            localBuilder.setSmallIcon(R.drawable.ic_notifications_black_24dp);
        }

        // Fill app name
        Bundle extras = new Bundle();
        try {
            if (icon != null) {
                int color = getIconColor(icon);
                CharSequence subText = createColorSubtext(var0.getPackageManager()
                        .getApplicationLabel(var0.getPackageManager().getApplicationInfo(buildContainer.getPackageName(),
                                0)), color);
                if (subText != null) extras.putCharSequence(NotificationCompat.EXTRA_SUB_TEXT,
                        subText);
                localBuilder.setColor(color);
            }
        } catch (PackageManager.NameNotFoundException ignored) {}
        localBuilder.setExtras(extras);

        NotificationManager manager = (NotificationManager) var0.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = localBuilder.build();
        manager.notify(id, notification);

    }

    private static int getIconColor (Drawable icon) {
        int color = com.xiaomi.xmsf.utils.ColorUtils.getIconColor(icon);
        if (color != Notification.COLOR_DEFAULT) {
            final float[] hsl = new float[3];
            ColorUtils.colorToHSL(color, hsl);
            hsl[1] = 0.94f;
            hsl[2] = Math.min(hsl[2] * 0.6f, 0.31f);
            return ColorUtils.HSLToColor(hsl);
        }
        return color;
    }

    private static Spannable createColorSubtext (CharSequence appName,
                                                 int color) {
        final Spannable amened = new SpannableStringBuilder(appName);
        // 弄一个自己的颜色 TODO：不知道小米有没有这个 API，或者抄袭 AOSP 的实现
        amened.setSpan(new ForegroundColorSpan(color),
                0, amened.length(), 0);
        return amened;
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

    private static String[] determineTitleAndDespByDIP(Context paramContext, PushMetaInfo paramPushMetaInfo) {

        try {
//            MIPushNotificationHelper.determineTitleAndDespByDIP(paramContext, paramPushMetaInfo);
            return JavaCalls.callStaticMethodOrThrow(MIPushNotificationHelper.class, "determineTitleAndDespByDIP", paramContext, paramPushMetaInfo);
        } catch (Exception e) {
            Log4a.e(TAG, e.getMessage(), e);
            return new String[]{paramPushMetaInfo.getTitle(), paramPushMetaInfo.getDescription()};
        }
    }

    private static Bitmap getBitmapFromId(Context context, int i) {
        return drawableToBitmap(context.getResources().getDrawable(i));
    }

    private static int getIconId(Context context, String str, String str2) {
        return context.getResources().getIdentifier(str2, "drawable", str);
    }

    private static int getIdForSmallIcon(Context context, String str) {
        int iconId = getIconId(context, str, NOTIFICATION_ICON);
        int iconId2 = getIconId(context, str, NOTIFICATION_SMALL_ICON);
        ApplicationInfo info = null;
        try {
            info = context.getPackageManager().getApplicationInfo(str, 0);
        } catch (PackageManager.NameNotFoundException ignored) {
            return 0;
        }
        if (iconId <= 0) {
            iconId = iconId2 > 0 ? iconId2 : info.icon;
        }
        return (iconId != 0 || Build.VERSION.SDK_INT < 9) ? iconId : info.logo;
    }
}
