package com.xiaomi.xmsf.push.control;

import android.app.Notification;
import android.app.NotificationManagerExtender;
import android.content.Context;

import com.oasisfeng.condom.CondomKit;

import me.pqpo.librarylog4a.Log4a;

/**
 * Created by Trumeet on 2018/1/23.
 */

public class NotificationManagerKit implements CondomKit {
    @Override
    public void onRegister(CondomKitRegistry registry) {
        registry.registerSystemService(Context.NOTIFICATION_SERVICE, new CondomKit.SystemServiceSupplier() {

            @Override
            public Object getSystemService(Context context, String name) {
                if (Context.NOTIFICATION_SERVICE.equals(name))
                    return new CondomNotificationManager(context);
                return null;
            }
        });
    }

    static class CondomNotificationManager extends NotificationManagerExtender {
        private static final String TAG = "CondomNotificationManager";
        CondomNotificationManager(Context context) {
            super(context);
        }

        @Override public void notify(final int id, final Notification notification) {
            Log4a.d(TAG, "notify -> " + id + ", " + notification);
            super.notify(id, notification);
        }

        @Override public void notify(final String tag, final int id, final Notification notification) {
            Log4a.d(TAG, "notify -> " + tag + ", " + id + ", " + notification);
            super.notify(tag, id, notification);
        }

        @Override public void cancel(final int id) {
            Log4a.d(TAG, "cancel -> " + id);
            super.cancel(id);
        }

        @Override public void cancel(final String tag, final int id) {
            Log4a.d(TAG, "cancel -> " + tag + ", " + id);
            super.cancel(tag, id);
        }

        @Override public void cancelAll() {
            Log4a.d(TAG, "cancelAll");
            super.cancelAll();
        }
    }
}
