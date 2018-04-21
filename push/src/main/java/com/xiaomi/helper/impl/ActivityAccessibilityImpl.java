package com.xiaomi.helper.impl;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

import com.xiaomi.helper.DetectionService;
import com.xiaomi.helper.ITopActivity;

import me.pqpo.librarylog4a.Log4a;

/**
 * Created by zts1993 on 2018/2/18.
 */

public class ActivityAccessibilityImpl implements ITopActivity {
    final static String TAG = "ActivityAccessibilityImpl";

    @Override
    public boolean isEnabled(Context context) {
        int accessibilityEnabled = 0;
        try {
            accessibilityEnabled = Settings.Secure.getInt(context.getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            Log4a.e(TAG, e.getMessage(), e);
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
        context.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
    }

    public boolean isAppForeground(Context context, String packageName) {
        return packageName.equals(DetectionService.getForegroundPackageName());
    }

}
