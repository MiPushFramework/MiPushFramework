package com.xiaomi.xmsf.push.notification;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManagerExtender;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Created by Trumeet on 2018/1/25.
 *
 * 在奥利奥上面给对应包的通知补上 channel 们
 */

@TargetApi(26)
public class OreoNotificationManager extends NotificationManagerExtender {
    private static final String TAG = "OreoNotificationManager";

    private HashMap<Integer /* notification code*/, String /* package name */>
    mRegisterMap;

    public OreoNotificationManager(Context context) {
        super(context);
    }

    private String poll (int notificationId) {
        Log.d(TAG, "poll: " + notificationId);
        if (mRegisterMap == null) {
            Log.d(TAG, "poll: null");
            return null;
        }
        String pkg = mRegisterMap.get(notificationId);
        if (pkg != null) {
            mRegisterMap.remove(notificationId);
        }
        Log.d(TAG, "poll: " + pkg);
        return pkg;
    }

    /**
     * Set channel to {@link Notification} via reflection
     * Bad Google
     * {@link Notification#mChannelId}
     */
    private Notification setChannelId (Notification notification, int id) {
        if (mRegisterMap == null || mRegisterMap.isEmpty() || Build.VERSION.SDK_INT < 26)
            return notification;
        String apk = poll(id);
        if (apk == null)
            return notification;
        try {
            Field mChannelIdField = notification.getClass().getDeclaredField("mChannelId");
            mChannelIdField.setAccessible(true);
            mChannelIdField.set(notification, NotificationController.channelId(apk));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return notification;
    }

    @Override public void notify(final int id, final Notification notification) {
        super.notify(id, setChannelId(notification, id));
    }

    @Override public void notify(final String tag, final int id, final Notification notification) {
        super.notify(tag, id, setChannelId(notification, id));
    }

    @SuppressLint("UseSparseArrays")
    public void register (int id, String pkg) {
        Log.d(TAG, "register: " + id + ":" + pkg);
        if (mRegisterMap == null)
            mRegisterMap = new HashMap<>(1);
        mRegisterMap.put(id, pkg);
    }

    public void unregister (int id) {
        if (mRegisterMap == null)
            return;
        mRegisterMap.remove(id);
        if (mRegisterMap.isEmpty())
            mRegisterMap = null;
    }
}
