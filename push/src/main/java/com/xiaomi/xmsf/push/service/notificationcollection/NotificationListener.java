package com.xiaomi.xmsf.push.service.notificationcollection;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Intent;
import android.os.Build.VERSION;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;

import com.xiaomi.channel.commonutils.misc.CollectionUtils;
import com.xiaomi.channel.commonutils.reflect.JavaCalls;
import com.xiaomi.push.service.OnlineConfig;
import com.xiaomi.xmpush.thrift.ConfigKey;
import com.xiaomi.xmpush.thrift.NotificationBarInfo;
import com.xiaomi.xmpush.thrift.NotificationBarInfoItem;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@TargetApi(19)
public class NotificationListener extends NotificationListenerService {
    private static NotificationBarInfo notificationInfo = new NotificationBarInfo();
    private int maxSize = 200;

    private NotificationBarInfoItem buildItem(int i, StatusBarNotification statusBarNotification) {
        int i2 = 0;
        if (statusBarNotification == null || statusBarNotification.getPackageName() == null) {
            return null;
        }
        NotificationBarInfoItem notificationBarInfoItem = new NotificationBarInfoItem();
        Notification notification = statusBarNotification.getNotification();
        if (notification == null) {
            return null;
        }
        notificationBarInfoItem.setType(i);
        notificationBarInfoItem.setPackageName(statusBarNotification.getPackageName());
        notificationBarInfoItem.setNotifyId(statusBarNotification.getId());
        if (notification.extras != null) {
            notificationBarInfoItem.setTitle(notification.extras.getString("android.title", ""));
            notificationBarInfoItem.setContent(notification.extras.getString("android.text", ""));
        }
        notificationBarInfoItem.setCustomLayoutIsSet(notification.contentView != null);
        Object callMethod = JavaCalls.callMethod(notification.contentIntent, "getIntent", new Object[0]);
        if (callMethod != null && (callMethod instanceof Intent)) {
            notificationBarInfoItem.setIntentUri(((Intent) callMethod).toUri(1));
        }
        notificationBarInfoItem.setArrivedTime(notification.when / 1000);
        notificationBarInfoItem.setRemovedTime(i == 2 ? System.currentTimeMillis() / 1000 : -1);
        notificationBarInfoItem.setFlags(notification.flags);
        notificationBarInfoItem.setPriority(notification.priority);
        if (notification.actions != null) {
            i2 = notification.actions.length;
        }
        notificationBarInfoItem.setActions(i2);
        notificationBarInfoItem.setDefaults(notification.defaults);
        if (VERSION.SDK_INT < 21) {
            return notificationBarInfoItem;
        }
        try {
            notificationBarInfoItem.setVisibility(((Integer) JavaCalls.getField(notification, "visibility")).intValue());
            notificationBarInfoItem.setCategory((String) JavaCalls.getField(notification, "category"));
            notificationBarInfoItem.setColor(((Integer) JavaCalls.getField(notification, "color")).intValue());
            return notificationBarInfoItem;
        } catch (Exception e) {
            return notificationBarInfoItem;
        }
    }

    private boolean checkAppSwitch(OnlineConfig onlineConfig, StatusBarNotification statusBarNotification) {
        boolean z = true;
        String stringValue = onlineConfig.getStringValue(ConfigKey.CollectionNotificationInfoAppSwitch.getValue(), "b");
        if (TextUtils.isEmpty(stringValue)) {
            return false;
        }
        List emptyList = Collections.emptyList();
        if (stringValue.length() > 1) {
            emptyList = Arrays.asList(stringValue.substring(1).split(";"));
        }
        String packageName = statusBarNotification.getPackageName();
        char charAt = stringValue.charAt(0);
        if (charAt != 'b') {
            return charAt == 'w' ? emptyList.contains(packageName) : false;
        } else {
            if (emptyList.contains(packageName)) {
                z = false;
            }
            return z;
        }
    }

    private boolean checkBaseSwitch(OnlineConfig onlineConfig) {
        return onlineConfig.getBooleanValue(ConfigKey.CollectionNotificationInfoBaseSwitch.getValue(), false);
    }

    private boolean checkRemovedSwitch(OnlineConfig onlineConfig, int i) {
        return i != 2 || onlineConfig.getBooleanValue(ConfigKey.CollectionNotificationInfoRemovedSwitch.getValue(), true);
    }

    private boolean checkSwitch(int i, StatusBarNotification statusBarNotification) {
        OnlineConfig instance = OnlineConfig.getInstance(getApplicationContext());
        return checkBaseSwitch(instance) && checkAppSwitch(instance, statusBarNotification) && checkRemovedSwitch(instance, i);
    }

    private NotificationBarInfoItem findExistItem(StatusBarNotification statusBarNotification) {
        if (statusBarNotification == null || statusBarNotification.getPackageName() == null) {
            return null;
        }
        if (CollectionUtils.isEmpty(notificationInfo.getData())) {
            return null;
        }
        CharSequence packageName = statusBarNotification.getPackageName();
        int id = statusBarNotification.getId();
        for (NotificationBarInfoItem notificationBarInfoItem : notificationInfo.getData()) {
            if (id == notificationBarInfoItem.getNotifyId() && TextUtils.equals(packageName, notificationBarInfoItem.getPackageName()) && notificationBarInfoItem.getType() == 1) {
                return notificationBarInfoItem;
            }
        }
        return null;
    }

    public static synchronized NotificationBarInfo takeNotificationInfo() {
        NotificationBarInfo notificationBarInfo;
        synchronized (NotificationListener.class) {
            notificationBarInfo = notificationInfo;
            notificationBarInfo.version = 1;
            notificationInfo = new NotificationBarInfo();
        }
        return notificationBarInfo;
    }

    public void onCreate() {
        this.maxSize = OnlineConfig.getInstance(getApplicationContext()).getIntValue(ConfigKey.UploadNotificationInfoMaxNum.getValue(), 200);
    }
    // ERROR //
    public void onNotificationPosted(StatusBarNotification paramStatusBarNotification)
    {
        // Byte code:
        //   0: aload_0
        //   1: iconst_1
        //   2: aload_1
        //   3: invokespecial 328	com/xiaomi/xmsf/push/service/notificationcollection/NotificationListener:checkSwitch	(ILandroid/service/notification/StatusBarNotification;)Z
        //   6: ifne +4 -> 10
        //   9: return
        //   10: ldc 2
        //   12: monitorenter
        //   13: getstatic 20	com/xiaomi/xmsf/push/service/notificationcollection/NotificationListener:notificationInfo	Lcom/xiaomi/xmpush/thrift/NotificationBarInfo;
        //   16: invokevirtual 331	com/xiaomi/xmpush/thrift/NotificationBarInfo:getDataSize	()I
        //   19: aload_0
        //   20: getfield 23	com/xiaomi/xmsf/push/service/notificationcollection/NotificationListener:maxSize	I
        //   23: if_icmplt +15 -> 38
        //   26: ldc 2
        //   28: monitorexit
        //   29: goto -20 -> 9
        //   32: astore_2
        //   33: ldc 2
        //   35: monitorexit
        //   36: aload_2
        //   37: athrow
        //   38: aload_0
        //   39: iconst_1
        //   40: aload_1
        //   41: invokespecial 333	com/xiaomi/xmsf/push/service/notificationcollection/NotificationListener:buildItem	(ILandroid/service/notification/StatusBarNotification;)Lcom/xiaomi/xmpush/thrift/NotificationBarInfoItem;
        //   44: astore_3
        //   45: aload_3
        //   46: ifnull +10 -> 56
        //   49: getstatic 20	com/xiaomi/xmsf/push/service/notificationcollection/NotificationListener:notificationInfo	Lcom/xiaomi/xmpush/thrift/NotificationBarInfo;
        //   52: aload_3
        //   53: invokevirtual 337	com/xiaomi/xmpush/thrift/NotificationBarInfo:addToData	(Lcom/xiaomi/xmpush/thrift/NotificationBarInfoItem;)V
        //   56: ldc 2
        //   58: monitorexit
        //   59: goto -50 -> 9
        //
        // Exception table:
        //   from	to	target	type
        //   13	36	32	finally
        //   38	59	32	finally
    }

    // ERROR //
    public void onNotificationRemoved(StatusBarNotification paramStatusBarNotification)
    {
        // Byte code:
        //   0: aload_0
        //   1: iconst_2
        //   2: aload_1
        //   3: invokespecial 328	com/xiaomi/xmsf/push/service/notificationcollection/NotificationListener:checkSwitch	(ILandroid/service/notification/StatusBarNotification;)Z
        //   6: ifne +4 -> 10
        //   9: return
        //   10: ldc 2
        //   12: monitorenter
        //   13: aload_0
        //   14: aload_1
        //   15: invokespecial 340	com/xiaomi/xmsf/push/service/notificationcollection/NotificationListener:findExistItem	(Landroid/service/notification/StatusBarNotification;)Lcom/xiaomi/xmpush/thrift/NotificationBarInfoItem;
        //   18: astore_3
        //   19: aload_3
        //   20: ifnull +32 -> 52
        //   23: aload_3
        //   24: iconst_3
        //   25: putfield 343	com/xiaomi/xmpush/thrift/NotificationBarInfoItem:type	I
        //   28: aload_3
        //   29: invokestatic 126	java/lang/System:currentTimeMillis	()J
        //   32: ldc2_w 115
        //   35: ldiv
        //   36: invokevirtual 129	com/xiaomi/xmpush/thrift/NotificationBarInfoItem:setRemovedTime	(J)Lcom/xiaomi/xmpush/thrift/NotificationBarInfoItem;
        //   39: pop
        //   40: ldc 2
        //   42: monitorexit
        //   43: goto -34 -> 9
        //   46: astore_2
        //   47: ldc 2
        //   49: monitorexit
        //   50: aload_2
        //   51: athrow
        //   52: getstatic 20	com/xiaomi/xmsf/push/service/notificationcollection/NotificationListener:notificationInfo	Lcom/xiaomi/xmpush/thrift/NotificationBarInfo;
        //   55: invokevirtual 331	com/xiaomi/xmpush/thrift/NotificationBarInfo:getDataSize	()I
        //   58: aload_0
        //   59: getfield 23	com/xiaomi/xmsf/push/service/notificationcollection/NotificationListener:maxSize	I
        //   62: if_icmplt +9 -> 71
        //   65: ldc 2
        //   67: monitorexit
        //   68: goto -59 -> 9
        //   71: aload_0
        //   72: iconst_2
        //   73: aload_1
        //   74: invokespecial 333	com/xiaomi/xmsf/push/service/notificationcollection/NotificationListener:buildItem	(ILandroid/service/notification/StatusBarNotification;)Lcom/xiaomi/xmpush/thrift/NotificationBarInfoItem;
        //   77: astore 4
        //   79: aload 4
        //   81: ifnull +11 -> 92
        //   84: getstatic 20	com/xiaomi/xmsf/push/service/notificationcollection/NotificationListener:notificationInfo	Lcom/xiaomi/xmpush/thrift/NotificationBarInfo;
        //   87: aload 4
        //   89: invokevirtual 337	com/xiaomi/xmpush/thrift/NotificationBarInfo:addToData	(Lcom/xiaomi/xmpush/thrift/NotificationBarInfoItem;)V
        //   92: ldc 2
        //   94: monitorexit
        //   95: goto -86 -> 9
        //
        // Exception table:
        //   from	to	target	type
        //   13	50	46	finally
        //   52	95	46	finally
    }
}
