package android.app;

import android.content.Context;
import android.os.Handler;

import com.oasisfeng.condom.CondomKit;

import me.pqpo.librarylog4a.Log4a;

/**
 * Created by Trumeet on 2018/1/19.
 */

public class NotificationKit implements CondomKit, CondomKit.SystemServiceSupplier {
    private static final String TAG = "NotificationKit";

    @Override
    public Object getSystemService(Context context, String name) {
        if (Context.NOTIFICATION_SERVICE.equals(name)) {
            Log4a.d(TAG, "patching notification manager");
            return new CondomNotificationManager(context
                    , null);
        }
        return null;
    }

    @Override
    public void onRegister(CondomKitRegistry registry) {
        registry.registerSystemService(Context.NOTIFICATION_SERVICE, this);
    }

    class CondomNotificationManager extends NotificationManager {
        CondomNotificationManager(Context context, Handler handler) {
            super(context, handler);
        }

        @Override
        public void notify (int id, Notification notification) {
            Log4a.d(TAG, "notify -> " + id + ";" + notification.toString());
        }
    }

}
