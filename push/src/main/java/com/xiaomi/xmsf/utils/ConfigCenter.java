package com.xiaomi.xmsf.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.xiaomi.xmsf.R;

import me.pqpo.librarylog4a.Log4a;
import top.trumeet.common.utils.PreferencesUtils;

/**
 * @author zts
 */
public class ConfigCenter {
    private static final String TAG = ConfigCenter.class.getSimpleName();


    private static volatile ConfigCenter conf = new ConfigCenter();

    public static ConfigCenter getInstance() {
        return conf;
    }

    public static boolean reloadConf(Context ctx, boolean force) {
        if (conf.inited && !force) {
            return false;
        }

        ConfigCenter tmp = new ConfigCenter();
        try {
            tmp.inited = true;

            SharedPreferences prefs = PreferencesUtils.getPreferences(ctx);
            tmp.autoRegister = prefs.getBoolean(PreferencesUtils.AUTO_REGISTER, tmp.autoRegister);
            tmp.debugIntent = prefs.getBoolean(PreferencesUtils.KEY_DEBUG_INTENT, tmp.debugIntent);
            tmp.foregroundNotification = prefs.getBoolean(PreferencesUtils.KEY_FOREGROUND_NOTIFICATION, tmp.foregroundNotification);
            tmp.enableWakeupTarget = prefs.getBoolean(PreferencesUtils.KEY_ENABLE_WAKEUP_TARGET, tmp.enableWakeupTarget);
            tmp.enableGroupNotification = prefs.getBoolean(PreferencesUtils.KEY_ENABLE_GROUP_NOTIFICATION, tmp.enableGroupNotification);

            {
                String mode = prefs.getString(PreferencesUtils.KEY_ACCESS_MODE, "0");
                tmp.accessMode = Integer.valueOf(mode);
            }

            conf = tmp;
            return true;
        } catch (RuntimeException e) {
            Log4a.e(TAG, e);
            Toast.makeText(ctx, ctx.getString(R.string.log_push_err)  + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }


    public boolean inited = false;

    public boolean autoRegister = true;
    public boolean debugIntent = false;
    public boolean foregroundNotification = true;
    public boolean enableWakeupTarget = true;
    public boolean enableGroupNotification = true;

    public int accessMode = 0;

}
