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

import static android.app.Notification.EXTRA_TITLE;
import static com.xiaomi.push.service.MyMIPushNotificationHelper.createColorSubtext;
import static top.trumeet.common.utils.NotificationUtils.getChannelIdByPkg;
import static top.trumeet.common.utils.NotificationUtils.getGroupIdByPkg;

/**
 * @author Trumeet
 * @date 2018/1/25
 */

public class NotificationController {
    private static final String ID_GROUP_APPLICATIONS = "applications";

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

        CharSequence name = ApplicationNameCache.getInstance().getAppName(context, packageName);
        if (name == null) {
            return null;
        }

        String channelId = getChannelIdByPkg(packageName);
        NotificationChannel notificationChannel = manager.getNotificationChannel(channelId);

        if (notificationChannel != null) {
            if (ID_GROUP_APPLICATIONS.equals(notificationChannel.getGroup()) || TextUtils.isEmpty(notificationChannel.getGroup())) {
                manager.deleteNotificationChannel(channelId);
                notificationChannel = null;
            }
        }

        if (notificationChannel == null) {
            NotificationChannelGroup notificationChannelGroup = createGroupWithPackage(packageName, name);
            manager.createNotificationChannelGroup(notificationChannelGroup);

            notificationChannel = createChannelWithPackage(packageName, name);
            notificationChannel.setGroup(notificationChannelGroup.getId());

            manager.createNotificationChannel(notificationChannel);
        }


        return notificationChannel;

    }


    @TargetApi(Build.VERSION_CODES.O)
    private static void sendSummaryNotification(Context context, String packageName) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        StatusBarNotification[] activeNotifications = manager.getActiveNotifications();
        if (activeNotifications == null || activeNotifications.length == 0) {
            return;
        }
        String groupId = getGroupIdByPkg(packageName);
        ArrayList<StatusBarNotification> statusBarNotifications = new ArrayList<>();

        for (StatusBarNotification statusBarNotification : activeNotifications) {
            if (groupId.equals(statusBarNotification.getNotification().getGroup())) {
                statusBarNotifications.add(statusBarNotification);
            }
        }

        CharSequence appName = ApplicationNameCache.getInstance().getAppName(context, packageName);

        if (statusBarNotifications.size() > 1) {
            Notification notifyDefault = statusBarNotifications.get(0).getNotification();

            Notification.Builder builder = new Notification.Builder(context);
            Notification.InboxStyle inboxStyle = new Notification.InboxStyle().setBigContentTitle(appName);

            for (StatusBarNotification statusBarNotification : statusBarNotifications) {
                Bundle extras = statusBarNotification.getNotification().extras;
                if (extras != null && extras.size() > 0) {
                    CharSequence title = (CharSequence) extras.get(EXTRA_TITLE);
                    if (title != null) {
                        inboxStyle.addLine(title);
                    }

                }
            }

            Bundle extras = new Bundle();
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
                    .setChannelId(notifyDefault.getChannelId())
                    .setGroup(groupId);
            Notification notification = builder.build();
            manager.notify(packageName.hashCode(), notification);
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

        }

        Notification notification = localBuilder.build();
        manager.notify(id, notification);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            sendSummaryNotification(context, packageName);
        }
    }


    public static void cancel(Context context, int id) {
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(id);

    }
}
