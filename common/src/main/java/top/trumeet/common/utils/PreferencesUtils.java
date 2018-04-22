package top.trumeet.common.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.crossbowffs.remotepreferences.RemotePreferences;

public class PreferencesUtils {

    public static String Authority = "top.trumeet.mipushframework.preferences";
    public static String MainPrefs = "top.trumeet.mipush_preferences";

    public static String KeyAccessMode = "AccessMode";
    public static String KeyDebugIcon = "DebugIcon";

    public static SharedPreferences getPreferences(Context ctx) {
        return new RemotePreferences(ctx, PreferencesUtils.Authority, PreferencesUtils.MainPrefs, true);
    }

}
