package top.trumeet.mipushframework.settings;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

import com.xiaomi.xmsf.BuildConfig;
import com.xiaomi.xmsf.R;

import java.util.Locale;

/**
 * Main settings activity
 * @author Trumeet
 */
@SuppressLint("ExportedPreferenceActivity")
public class MainActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preference);

        Preference preferenceVersion = findPreference("key_version");
        preferenceVersion.setSummary(getString(R.string.preference_version,
                String.format(Locale.US, "%1$s (%2$s)", BuildConfig.VERSION_NAME,
                        BuildConfig.VERSION_CODE)));
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
