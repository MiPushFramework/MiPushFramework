package top.trumeet.mipushframework;

import android.app.Application;
import android.content.SharedPreferences;

import com.crashlytics.android.Crashlytics;
import com.crossbowffs.remotepreferences.RemotePreferenceAccessException;

import io.fabric.sdk.android.Fabric;
import top.trumeet.common.utils.PreferencesUtils;
import top.trumeet.mipush.BuildConfig;
import top.trumeet.mipushframework.utils.BaseAppsBinder;

/**
 * Created by Trumeet on 2017/12/23.
 */

public class MiPushFramework extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (!BuildConfig.DEBUG) {
            final Fabric fabric = new Fabric.Builder(this)
                    .kits(new Crashlytics())
                    .build();
            Fabric.with(fabric);
        }

        init();

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    private void init() {
        //debugIcon init
        try {
            SharedPreferences prefs = PreferencesUtils.getPreferences(this);
            BaseAppsBinder.debugIcon = prefs.getBoolean(PreferencesUtils.KeyDebugIcon, false);
        } catch (RemotePreferenceAccessException e) {
        }
    }

}
