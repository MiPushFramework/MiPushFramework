package top.trumeet.mipushframework;

import android.app.Application;
import android.content.SharedPreferences;

import com.crashlytics.android.Crashlytics;
import com.crossbowffs.remotepreferences.RemotePreferenceAccessException;

import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import io.fabric.sdk.android.Fabric;
import rx_activity_result2.RxActivityResult;
import top.trumeet.common.utils.PreferencesUtils;
import top.trumeet.mipush.BuildConfig;
import top.trumeet.mipushframework.debug.IdJob;
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
            // 用于判断部分 ROM 是不是把 IdJob 给拦截了
            Crashlytics.getInstance().core.setBool("COLLECT_ENABLE", true);
            // 收集信息
            OneTimeWorkRequest request = OneTimeWorkRequest.from(IdJob.class).get(0);
            WorkManager.getInstance().beginUniqueWork("Collect",
                    ExistingWorkPolicy.KEEP, request).enqueue();
        }

        init();

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    private void init() {
        RxActivityResult.register(this);
        //debugIcon init
        try {
            SharedPreferences prefs = PreferencesUtils.getPreferences(this);
            BaseAppsBinder.debugIcon = prefs.getBoolean(PreferencesUtils.KEY_DEBUG_ICON, false);
        } catch (RemotePreferenceAccessException e) {
        }
    }

}
