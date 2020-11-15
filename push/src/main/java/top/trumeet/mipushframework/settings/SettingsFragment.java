package top.trumeet.mipushframework.settings;

import android.content.ComponentName;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import moe.shizuku.preference.Preference;
import moe.shizuku.preference.PreferenceFragment;
import moe.shizuku.preference.PreferenceGroup;
import moe.shizuku.preference.SwitchPreferenceCompat;
import top.trumeet.common.Constants;
import top.trumeet.common.utils.rom.RomUtils;

import com.xiaomi.xmsf.R;

import top.trumeet.mipushframework.MainActivity;
import top.trumeet.mipushframework.utils.ShellUtils;

import static top.trumeet.common.Constants.FAKE_CONFIGURATION_PATH;
import static top.trumeet.common.utils.rom.RomUtils.ROM_H2OS;
import static top.trumeet.common.utils.rom.RomUtils.ROM_MIUI;
import static top.trumeet.common.utils.rom.RomUtils.ROM_UNKNOWN;

/**
 * Created by Trumeet on 2017/8/27.
 * Main settings
 *
 * @author Trumeet
 * @see MainActivity
 */

public class SettingsFragment extends PreferenceFragment {

    private static final String TAG = SettingsFragment.class.getSimpleName();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings);
        setPreferenceOnclick("key_get_log", preference -> {
            startActivity(new Intent()
                    .setComponent(new ComponentName(Constants.SERVICE_APP_NAME,
                            Constants.SHARE_LOG_COMPONENT_NAME)));
            return true;
        });


        String globalFake = Constants.FAKE_CONFIGURATION_GLOBAL;
        addItem(new File(globalFake).exists(), (preference, newValue) -> {
                    boolean enabled = (boolean) newValue;

                    List<String> commands = new ArrayList<>(3);
                    if (!new File(FAKE_CONFIGURATION_PATH).exists()) {
                        commands.add("mkdir -p " + FAKE_CONFIGURATION_PATH);
                    }

                    if (new File(FAKE_CONFIGURATION_PATH).isFile()) {
                        commands.add("rm -rf " + FAKE_CONFIGURATION_PATH);
                    }


                    if (enabled) {
                        if (!new File(globalFake).exists()) commands.add("touch " + globalFake);
                    } else {
                        commands.add("rm " + globalFake);
                    }

                    Log.i(TAG, "Final Commands: " + commands.toString());
                    // About permissions and groups: these commands below with root WILL make the file accessible (not editable) for all apps.
                    Log.d(TAG, "Exit: " + ShellUtils.execCmd(commands, true, true).toString());
                    return true;
                },
                getString(R.string.fake_global_enable_title),
                getString(R.string.fake_enable_detail),
                getPreferenceScreen().findPreference("activity_push_advance_setting").getParent()
        );

    }

    private void addItem(boolean value, Preference.OnPreferenceChangeListener listener,
                         CharSequence title, CharSequence summary, PreferenceGroup parent) {
        SwitchPreferenceCompat preference = new SwitchPreferenceCompat(getContext(),
                null, moe.shizuku.preference.R.attr.switchPreferenceStyle,
                R.style.Preference_SwitchPreferenceCompat);
        preference.setOnPreferenceChangeListener(listener);
        preference.setTitle(title);
        preference.setSummary(summary);
        preference.setChecked(value);
        parent.addPreference(preference);
    }

    private void setPreferenceOnclick(String key, Preference.OnPreferenceClickListener onPreferenceClickListener) {
        getPreferenceScreen().findPreference(key).setOnPreferenceClickListener(onPreferenceClickListener);

    }

    @Override
    public void onStart() {
        super.onStart();
        long time = System.currentTimeMillis();
        Log.d(TAG, "rebuild UI took: " + (System.currentTimeMillis() -
                time));
    }

}
