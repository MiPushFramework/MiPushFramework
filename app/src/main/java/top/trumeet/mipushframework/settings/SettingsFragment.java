package top.trumeet.mipushframework.settings;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import moe.shizuku.preference.Preference;
import moe.shizuku.preference.PreferenceFragment;
import top.trumeet.common.Constants;
import top.trumeet.common.push.PushServiceAccessibility;
import top.trumeet.mipush.R;
import top.trumeet.mipushframework.MainActivity;

/**
 * Created by Trumeet on 2017/8/27.
 * Main settings
 * @see MainActivity
 * @author Trumeet
 */

public class SettingsFragment extends PreferenceFragment {
    private static final String TAG = SettingsFragment.class.getSimpleName();

    private Preference mDozePreference;
    private Preference mCheckServicePreference;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings);
        mDozePreference = getPreferenceScreen()
                .findPreference("key_remove_doze");
        mCheckServicePreference = getPreferenceScreen()
                .findPreference("key_check_service");

       setPreferenceOnclick("key_get_log", new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent()
                .setComponent(new ComponentName(Constants.SERVICE_APP_NAME,
                        Constants.SHARE_LOG_COMPONENT_NAME)));
                return true;
            }
        });

       setPreferenceOnclick("key_clear_log", new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent()
                .setComponent(new ComponentName(Constants.SERVICE_APP_NAME,
                        Constants.CLEAR_LOG_COMPONENT_NAME)));
                return true;
            }
        });




    }

    private void setPreferenceOnclick(String key, Preference.OnPreferenceClickListener onPreferenceClickListener) {
          getPreferenceScreen().findPreference(key).setOnPreferenceClickListener(onPreferenceClickListener);

    }

    @Override
    public void onStart () {
        super.onStart();
        long time = System.currentTimeMillis();
        mDozePreference.setVisible(!PushServiceAccessibility.isInDozeWhiteList(getActivity()));
        Log.d(TAG, "rebuild UI took: " + String.valueOf(System.currentTimeMillis() -
                time));
    }
}
