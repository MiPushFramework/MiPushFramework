package top.trumeet.common.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.support.annotation.NonNull;

import com.crossbowffs.remotepreferences.RemotePreferences;

/**
 * @author zts
 */
public class PreferencesUtils {

    public static final String AUTHORITY = "top.trumeet.mipushframework.preferences";
    public static final String MAIN_PREFS = "top.trumeet.mipush_preferences";

    public static final String KEY_ACCESS_MODE = "AccessMode";
    public static final String KEY_DEBUG_ICON = "DebugIcon";
    public static final String AUTO_REGISTER = "AutoRegister";
    public static final String KEY_DEBUG_INTENT = "DebugIntent";
    public static final String KEY_FOREGROUND_NOTIFICATION = "ForegroundNotification";
    public static final String KEY_ENABLE_WAKEUP_TARGET = "EnableWakeupTarget";
    public static final String KEY_ENABLE_GROUP_NOTIFICATION = "EnableGroupNotification";
    private static SharedPreferences mPreference;


    public static SharedPreferences getPreferences(Context ctx) {
        if (mPreference == null) {
            mPreference = new RemotePreferences(ctx, PreferencesUtils.AUTHORITY, PreferencesUtils.MAIN_PREFS, true);
        }
        return mPreference;
    }

    public static SharedPreferences.OnSharedPreferenceChangeListener subscribe (@NonNull RemotePreferences preferences, @NonNull ContentObserver observer) {
        //getContentResolver().registerContentObserver(Uri.parse("content://" + AUTHORITY)
        //        , false, mSettingsObserver);
        SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences, key) -> {
            observer.onChange(false);
        };
        preferences.registerOnSharedPreferenceChangeListener(listener);
        return listener;
    }

    public static void unsubscribe (@NonNull RemotePreferences preferences, @NonNull SharedPreferences.OnSharedPreferenceChangeListener listener) {
        preferences.unregisterOnSharedPreferenceChangeListener(listener);
    }
}
