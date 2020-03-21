package top.trumeet.mipushframework.debug;

import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.support.annotation.NonNull;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import androidx.work.Worker;
import top.trumeet.common.Constants;
import top.trumeet.common.push.PushController;
import top.trumeet.common.utils.rom.RomUtils;
import top.trumeet.mipush.BuildConfig;
import top.trumeet.mipushframework.MainActivity;

import static top.trumeet.mipush.BuildConfig.DEBUG;

/**
 * 搜集一些必要的匿名信息，以便改善产品和服务。
 */
public class IdJob extends Worker {
    private static final String TAG = "IdJob";
    private void collect () {
        if (DEBUG || BuildConfig.FABRIC_KEY.equals("null")) {
            Log.e(MainActivity.TAG, TAG + ": Fabric is disabled, skipping");
            return;
        }
        Log.d(MainActivity.TAG, TAG + ": collect...");
        long ms = System.currentTimeMillis();
        put("PUSH_API_PROTOCOL_LEGACY",
                PushController.isLegacySupported(getApplicationContext()));

        try {
            PackageManager pm = getApplicationContext().getPackageManager();
            PackageInfo info = pm.getPackageInfo(Constants.SERVICE_APP_NAME,
                    PackageManager.GET_DISABLED_COMPONENTS |
                            PackageManager.GET_SERVICES | PackageManager.GET_ACTIVITIES | PackageManager.GET_RECEIVERS |
                            PackageManager.GET_PROVIDERS);
            put("PUSH_INSTALLED", true);
            Set<Component> components = new HashSet<>(30);
            if (info.activities != null) {
                for (ActivityInfo activityInfo : info.activities) {
                    int enable = pm.getComponentEnabledSetting(new ComponentName(activityInfo.packageName,
                            activityInfo.name));
                    components.add(new Component(activityInfo.name,
                            enable == PackageManager.COMPONENT_ENABLED_STATE_ENABLED ||
                    enable == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT));
                }
            }
            if (info.receivers != null) {
                for (ActivityInfo activityInfo : info.receivers) {
                    int enable = pm.getComponentEnabledSetting(new ComponentName(activityInfo.packageName,
                            activityInfo.name));
                    components.add(new Component(activityInfo.name,
                            enable == PackageManager.COMPONENT_ENABLED_STATE_ENABLED ||
                                    enable == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT));
                }
            }
            if (info.providers != null) {
                for (ProviderInfo providerInfo : info.providers) {
                    int enable = pm.getComponentEnabledSetting(new ComponentName(providerInfo.packageName,
                            providerInfo.name));
                    components.add(new Component(providerInfo.name,
                            enable == PackageManager.COMPONENT_ENABLED_STATE_ENABLED ||
                                    enable == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT));
                }
            }
            if (info.receivers != null) {
                for (ServiceInfo serviceInfo : info.services) {
                    int enable = pm.getComponentEnabledSetting(new ComponentName(serviceInfo.packageName,
                            serviceInfo.name));
                    components.add(new Component(serviceInfo.name,
                            enable == PackageManager.COMPONENT_ENABLED_STATE_ENABLED ||
                                    enable == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT));
                }
            }
            put("PUSH_ENABLED_COMPONENTS", Arrays.toString(components.toArray()));
        } catch (PackageManager.NameNotFoundException e) {
            put("PUSH_INSTALLED", false);
        }
        put("PUSH_VERSION", PushController.getConnected(getApplicationContext(), null)
        .getVersionCode());
        put("ROM", RomUtils.getOs());
        ms = System.currentTimeMillis() - ms;
        Log.d(MainActivity.TAG, TAG + ": collect() took " + ms);
    }

    @NonNull
    @Override
    public Result doWork() {
        collect();
        return Result.SUCCESS;
    }

    private static class Component {
        public final String name;
        public final boolean enabled;

        public Component(String name, boolean enabled) {
            this.name = name;
            this.enabled = enabled;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Component component = (Component) o;
            return enabled == component.enabled &&
                    Objects.equals(name, component.name);
        }

        @Override
        public int hashCode() {

            return Objects.hash(name, enabled);
        }

        @Override
        public String toString() {
            return name + "(" + enabled + ")";
        }
    }

    private void put (String key, boolean value) {
        Log.d(MainActivity.TAG, TAG + ": " + key + "=" + value);
        Crashlytics.getInstance().core.setBool(key, value);
    }

    private void put (String key, String value) {
        Log.d(MainActivity.TAG, TAG + ": " + key + "=" + value);
        Crashlytics.getInstance().core.setString(key, value);
    }

    private void put (String key, int value) {
        Log.d(MainActivity.TAG, TAG + ": " + key + "=" + value);
        Crashlytics.getInstance().core.setInt(key, value);
    }
}
