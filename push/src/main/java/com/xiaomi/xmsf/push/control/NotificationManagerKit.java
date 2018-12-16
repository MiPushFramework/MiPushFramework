package com.xiaomi.xmsf.push.control;

import android.app.Notification;
import android.app.NotificationManagerExtender;
import android.content.Context;

import com.elvishew.xlog.Logger;
import com.elvishew.xlog.XLog;
import com.oasisfeng.condom.CondomKit;



/**
 *
 * @author Trumeet
 * @date 2018/1/23
 */

public class NotificationManagerKit implements CondomKit {
    @Override
    public void onRegister(CondomKitRegistry registry) {
        registry.registerSystemService(Context.NOTIFICATION_SERVICE, new CondomKit.SystemServiceSupplier() {

            @Override
            public Object getSystemService(Context context, String name) {
                if (Context.NOTIFICATION_SERVICE.equals(name)) {
                    return new CondomNotificationManager(context);
                }
                return null;
            }
        });
    }

    static class CondomNotificationManager extends NotificationManagerExtender {
        private final Logger logger = XLog.tag("CondomNotificationManager").build();
        CondomNotificationManager(Context context) {
            super(context);
        }

        @Override public void notify(final int id, final Notification notification) {
            logger.d("notify -> " + id + ", " + notification);
            super.notify(id, notification);
        }

        @Override public void notify(final String tag, final int id, final Notification notification) {
            logger.d("notify -> " + tag + ", " + id + ", " + notification);
            super.notify(tag, id, notification);
        }

        @Override public void cancel(final int id) {
            logger.d("cancel -> " + id);
            super.cancel(id);
        }

        @Override public void cancel(final String tag, final int id) {
            logger.d("cancel -> " + tag + ", " + id);
            super.cancel(tag, id);
        }

        @Override public void cancelAll() {
            logger.d("cancelAll");
            super.cancelAll();
        }
    }
}
