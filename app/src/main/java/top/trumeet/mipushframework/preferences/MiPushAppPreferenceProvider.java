package top.trumeet.mipushframework.preferences;

import com.crossbowffs.remotepreferences.RemotePreferenceProvider;

import top.trumeet.common.utils.PreferencesUtils;

public class MiPushAppPreferenceProvider extends RemotePreferenceProvider {
    public MiPushAppPreferenceProvider() {
        super(PreferencesUtils.Authority, new String[]{PreferencesUtils.MainPrefs});
    }

    @Override
    protected boolean checkAccess(String prefName, String prefKey, boolean write) {
        if (write) {
            return false;
        }

        if (!"com.xiaomi.xmsf".equals(getCallingPackage())) {
            return false;
        }

        return true;
    }
}