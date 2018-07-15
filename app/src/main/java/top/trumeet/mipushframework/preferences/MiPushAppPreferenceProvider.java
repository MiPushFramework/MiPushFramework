package top.trumeet.mipushframework.preferences;

import com.crossbowffs.remotepreferences.RemotePreferenceProvider;

import top.trumeet.common.utils.PreferencesUtils;

public class MiPushAppPreferenceProvider extends RemotePreferenceProvider {
    public MiPushAppPreferenceProvider() {
        super(PreferencesUtils.AUTHORITY, new String[]{PreferencesUtils.MAIN_PREFS});
    }

    @Override
    protected boolean checkAccess(String prefName, String prefKey, boolean write) {
        if (write) {
            return false;
        }
        
        return true;
    }
}