package com.xiaomi.xmsf.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.xiaomi.xmsf.BuildConfig;
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
            tmp.autoRegister = prefs.getBoolean(PreferencesUtils.KeyAutoRegister, tmp.autoRegister);
            tmp.foregroundNotification = prefs.getBoolean(PreferencesUtils.KeyForegroundNotification, tmp.foregroundNotification);
            tmp.enableWakeupTarget = prefs.getBoolean(PreferencesUtils.KeyEnableWakeupTarget, tmp.enableWakeupTarget);

            {
                String mode = prefs.getString(PreferencesUtils.KeyAccessMode, "0");
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


    private boolean inited = false;

    public boolean autoRegister = true;
    public boolean foregroundNotification = true;
    public boolean enableWakeupTarget = true;

    public int accessMode = 0;

}
