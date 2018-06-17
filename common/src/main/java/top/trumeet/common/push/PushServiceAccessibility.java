package top.trumeet.common.push;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.PowerManager;

import top.trumeet.common.Constants;

/**
 * Created by Trumeet on 2017/8/25.
 * A util class to check XMPush accessibility
 */

public class PushServiceAccessibility {

    /**
     * Check this app is in system doze whitelist.
     *
     * @param context Context param
     * @return is in whitelist, always true when pre-marshmallow
     */
    @TargetApi(Build.VERSION_CODES.M)
    public static boolean isInDozeWhiteList(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        PowerManager powerManager = context.getSystemService(PowerManager.class);
        return powerManager.isIgnoringBatteryOptimizations(Constants.SERVICE_APP_NAME);
    }
}
