package com.xiaomi.push.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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

import com.xiaomi.channel.commonutils.logger.MyLog;
import com.xiaomi.channel.commonutils.reflect.JavaCalls;
import com.xiaomi.xmpush.thrift.PushMetaInfo;
import com.xiaomi.xmpush.thrift.XmPushActionContainer;
import com.xiaomi.xmsf.R;
import com.xiaomi.xmsf.XmsfApp;
import com.xiaomi.xmsf.push.notification.NotificationController;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import me.pqpo.librarylog4a.Log4a;
import top.trumeet.common.cache.ApplicationNameCache;
import top.trumeet.common.cache.IconCache;

import static com.xiaomi.push.service.MIPushNotificationHelper.drawableToBitmap;
import static com.xiaomi.push.service.MIPushNotificationHelper.isBusinessMessage;

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

        Log4a.i(TAG, "title:" + title + "  description:" + description);

        if (description.length() > 30) { //TODO length 30 is constant
            Notification.BigTextStyle style = new Notification.BigTextStyle();
            style.bigText(description);
            style.setBigContentTitle(title);
            style.setSummaryText(description);
            localBuilder.setStyle(style);
        }

        Bitmap iconBitmap = IconCache.getInstance().getRawIconBitmap(var0, buildContainer.getPackageName());

        // Set small icon
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int iconLargeId = getIconId(var0, buildContainer.getPackageName(), NOTIFICATION_ICON);
            int iconSmallId = getIconId(var0, buildContainer.getPackageName(), NOTIFICATION_SMALL_ICON);
            Log4a.d(TAG, "id: " + iconLargeId + ", id2: " + iconSmallId);

            if (iconLargeId <= 0) {
                if (iconBitmap != null) {
                    localBuilder.setLargeIcon(iconBitmap);
                }
            } else {
                localBuilder.setLargeIcon(getBitmapFromId(var0, iconLargeId));
            }

            if (iconSmallId <= 0) {

                Bitmap whiteIconBitmap = IconCache.getInstance().getWhiteIconBitmap(var0, buildContainer.getPackageName());
                if (whiteIconBitmap != null) {
                    localBuilder.setSmallIcon(Icon.createWithBitmap(whiteIconBitmap));
                } else {
                    localBuilder.setSmallIcon(R.drawable.ic_notifications_black_24dp);
                }

            } else {
                localBuilder.setSmallIcon(Icon.createWithResource(buildContainer.getPackageName(), iconSmallId));
            }
        } else {
            localBuilder.setSmallIcon(R.drawable.ic_notifications_black_24dp);
        }

        PendingIntent localPendingIntent = getClickedPendingIntent(var0, buildContainer, metaInfo, var1);
        if (localPendingIntent != null) {
            localBuilder.setContentIntent(localPendingIntent);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

//            try {
//                RemoteViews localRemoteViews = JavaCalls.callStaticMethodOrThrow(MIPushNotificationHelper.class, "getNotificationForCustomLayout", var0.getApplicationContext(), buildContainer, var1);
//                if (localRemoteViews != null) {
//                    localBuilder.setCustomContentView(localRemoteViews);
//                }
//            } catch (Exception e) {
//                Log4a.e(TAG, e.getLocalizedMessage(), e);
//            }

            // Debug actions
            if (XmsfApp.conf.debugIntent) {
                int i = R.drawable.ic_notifications_black_24dp;

                PendingIntent pendingIntentOpenActivity = openActivityPendingIntent(var0, buildContainer, metaInfo, var1);
                if (pendingIntentOpenActivity != null) {
                    localBuilder.addAction(new Notification.Action(i, "Open App", pendingIntentOpenActivity));
                }

                PendingIntent pendingIntentJump = startServicePendingIntent(var0, buildContainer, metaInfo, var1);
                if (pendingIntentJump != null) {
                    localBuilder.addAction(new Notification.Action(i, "Jump", pendingIntentJump));
                }

                Intent sdkIntentJump = getSdkIntent(var0, buildContainer.getPackageName(), metaInfo);
                if (sdkIntentJump != null) {
                    PendingIntent pendingIntent = PendingIntent.getActivity(var0, 0, sdkIntentJump, PendingIntent.FLAG_UPDATE_CURRENT);
                    localBuilder.addAction(new Notification.Action(i, "SDK Intent", pendingIntent));
                }

            }

            localBuilder.setWhen(System.currentTimeMillis());
            localBuilder.setShowWhen(true);
        }

        String[] titleAndDesp = determineTitleAndDespByDIP(var0, metaInfo);
        localBuilder.setContentTitle(titleAndDesp[0]);
        localBuilder.setContentText(titleAndDesp[1]);


        //for VERSION < N_MR1
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
            localBuilder.setDefaults(Notification.DEFAULT_ALL);
        }

        // Fill app name
        Bundle extras = new Bundle();
        CharSequence appName = ApplicationNameCache.getInstance().getAppName(var0, buildContainer.getPackageName());

        if (iconBitmap != null && !TextUtils.isEmpty(appName)) {
            int color = getIconColor(iconBitmap);
            CharSequence subText = createColorSubtext(appName, color);
            if (subText != null) {
                extras.putCharSequence(NotificationCompat.EXTRA_SUB_TEXT, subText);
            }
            localBuilder.setColor(color);
        }

        localBuilder.setExtras(extras);

        NotificationController.publish(var0, id, buildContainer.getPackageName(), localBuilder);

    }

    private static int getIconColor(Bitmap bitmap) {
        int color = com.xiaomi.xmsf.utils.ColorUtils.getIconColor(bitmap);
        if (color != Notification.COLOR_DEFAULT) {
            final float[] hsl = new float[3];
            ColorUtils.colorToHSL(color, hsl);
            hsl[1] = 0.94f;
            hsl[2] = Math.min(hsl[2] * 0.6f, 0.31f);
            return ColorUtils.HSLToColor(hsl);
        }
        return color;
    }

    public static Spannable createColorSubtext(CharSequence appName,
                                               int color) {
        final Spannable amened = new SpannableStringBuilder(appName);
        // 弄一个自己的颜色 TODO：不知道小米有没有这个 API，或者抄袭 AOSP 的实现
        amened.setSpan(new ForegroundColorSpan(color),
                0, amened.length(), 0);
        return amened;
    }


    private static PendingIntent openActivityPendingIntent(Context paramContext, XmPushActionContainer paramXmPushActionContainer, PushMetaInfo paramPushMetaInfo, byte[] paramArrayOfByte) {
        String packageName = paramXmPushActionContainer.getPackageName();
        PackageManager packageManager = paramContext.getPackageManager();
        Intent localIntent1 = packageManager.getLaunchIntentForPackage(packageName);
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

    /**
     * @see com.xiaomi.mipush.sdk.PushMessageProcessor#getNotificationMessageIntent
     */
    public static Intent getSdkIntent(Context context, String pkgName, PushMetaInfo paramPushMetaInfo) {

        Map<String, String> extra = paramPushMetaInfo.getExtra();
        if (extra == null) {
            return null;
        }

        if (!extra.containsKey(PushConstants.EXTRA_PARAM_NOTIFY_EFFECT)) {
            return null;
        }

        Intent intent = null;

        String typeId = (String) extra.get(PushConstants.EXTRA_PARAM_NOTIFY_EFFECT);
        if (PushConstants.NOTIFICATION_CLICK_DEFAULT.equals(typeId)) {
            try {
                intent = context.getPackageManager().getLaunchIntentForPackage(pkgName);
            } catch (Exception e2) {
                MyLog.e("Cause: " + e2.getMessage());
            }
        } else if (PushConstants.NOTIFICATION_CLICK_INTENT.equals(typeId)) {

            if (extra.containsKey(PushConstants.EXTRA_PARAM_INTENT_URI)) {
                String intentStr = extra.get(PushConstants.EXTRA_PARAM_INTENT_URI);
                if (intentStr != null) {
                    try {
                        intent = Intent.parseUri(intentStr, Intent.URI_INTENT_SCHEME);
                        intent.setPackage(pkgName);
                    } catch (URISyntaxException e3) {
                        MyLog.e("Cause: " + e3.getMessage());
                    }
                }
            } else {
                if (extra.containsKey(PushConstants.EXTRA_PARAM_CLASS_NAME)) {
                    String className = (String) extra.get(PushConstants.EXTRA_PARAM_CLASS_NAME);
                    intent = new Intent();
                    intent.setComponent(new ComponentName(pkgName, className));
                    try {
                        if (extra.containsKey(PushConstants.EXTRA_PARAM_INTENT_FLAG)) {
                            intent.setFlags(Integer.parseInt(extra.get(PushConstants.EXTRA_PARAM_INTENT_FLAG)));
                        }
                    } catch (NumberFormatException e4) {
                        MyLog.e("Cause by intent_flag: " + e4.getMessage());
                    }

                }
            }
        } else if (PushConstants.NOTIFICATION_CLICK_WEB_PAGE.equals(typeId)) {
            String uri = (String) extra.get(PushConstants.EXTRA_PARAM_WEB_URI);

            MalformedURLException e;

            if (uri != null) {
                String tmp = uri.trim();
                if (!(tmp.startsWith("http://") || tmp.startsWith("https://"))) {
                    tmp = "http://" + tmp;
                }
                try {
                    String protocol = new URL(tmp).getProtocol();
                    if (!"http".equals(protocol)) {
                        if (!"https".equals(protocol)) {
                            //why ?
                        }
                    }
                    Intent intent2 = new Intent("android.intent.action.VIEW");
                    intent2.setData(Uri.parse(tmp));
                    intent = intent2;
                } catch (MalformedURLException e6) {
                    e = e6;
                    MyLog.e("Cause: " + e.getMessage());
                    return null;
                }
            }
        }


        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (context.getPackageManager().resolveActivity(intent, 65536) != null) {
                //TODO fixit
//                String payload = actualMsg.getPayload();
//                if (!TextUtils.isEmpty(payload)) {
//                    intent.putExtra(PushServiceConstants.EXTENSION_ELEMENT_PAYLOAD, payload);
//                }

                return intent;
            }
        }

        return null;
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
