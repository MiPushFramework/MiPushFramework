package top.trumeet.mipushframework.permissions;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.xiaomi.xmsf.R;

import moe.shizuku.preference.Preference;
import moe.shizuku.preference.PreferenceCategory;
import moe.shizuku.preference.PreferenceFragment;
import moe.shizuku.preference.PreferenceScreen;
import moe.shizuku.preference.SimpleMenuPreference;
import moe.shizuku.preference.SwitchPreferenceCompat;
import top.trumeet.mipushframework.event.RecentActivityActivity;
import top.trumeet.mipushframework.register.RegisterDB;
import top.trumeet.mipushframework.register.RegisteredApplication;

/**
 * Created by Trumeet on 2017/8/27.
 * @author Trumeet
 */

public class ManagePermissionsActivity extends AppCompatActivity {
    public static final String EXTRA_PACKAGE_NAME =
            ManagePermissionsActivity.class.getName()
            + ".EXTRA_PACKAGE_NAME";

    private LoadTask mTask;

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null &&
                getIntent().hasExtra(EXTRA_PACKAGE_NAME)) {
            mTask = new LoadTask(getIntent().getStringExtra(EXTRA_PACKAGE_NAME));
            mTask.execute();
        }
        getSupportActionBar()
                .setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy () {
        if (mTask != null && !mTask.isCancelled()) {
            mTask.cancel(true);
            mTask = null;
        }
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged (Configuration configuration) {
        super.onConfigurationChanged(configuration);
    }

    private class LoadTask extends AsyncTask<Void, Void, RegisteredApplication> {
        private String pkg;

        LoadTask (String pkg) {
            this.pkg = pkg;
        }

        @Override
        protected RegisteredApplication doInBackground(Void... voids) {
            return RegisterDB.registerApplication(pkg /* Package */
                    , false /* Auto Crate */,
                    ManagePermissionsActivity.this /* Context */);
        }

        @Override
        protected void onPostExecute (RegisteredApplication application) {
            if (application != null) {
                ManagePermissionsFragment fragment = new ManagePermissionsFragment();
                fragment.setApplicationItem(application);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(android.R.id.content,
                                fragment)
                        .commitAllowingStateLoss();
            }
        }
    }

    public static class ManagePermissionsFragment extends PreferenceFragment {
        private RegisteredApplication mApplicationItem;
        private SaveTask mSaveTask;

        /**
         * Not using {@link android.os.Parcelable}, too bad
         * @param applicationItem item
         */
        public void setApplicationItem (RegisteredApplication applicationItem) {
            this.mApplicationItem = applicationItem;
        }

        @Override
        public void onCreate (Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        @Override
        public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
            MenuItem itemOk = menu.add(0, 0, 0, R.string.apply);
            Drawable iconOk = ContextCompat.getDrawable(getActivity(),
                    R.drawable.ic_check_black_24dp);
            itemOk.setIcon(iconOk);
            itemOk.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }

        @Override
        public boolean onOptionsItemSelected (MenuItem item) {
            if (item.getItemId() == 0) {
                if (mSaveTask != null && !mSaveTask.isCancelled()) {
                    return true;
                }
                mSaveTask = new SaveTask();
                mSaveTask.execute();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public void onDetach () {
            if (mSaveTask != null && !mSaveTask.isCancelled()) {
                mSaveTask.cancel(true);
                mSaveTask = null;
            }
            super.onDetach();
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            PreferenceScreen screen = getPreferenceManager()
                    .createPreferenceScreen(getActivity());

            Preference appPreference = new Preference(getActivity());
            try {
                appPreference.setIcon(getActivity().getPackageManager()
                .getApplicationIcon(mApplicationItem.getPackageName()));
                appPreference.setSummary(mApplicationItem.getPackageName());
                appPreference.setTitle(getActivity().getPackageManager()
                .getApplicationLabel(getActivity().getPackageManager()
                .getApplicationInfo(mApplicationItem.getPackageName(), 0)));
            } catch (PackageManager.NameNotFoundException e) {
                appPreference.setIcon(android.R.drawable.sym_def_app_icon);
                appPreference.setSummary(mApplicationItem.getPackageName());
                appPreference.setTitle(mApplicationItem.getPackageName());
            }
            screen.addPreference(appPreference);

            final SimpleMenuPreference preferenceRegisterMode =
                    new SimpleMenuPreference(getActivity(),
                            null, moe.shizuku.preference.simplemenu.R.attr.simpleMenuPreferenceStyle,
                            R.style.SimpleMenuPreference);
            preferenceRegisterMode.setEntries(R.array.register_types);
            preferenceRegisterMode.setEntryValues(R.array.register_entries);
            preferenceRegisterMode.setTitle(R.string.permission_register_type);
            preferenceRegisterMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    mApplicationItem.setType(Integer.parseInt(String.valueOf(newValue)));
                    updateRegisterType(mApplicationItem.getType(),
                            preferenceRegisterMode);
                    return true;
                }
            });
            updateRegisterType(mApplicationItem.getType(),
                    preferenceRegisterMode);

            Preference viewRecentActivityPreference = new Preference(getActivity());
            viewRecentActivityPreference.setTitle(R.string.recent_activity_view);
            viewRecentActivityPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getActivity(),
                            RecentActivityActivity.class)
                    .setData(Uri.parse(mApplicationItem.getPackageName())));
                    return true;
                }
            });

            screen.addPreference(preferenceRegisterMode);
            screen.addPreference(viewRecentActivityPreference);

            PreferenceCategory category = new PreferenceCategory(getActivity(), null, moe.shizuku.preference.R.attr.preferenceCategoryStyle,
                    R.style.Preference_Category_Material);
            category.setTitle(R.string.permissions);
            screen.addPreference(category);

            addItem(mApplicationItem.getAllowReceivePush(),
                    new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            mApplicationItem.setAllowReceivePush(((Boolean)newValue));
                            return true;
                        }
                    },
            getString(R.string.permission_allow_receive),
                    category);

            addItem(mApplicationItem.getAllowReceiveRegisterResult(),
                    new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            mApplicationItem.setAllowReceiveRegisterResult(((Boolean)newValue));
                            return true;
                        }
                    },
                    getString(R.string.permission_allow_receive_register_result),
                    category);

            setPreferenceScreen(screen);
        }

        private void updateRegisterType (@RegisteredApplication.Type int type,
                                         SimpleMenuPreference preference) {
            int index = 0;
            switch (type) {
                case RegisteredApplication.Type.ALLOW:
                    index = 1;
                    break;
                case RegisteredApplication.Type.ASK:
                    index = 0;
                    break;
                case RegisteredApplication.Type.DENY:
                    index = 2;
                    break;
            }
            preference.setValueIndex(index);
            preference.setSummary(getResources().getStringArray(R.array.register_types)
            [index]);
        }

        private void addItem (boolean value, Preference.OnPreferenceChangeListener listener
                , CharSequence title, PreferenceCategory parent) {
            SwitchPreferenceCompat preference = new SwitchPreferenceCompat(getActivity(),
                    null, moe.shizuku.preference.R.attr.switchPreferenceStyle,
                    R.style.Preference_SwitchPreferenceCompat);
            preference.setOnPreferenceChangeListener(listener);
            preference.setTitle(title);
            preference.setChecked(value);
            parent.addPreference(preference);
        }

        private class SaveTask extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                RegisterDB.update(mApplicationItem,
                        getActivity());
                return null;
            }

            @Override
            protected void onPostExecute (Void result) {
                getActivity().finish();
            }
        }
    }
}
