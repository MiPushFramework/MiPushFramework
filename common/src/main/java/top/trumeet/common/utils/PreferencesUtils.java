package top.trumeet.common.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.crossbowffs.remotepreferences.RemotePreferences;

/**
 * @author zts
 */
public class PreferencesUtils {

    public static final String Authority = "top.trumeet.mipushframework.preferences";
    public static final String MainPrefs = "top.trumeet.mipush_preferences";

    public static final String KeyAccessMode = "AccessMode";
    public static final String KeyDebugIcon = "DebugIcon";
    public static final String KeyAutoRegister = "AutoRegister";
    public static final String KeyForegroundNotification = "ForegroundNotification";
    public static final String KeyEnableWakeupTarget = "EnableWakeupTarget";

    public static SharedPreferences getPreferences(Context ctx) {
        return new RemotePreferences(ctx, PreferencesUtils.Authority, PreferencesUtils.MainPrefs, true);
    }

}
