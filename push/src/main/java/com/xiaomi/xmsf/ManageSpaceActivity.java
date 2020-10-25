package com.xiaomi.xmsf;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.catchingnow.icebox.sdk_client.IceBox;
import com.xiaomi.xmsf.push.notification.NotificationController;
import com.xiaomi.xmsf.utils.LogUtils;

import java.util.Date;

import top.trumeet.common.Constants;
import top.trumeet.common.db.EventDb;
import top.trumeet.common.utils.Utils;


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

            //TODO: Three messages seem to be too much, and need separate strings for toast.
            getPreferenceScreen().findPreference("clear_history").setOnPreferenceClickListener(preference -> {
                Toast.makeText(context, getString(R.string.settings_clear_history) + getString(R.string.start), Toast.LENGTH_SHORT).show();
                EventDb.deleteHistory(context, null);
                Toast.makeText(context, getString(R.string.settings_clear_history) + getString(R.string.end), Toast.LENGTH_SHORT).show();
                return true;
            });

            getPreferenceScreen().findPreference("clear_log").setOnPreferenceClickListener(preference -> {
                Toast.makeText(context, getString(R.string.settings_clear_log) + getString(R.string.start), Toast.LENGTH_SHORT).show();
                LogUtils.clearLog(context);
                Toast.makeText(context, getString(R.string.settings_clear_log) + getString(R.string.end), Toast.LENGTH_SHORT).show();
                return true;
            });


            getPreferenceScreen().findPreference("mock_notification").setOnPreferenceClickListener(preference -> {
                String packageName = Constants.MANAGER_APP_NAME;
                Date date = new Date();
                String title = context.getString(R.string.debug_test_title);
                String description = context.getString(R.string.debug_test_content) + date.toString();
                NotificationController.test(context, packageName, title, description);
                return true;
            });

            Preference iceboxSupported = getPreferenceScreen().findPreference("IceboxSupported");
            if (!Utils.isAppInstalled(IceBox.PACKAGE_NAME)) {
                iceboxSupported.setEnabled(false);
                iceboxSupported.setTitle(R.string.settings_icebox_not_installed);
            } else {
                iceboxSupported.setOnPreferenceChangeListener((preference, newValue) -> {
                    Boolean value = (Boolean) newValue;
                    if (value) {
                        if (ContextCompat.checkSelfPermission(getActivity(), IceBox.SDK_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{IceBox.SDK_PERMISSION}, 0x233);
                        } else {
                            Toast.makeText(context, getString(R.string.icebox_permission_granted), Toast.LENGTH_SHORT).show();
                        }
                    }
                    return true;
                });

            }


        }
    }


}
