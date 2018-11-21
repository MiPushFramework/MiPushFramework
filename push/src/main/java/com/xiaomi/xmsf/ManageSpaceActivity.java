package com.xiaomi.xmsf;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.TwoStatePreference;
import android.widget.Toast;

import com.xiaomi.xmsf.utils.LogUtils;

import me.pqpo.librarylog4a.Log4a;
import top.trumeet.common.Constants;
import top.trumeet.common.db.EventDb;

import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED;


public class ManageSpaceActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        top.trumeet.mipush.provider.DatabaseUtils.init(this);

        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }


    public static class MyPreferenceFragment extends PreferenceFragment {


        public MyPreferenceFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.fragmented_preferences);

            Context context = getActivity();

            //Too bad in ui thread

            getPreferenceScreen().findPreference("clear_history").setOnPreferenceClickListener(preference -> {
                Toast.makeText(context, getString(R.string.settings_clear_history) + getString(R.string.start), Toast.LENGTH_SHORT).show();
                EventDb.deleteHistory(context, null);
                Toast.makeText(context, getString(R.string.settings_clear_history) + getString(R.string.end), Toast.LENGTH_SHORT).show();
                return true;
            });

            getPreferenceScreen().findPreference("clear_log").setOnPreferenceClickListener(preference -> {
                Toast.makeText(context, getString(R.string.settings_clear_log) + getString(R.string.start), Toast.LENGTH_SHORT).show();
                Log4a.flush();
                LogUtils.clearLog(context);
                Toast.makeText(context, getString(R.string.settings_clear_log) + getString(R.string.end), Toast.LENGTH_SHORT).show();
                return true;
            });


            PackageManager pm = context.getPackageManager();
            ComponentName componentName = new ComponentName(Constants.SERVICE_APP_NAME, EmptyActivity.OnePlus.class.getName());
            boolean disabled = pm.getComponentEnabledSetting(componentName) == COMPONENT_ENABLED_STATE_DISABLED;

            TwoStatePreference preferencePushIcon = (TwoStatePreference) getPreferenceScreen().findPreference("activity_push_icon");
            preferencePushIcon.setChecked(!disabled);
            preferencePushIcon.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent().setComponent(new ComponentName(Constants.SERVICE_APP_NAME,
                        Constants.KEEPLIVE_COMPONENT_NAME));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                TwoStatePreference switchPreference =  (TwoStatePreference) preference;
                intent.putExtra(EmptyActivity.ENABLE_LAUNCHER, switchPreference.isChecked());
                startActivity(intent);
                return true;
            });

        }
    }


}
