package top.trumeet.common.ita.impl;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

import top.trumeet.common.ita.DetectionService;
import top.trumeet.common.ita.ITopActivity;


/**
 * Created by zts1993 on 2018/2/18.
 */

public class ActivityAccessibilityImpl implements ITopActivity {
    private final static String TAG = "ActivityAccessibility";

    @Override
    public boolean isEnabled(Context context) {
        int accessibilityEnabled = 0;
        try {
            accessibilityEnabled = Settings.Secure.getInt(context.getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        if (accessibilityEnabled == 1) {
            String services = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (services != null) {
                return services.toLowerCase().contains(context.getPackageName().toLowerCase());
            }
        }

        return false;
    }


    @Override
    public void guideToEnable(Context context) {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public boolean isAppForeground(Context context, String packageName) {
        return packageName.equals(DetectionService.getForegroundPackageName());
    }

}
