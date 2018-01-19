package top.trumeet.common.push;

import android.annotation.TargetApi;
import android.annotation.WorkerThread;
import android.app.AppOpsManager;
import android.content.Context;
import android.os.Build;
import android.os.PowerManager;

import java.util.concurrent.CountDownLatch;

import top.trumeet.common.Constants;

/**
 * Created by Trumeet on 2017/8/25.
 * A util class to check XMPush accessibility
 */

public class PushServiceAccessibility {
    private static final String TAG = "Accessibility";

    /**
     * Check this app is in system doze whitelist.
     * @param context Context param
     * @return is in whitelist, always true when pre-marshmallow
     */
    @TargetApi(Build.VERSION_CODES.M)
    public static boolean isInDozeWhiteList (Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true;
        PowerManager powerManager = context.getSystemService(PowerManager.class);
        return powerManager.isIgnoringBatteryOptimizations(Constants.SERVICE_APP_NAME);
    }

    /**
     * Check this app is in allowed background activity.
     * @param context Context param
     * @return allowed status, always true when pre-oreo
     */
    @WorkerThread
    public static boolean checkAllowRunInBackground (Context context) {
        try {
            final PushController controller = new PushController();
            final CountDownLatch latch = new CountDownLatch(1);
            final CheckOpResult result = new CheckOpResult();
            controller.connect(context,
                    new PushController.OnReadyListener() {
                        @TargetApi(19)
                        @Override
                        public void onReady() {
                            result.result = controller.checkOp(AppOpsManager.OP_RUN_IN_BACKGROUND);
                            latch.countDown();
                        }
                    });
            latch.await();
            controller.disconnect();
            return result.result == null || (result.result == AppOpsManager.MODE_ALLOWED);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // Too bad
    private static class CheckOpResult {
        private Integer result;
    }
}
