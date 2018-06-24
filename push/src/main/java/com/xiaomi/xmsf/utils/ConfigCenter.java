package com.xiaomi.xmsf.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.crossbowffs.remotepreferences.RemotePreferenceAccessException;

import top.trumeet.common.utils.PreferencesUtils;

/**
 * @author zts
 */
public class ConfigCenter {

    private static volatile ConfigCenter conf = new ConfigCenter();

    public static ConfigCenter getInstance() {
        return conf;
    }

    public static void reloadConf(Context ctx) {
        ConfigCenter tmp = new ConfigCenter();
        try {
            SharedPreferences prefs = PreferencesUtils.getPreferences(ctx);
            tmp.autoRegister = prefs.getBoolean(PreferencesUtils.KeyAutoRegister, tmp.autoRegister);
            tmp.debugIntent = prefs.getBoolean(PreferencesUtils.KeyDebugIntent, tmp.debugIntent);
            tmp.foregroundNotification = prefs.getBoolean(PreferencesUtils.KeyForegroundNotification, tmp.foregroundNotification);
            tmp.enableWakeupTarget = prefs.getBoolean(PreferencesUtils.KeyEnableWakeupTarget, tmp.enableWakeupTarget);
            tmp.disablePushNotification = prefs.getBoolean(PreferencesUtils.KeyDisablePushNotification, tmp.disablePushNotification);
            tmp.enableGroupNotification = prefs.getBoolean(PreferencesUtils.KeyEnableGroupNotification, tmp.enableGroupNotification);

            {
                String mode = prefs.getString(PreferencesUtils.KeyAccessMode, "0");
                tmp.accessMode = Integer.valueOf(mode);
            }

            conf = tmp;
        } catch (RemotePreferenceAccessException ignored) {
        }
    }


    public boolean autoRegister = true;
    public boolean debugIntent = false;
    public boolean foregroundNotification = true;
    public boolean enableWakeupTarget = true;
    public boolean disablePushNotification = false;
    public boolean enableGroupNotification = true;

    public int accessMode = 0;

}
