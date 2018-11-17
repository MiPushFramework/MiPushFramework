package com.xiaomi.push.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.graphics.ColorUtils;
import android.text.TextUtils;

import com.xiaomi.channel.commonutils.logger.MyLog;
import com.xiaomi.channel.commonutils.reflect.JavaCalls;
import com.xiaomi.xmpush.thrift.PushMetaInfo;
import com.xiaomi.xmpush.thrift.XmPushActionContainer;
import com.xiaomi.xmsf.BuildConfig;
import com.xiaomi.xmsf.R;
import com.xiaomi.xmsf.push.notification.NotificationController;
import com.xiaomi.xmsf.utils.ColorUtil;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import me.pqpo.librarylog4a.Log4a;
import top.trumeet.common.cache.ApplicationNameCache;
import top.trumeet.common.cache.IconCache;

import static com.xiaomi.push.service.MIPushNotificationHelper.isBusinessMessage;

/**
 * @author zts1993
 * @date 2018/2/8
 */

public class MyMIPushNotificationHelper {

    private static final String NOTIFICATION_SMALL_ICON = "mipush_small_notification";

    private static final int NOTIFICATION_BIG_STYLE_MIN_LEN = 25;

    private static final String TAG = "MyNotificationHelper";

    /**
     * @see MIPushNotificationHelper#notifyPushMessage
     */
    static void notifyPushMessage(XMPushService var0, XmPushActionContainer buildContainer, byte[] var1, long var2) {
        PushMetaInfo metaInfo = buildContainer.getMetaInfo();

        String title = metaInfo.getTitle();
        String description = metaInfo.getDescription();

        int id = MyClientEventDispatcher.getNotificationId(buildContainer);

        Notification.Builder localBuilder = new Notification.Builder(var0);

        Log4a.i(TAG, "title:" + title + "  description:" + description);

        if (description.length() > NOTIFICATION_BIG_STYLE_MIN_LEN) {
            Notification.BigTextStyle style = new Notification.BigTextStyle();
            style.bigText(description);
            style.setBigContentTitle(title);
            style.setSummaryText(description);
            localBuilder.setStyle(style);
        }


        // Set small icon
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int iconSmallId = getIconId(var0, buildContainer.getPackageName(), NOTIFICATION_SMALL_ICON);
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

                Intent sdkIntentJump = getSdkIntent(var0, buildContainer.getPackageName(), buildContainer);
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


        //for VERSION < Oero
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            localBuilder.setDefaults(Notification.DEFAULT_ALL);
            localBuilder.setPriority(Notification.PRIORITY_HIGH);
        }

        // Fill app name
        Bundle extras = new Bundle();
        CharSequence appName = ApplicationNameCache.getInstance().getAppName(var0, buildContainer.getPackageName());

        int color = getIconColor(var0, buildContainer.getPackageName());
        if (color != -1 && !TextUtils.isEmpty(appName)) {
            CharSequence subText = ColorUtil.createColorSubtext(appName, color);
            if (subText != null) {
                extras.putCharSequence(NotificationCompat.EXTRA_SUB_TEXT, subText);
            }
            localBuilder.setColor(color);
        }

        localBuilder.setExtras(extras);

        NotificationController.publish(var0, id, buildContainer.getPackageName(), localBuilder);

    }


    /**
     * @param ctx context
     * @param pkg packageName
     * @return -1 if not processed
     */
    private static int getIconColor(final Context ctx, final String pkg) {
        return IconCache.getInstance().getAppColor(ctx, pkg, (ctx1, iconBitmap) -> {
            if (iconBitmap == null) {
                return -1;
            }
            int color = ColorUtil.getIconColor(iconBitmap);
            if (color != Notification.COLOR_DEFAULT) {
                final float[] hsl = new float[3];
                ColorUtils.colorToHSL(color, hsl);
                hsl[1] = 0.94f;
                hsl[2] = Math.min(hsl[2] * 0.6f, 0.31f);
                return ColorUtils.HSLToColor(hsl);
            } else {
                return -1;
            }
        });
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
            localIntent.putExtra(PushConstants.MIPUSH_EXTRA_PAYLOAD, paramArrayOfByte);
            localIntent.putExtra(MIPushNotificationHelper.FROM_NOTIFICATION, true);
            localIntent.addCategory(String.valueOf(paramPushMetaInfo.getNotifyId()));
            localPendingIntent = PendingIntent.getService(paramContext, id, localIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            Intent localIntent = new Intent();
            localIntent.setComponent(new ComponentName("com.xiaomi.xmsf", "com.xiaomi.push.sdk.MyPushMessageHandler"));
            localIntent.putExtra(PushConstants.MIPUSH_EXTRA_PAYLOAD, paramArrayOfByte);
            localIntent.putExtra(MIPushNotificationHelper.FROM_NOTIFICATION, true);
            localIntent.addCategory(String.valueOf(paramPushMetaInfo.getNotifyId()));
            localPendingIntent = PendingIntent.getService(paramContext, id, localIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        }
        return localPendingIntent;
    }

    /**
     * @see com.xiaomi.mipush.sdk.PushMessageProcessor#getNotificationMessageIntent
     */
    public static Intent getSdkIntent(Context context, String pkgName, XmPushActionContainer container) {
        PushMetaInfo paramPushMetaInfo = container.getMetaInfo();
        Map<String, String> extra = paramPushMetaInfo.getExtra();
        if (extra == null) {
            return null;
        }

        if (!extra.containsKey(PushConstants.EXTRA_PARAM_NOTIFY_EFFECT)) {
            return null;
        }

        Intent intent = null;

        String typeId = extra.get(PushConstants.EXTRA_PARAM_NOTIFY_EFFECT);
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
            String uri = extra.get(PushConstants.EXTRA_PARAM_WEB_URI);

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
            if (context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
                //TODO fixit

                //we don't have RegSecret we cannot decode push action

                if (inFetchIntentBlackList(pkgName)) {
                    return null;
                }

                return intent;
            }
        }

        return null;
    }

    /**
     * tmp black list
     *
     * @param pkg package name
     * @return is in black list
     */
    private static boolean inFetchIntentBlackList(String pkg) {
        if (pkg.contains("youku")) {
            return true;
        }

        return false;
    }


    private static PendingIntent startServicePendingIntent(Context paramContext, XmPushActionContainer paramXmPushActionContainer, PushMetaInfo paramPushMetaInfo, byte[] paramArrayOfByte) {
        if (paramPushMetaInfo == null) {
            return null;
        }
        PendingIntent localPendingIntent;

        if (isBusinessMessage(paramXmPushActionContainer)) {
            Intent localIntent = new Intent();
            localIntent.setComponent(new ComponentName("com.xiaomi.xmsf", "com.xiaomi.mipush.sdk.PushMessageHandler"));
            localIntent.putExtra(PushConstants.MIPUSH_EXTRA_PAYLOAD, paramArrayOfByte);
            localIntent.putExtra(MIPushNotificationHelper.FROM_NOTIFICATION, true);
            localIntent.addCategory(String.valueOf(paramPushMetaInfo.getNotifyId()));
            localPendingIntent = PendingIntent.getService(paramContext, 0, localIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            Intent localIntent = new Intent("com.xiaomi.mipush.RECEIVE_MESSAGE");
            localIntent.setComponent(new ComponentName(paramXmPushActionContainer.packageName, "com.xiaomi.mipush.sdk.PushMessageHandler"));
            localIntent.putExtra(PushConstants.MIPUSH_EXTRA_PAYLOAD, paramArrayOfByte);
            localIntent.putExtra(MIPushNotificationHelper.FROM_NOTIFICATION, true);
            localIntent.addCategory(String.valueOf(paramPushMetaInfo.getNotifyId()));
            localPendingIntent = PendingIntent.getService(paramContext, 0, localIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        return localPendingIntent;
    }

    /**
     * @see MIPushNotificationHelper#determineTitleAndDespByDIP
     */
    private static String[] determineTitleAndDespByDIP(Context paramContext, PushMetaInfo paramPushMetaInfo) {

        try {
            return JavaCalls.callStaticMethodOrThrow(MIPushNotificationHelper.class, "determineTitleAndDespByDIP", paramContext, paramPushMetaInfo);
        } catch (Exception e) {
            Log4a.e(TAG, e.getMessage(), e);
            return new String[]{paramPushMetaInfo.getTitle(), paramPushMetaInfo.getDescription()};
        }
    }

    private static int getIconId(Context context, String str, String str2) {
        return context.getResources().getIdentifier(str2, "drawable", str);
    }


}
