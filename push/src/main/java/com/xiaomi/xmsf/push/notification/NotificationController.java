package com.xiaomi.xmsf.push.notification;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.ColorUtils;
import com.xiaomi.xmsf.R;
import com.xiaomi.xmsf.utils.ColorUtil;
import top.trumeet.common.cache.ApplicationNameCache;
import top.trumeet.common.cache.IconCache;
import top.trumeet.common.utils.NotificationUtils;

import static top.trumeet.common.utils.NotificationUtils.getChannelIdByPkg;
import static top.trumeet.common.utils.NotificationUtils.getGroupIdByPkg;

/**
 * @author Trumeet
 * @date 2018/1/25
 */

public class NotificationController {

    private static final String NOTIFICATION_SMALL_ICON = "mipush_small_notification";

    private static final String ID_GROUP_APPLICATIONS = "applications";

    public static final String CHANNEL_WARN = "warn";

    @TargetApi(26)
    public static void deleteOldNotificationChannelGroup(@NonNull Context context) {
        try {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.deleteNotificationChannelGroup(ID_GROUP_APPLICATIONS);
        } catch (Exception ignore) {

        }

    }

    @TargetApi(26)
    private static NotificationChannelGroup createGroupWithPackage(@NonNull String packageName,
                                                                   @NonNull CharSequence name) {
        return new NotificationChannelGroup(getGroupIdByPkg(packageName), name);
    }

    @TargetApi(26)
    private static NotificationChannel createChannelWithPackage(@NonNull String packageName,
                                                                @NonNull CharSequence name) {
        NotificationChannel channel = new NotificationChannel(getChannelIdByPkg(packageName),
                name, NotificationManager.IMPORTANCE_DEFAULT);
        channel.enableVibration(true);
        return channel;
    }

    public static NotificationChannel registerChannelIfNeeded(Context context, String packageName) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return null;
        }

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = getChannelIdByPkg(packageName);
        NotificationChannel notificationChannel = manager.getNotificationChannel(channelId);

        if (notificationChannel != null) {
            if (ID_GROUP_APPLICATIONS.equals(notificationChannel.getGroup()) || TextUtils.isEmpty(notificationChannel.getGroup())) {
                manager.deleteNotificationChannel(channelId);
                notificationChannel = null;
            }
        }

        if (notificationChannel == null) {

            CharSequence name = ApplicationNameCache.getInstance().getAppName(context, packageName);
            if (name == null) {
                return null;
            }

            NotificationChannelGroup notificationChannelGroup = createGroupWithPackage(packageName, name);
            manager.createNotificationChannelGroup(notificationChannelGroup);

            notificationChannel = createChannelWithPackage(packageName, name);
            notificationChannel.setGroup(notificationChannelGroup.getId());

            manager.createNotificationChannel(notificationChannel);
        }

        return notificationChannel;

    }


    @TargetApi(Build.VERSION_CODES.N)
    private static void updateSummaryNotification(Context context, String packageName, String groupId) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        StatusBarNotification[] activeNotifications = manager.getActiveNotifications();

        
        int notificationCntInGroup = 0;
        for (StatusBarNotification statusBarNotification : activeNotifications) {
            if (groupId.equals(statusBarNotification.getNotification().getGroup())) {
                notificationCntInGroup++;
            }
        }

        if (notificationCntInGroup > 1) {

            CharSequence appName = ApplicationNameCache.getInstance().getAppName(context, packageName);

            Bundle extras = new Bundle();
            Notification.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder = new Notification.Builder(context, getChannelIdByPkg(packageName));
                builder.setGroupAlertBehavior(Notification.GROUP_ALERT_CHILDREN);
            } else {
                builder = new Notification.Builder(context);
            }

            int color = getIconColor(context, packageName);
            if (color != Notification.COLOR_DEFAULT) {
                CharSequence subText = ColorUtil.createColorSubtext(appName, color);
                if (subText != null) {
                    extras.putCharSequence(NotificationCompat.EXTRA_SUB_TEXT, subText);
                }
                builder.setColor(color);
            } else {
                extras.putCharSequence(NotificationCompat.EXTRA_SUB_TEXT, appName);
            }

            builder.setExtras(extras);

            // Set small icon
            NotificationController.processSmallIcon(context, packageName, builder);

            builder.setCategory(Notification.CATEGORY_EVENT)
                    .setGroupSummary(true)
                    .setGroup(groupId);
            Notification notification = builder.build();
            manager.notify(packageName.hashCode(), notification);
        } else {
            manager.cancel(packageName.hashCode());
        }
    }

    public static void publish(Context context, int id, String packageName, Notification.Builder localBuilder) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Make the behavior consistent with official MIUI
        Bundle extras = new Bundle();
        extras.putString("target_package", packageName);
        localBuilder.addExtras(extras);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Forward Compatibility
            registerChannelIfNeeded(context, packageName);

            localBuilder.setChannelId(getChannelIdByPkg(packageName));
            localBuilder.setGroup(getGroupIdByPkg(packageName));
            localBuilder.setGroupAlertBehavior(Notification.GROUP_ALERT_CHILDREN);
        } else {


            //for VERSION < Oero
            localBuilder.setDefaults(Notification.DEFAULT_ALL);
            localBuilder.setPriority(Notification.PRIORITY_HIGH);


            localBuilder.setGroup(getGroupIdByPkg(packageName));
        }

        Notification notification = localBuilder.build();
        manager.notify(id, notification);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            updateSummaryNotification(context, packageName, getGroupIdByPkg(packageName));
        }
    }


    public static void cancel(Context context, int id) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String groupId = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            StatusBarNotification[] activeNotifications = manager.getActiveNotifications();
            for (StatusBarNotification activeNotification : activeNotifications) {
                if (activeNotification.getId() == id) {
                    groupId = activeNotification.getNotification().getGroup();
                    break;
                }

            }
        }

        manager.cancel(id);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (groupId != null) {
                updateSummaryNotification(context, NotificationUtils.getPackageName(groupId), groupId);
            }
        }
    }


    /**
     * @param ctx context
     * @param pkg packageName
     * @return 0 if not processed
     */
    public static int getIconColor(final Context ctx, final String pkg) {
        return IconCache.getInstance().getAppColor(ctx, pkg, (ctx1, iconBitmap) -> {
            if (iconBitmap == null) {
                return Notification.COLOR_DEFAULT;
            }
            int color = ColorUtil.getIconColor(iconBitmap);
            if (color != Notification.COLOR_DEFAULT) {
                final float[] hsl = new float[3];
                ColorUtils.colorToHSL(color, hsl);
                hsl[1] = 0.94f;
                hsl[2] = Math.min(hsl[2] * 0.6f, 0.31f);
                return ColorUtils.HSLToColor(hsl);
            } else {
                return Notification.COLOR_DEFAULT;
            }
        });
    }


    public static void processSmallIcon(Context context, String packageName, Notification.Builder notificationBuilder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int iconSmallId = getIconId(context, packageName, NOTIFICATION_SMALL_ICON);
            if (iconSmallId <= 0) {

                Icon iconCache = IconCache.getInstance().getIconCache(context, packageName, (ctx, b) -> Icon.createWithBitmap(b));
                if (iconCache != null) {
                    notificationBuilder.setSmallIcon(iconCache);
                } else {
                    notificationBuilder.setSmallIcon(R.drawable.ic_notifications_black_24dp);
                }

            } else {
                notificationBuilder.setSmallIcon(Icon.createWithResource(packageName, iconSmallId));
            }
        } else {
            notificationBuilder.setSmallIcon(R.drawable.ic_notifications_black_24dp);
        }
    }


    public static void buildExtraSubText(Context var0, String packageName, Notification.Builder localBuilder, Bundle extras) {
        CharSequence appName = ApplicationNameCache.getInstance().getAppName(var0, packageName);
        int color = getIconColor(var0, packageName);
        if (color != Notification.COLOR_DEFAULT) {
            CharSequence subText = ColorUtil.createColorSubtext(appName, color);
            if (subText != null) {
                extras.putCharSequence(NotificationCompat.EXTRA_SUB_TEXT, subText);
            }
            localBuilder.setColor(color);
        } else {
            extras.putCharSequence(NotificationCompat.EXTRA_SUB_TEXT, appName);
        }
    }



    private static int getIconId(Context context, String str, String str2) {
        return context.getResources().getIdentifier(str2, "drawable", str);
    }



    public static void test(Context context, String packageName, String title, String description) {
        NotificationController.registerChannelIfNeeded(context, packageName);

        int id = (int) (System.currentTimeMillis() / 1000L);

        Notification.Builder localBuilder = new Notification.Builder(context);

        Notification.BigTextStyle style = new Notification.BigTextStyle();
        style.bigText(description);
        style.setBigContentTitle(title);
        style.setSummaryText(description);
        localBuilder.setStyle(style);
        NotificationController.processSmallIcon(context, packageName, localBuilder);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            localBuilder.setWhen(System.currentTimeMillis());
            localBuilder.setShowWhen(true);
        }

        NotificationController.publish(context, id, packageName, localBuilder);
    }

}
