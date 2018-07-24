package com.xiaomi.xmsf.push.notification;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import java.util.ArrayList;

import top.trumeet.common.cache.ApplicationNameCache;
import top.trumeet.common.utils.NotificationUtils;

import static com.xiaomi.push.service.MyMIPushNotificationHelper.createColorSubtext;
import static top.trumeet.common.utils.NotificationUtils.getChannelIdByPkg;
import static top.trumeet.common.utils.NotificationUtils.getGroupIdByPkg;

/**
 * @author Trumeet
 * @date 2018/1/25
 */

public class NotificationController {
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


    @TargetApi(Build.VERSION_CODES.O)
    private static void updateSummaryNotification(Context context, String packageName, String groupId) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        StatusBarNotification[] activeNotifications = manager.getActiveNotifications();

        ArrayList<StatusBarNotification> statusBarNotifications = new ArrayList<>();

        for (StatusBarNotification statusBarNotification : activeNotifications) {
            if (groupId.equals(statusBarNotification.getNotification().getGroup())) {
                statusBarNotifications.add(statusBarNotification);
            }
        }

        if (statusBarNotifications.size() > 1) {

            CharSequence appName = ApplicationNameCache.getInstance().getAppName(context, packageName);
            Notification notifyDefault = statusBarNotifications.get(0).getNotification();

            Bundle extras = new Bundle();
            Notification.Builder builder = new Notification.Builder(context, notifyDefault.getChannelId());
            int color = notifyDefault.color;

            CharSequence subText = createColorSubtext(appName, color);
            if (subText != null) {
                extras.putCharSequence(NotificationCompat.EXTRA_SUB_TEXT, subText);
            }
            builder.setColor(color);
            builder.setExtras(extras);

            builder.setSmallIcon(notifyDefault.getSmallIcon())
                    .setLargeIcon(notifyDefault.getLargeIcon())
                    .setCategory(Notification.CATEGORY_EVENT)
                    .setGroupSummary(true)
                     .setGroupAlertBehavior(Notification.GROUP_ALERT_CHILDREN)
                    .setChannelId(notifyDefault.getChannelId())
                    .setGroup(groupId);
            Notification notification = builder.build();
            manager.notify(packageName.hashCode(), notification);
        } else {
            manager.cancel(packageName.hashCode());
        }

    }

    public static void publish(Context context, int id, String packageName, Notification.Builder localBuilder) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Forward Compatibility
            registerChannelIfNeeded(context, packageName);

            localBuilder.setChannelId(getChannelIdByPkg(packageName));
            localBuilder.setGroup(getGroupIdByPkg(packageName));
            localBuilder.setGroupAlertBehavior(Notification.GROUP_ALERT_ALL);
        } else {
            localBuilder.setGroup(getGroupIdByPkg(packageName));
        }

        Notification notification = localBuilder.build();
        manager.notify(id, notification);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
}
