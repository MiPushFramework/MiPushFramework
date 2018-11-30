package top.trumeet.mipushframework.permissions;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.settings.widget.EntityHeaderController;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import moe.shizuku.preference.Preference;
import moe.shizuku.preference.PreferenceCategory;
import moe.shizuku.preference.PreferenceFragment;
import moe.shizuku.preference.PreferenceGroup;
import moe.shizuku.preference.PreferenceScreen;
import moe.shizuku.preference.SimpleMenuPreference;
import moe.shizuku.preference.SwitchPreferenceCompat;
import top.trumeet.common.Constants;
import top.trumeet.common.db.RegisteredApplicationDb;
import top.trumeet.common.override.UserHandleOverride;
import top.trumeet.common.register.RegisteredApplication;
import top.trumeet.common.utils.Utils;
import top.trumeet.mipush.R;
import top.trumeet.mipushframework.control.CheckPermissionsUtils;
import top.trumeet.mipushframework.event.RecentActivityActivity;
import top.trumeet.mipushframework.utils.ShellUtils;
import top.trumeet.mipushframework.widgets.InfoPreference;

import static android.os.Build.VERSION_CODES.O;
import static android.provider.Settings.EXTRA_APP_PACKAGE;
import static android.provider.Settings.EXTRA_CHANNEL_ID;
import static top.trumeet.common.Constants.FAKE_CONFIGURATION_PATH;
import static top.trumeet.common.utils.NotificationUtils.getChannelIdByPkg;

/**
 * Created by Trumeet on 2017/8/27.
 * @author Trumeet
 */

public class ManagePermissionsActivity extends AppCompatActivity {
    private static final String TAG = ManagePermissionsActivity.class.getSimpleName();

    public static final String EXTRA_PACKAGE_NAME =
            ManagePermissionsActivity.class.getName()
            + ".EXTRA_PACKAGE_NAME";

    public static final String EXTRA_IGNORE_NOT_REGISTERED =
            ManagePermissionsActivity.class.getName()
            + ".EXTRA_IGNORE_NOT_REGISTERED";

    private CompositeDisposable composite = new CompositeDisposable();

    private LoadTask mTask;
    private String mPkg;

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null &&
                getIntent().hasExtra(EXTRA_PACKAGE_NAME)) {
            mPkg = getIntent().getStringExtra(EXTRA_PACKAGE_NAME);
            checkAndStart();
        }
        getSupportActionBar()
                .setDisplayHomeAsUpEnabled(true);
    }

    private void checkAndStart () {
        composite.add(CheckPermissionsUtils.checkAndRun(result -> {
            switch (result) {
                case OK:
                    mTask = new LoadTask(mPkg);
                    mTask.execute();
                    break;
                case PERMISSION_NEEDED:
                    Toast.makeText(this, getString(top.trumeet.common.R.string.request_permission), Toast.LENGTH_LONG)
                            .show();
                    // Restart to request permissions again.
                    checkAndStart();
                    break;
                case PERMISSION_NEEDED_SHOW_SETTINGS:
                    Toast.makeText(this, getString(top.trumeet.common.R.string.request_permission), Toast.LENGTH_LONG)
                            .show();
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            .setData(uri)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    break;
                case REMOVE_DOZE_NEEDED:
                    Toast.makeText(this, getString(R.string.request_battery_whitelist), Toast.LENGTH_LONG)
                            .show();
                    checkAndStart();
                    break;
            }
        }, throwable -> {
            Log.e(TAG, "check permissions", throwable);
        }, this));
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
        // Activity request should cancel in onPause?
        if (composite != null && !composite.isDisposed()) {
            composite.dispose();
        }
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged (Configuration configuration) {
        super.onConfigurationChanged(configuration);
    }

    private class LoadTask extends AsyncTask<Void, Void, RegisteredApplication> {
        private String pkg;
        private CancellationSignal mSignal;

        LoadTask (String pkg) {
            this.pkg = pkg;
        }

        @Override
        protected RegisteredApplication doInBackground(Void... voids) {
            mSignal = new CancellationSignal();
            RegisteredApplication application = RegisteredApplicationDb.registerApplication(pkg /* Package */
                    , false /* Auto Crate */,
                    ManagePermissionsActivity.this /* Context */,
                    mSignal);
            if (application == null && getIntent().getBooleanExtra(EXTRA_IGNORE_NOT_REGISTERED, false)) {
                application = new RegisteredApplication();
                application.setPackageName(pkg);
                application.setRegisteredType(0);
            } else if (application != null) {
                // TODO: Add unregistered application detection
                application.setRegisteredType(1);
            }
            return application;
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

        @Override
        protected void onCancelled () {
            if (mSignal != null) {
                if (!mSignal.isCanceled())
                    mSignal.cancel();
                mSignal = null;
            }
        }
    }

    public static class ManagePermissionsFragment extends PreferenceFragment {
        private RegisteredApplication mApplicationItem;
        private SaveTask mSaveTask;
        private MenuItem menuOk;
        // Will be used in SaveTask, null = not changed
        // Isn't a good idea
        private Boolean changeFakeSettings = null;

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
            menuOk = menu.add(0, 0, 0, R.string.apply);
            Drawable iconOk = ContextCompat.getDrawable(getActivity(),
                    R.drawable.ic_check_black_24dp);
            DrawableCompat.setTint(iconOk, Utils.getColorAttr(getContext(), R.attr.colorAccent));
            menuOk.setIcon(iconOk);
            menuOk.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
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

        private boolean suggestEnableFake (String pkg) {
            List<String> pkgsEqual = Arrays.asList(getResources().getStringArray(R.array.fake_blacklist_equals));
            if (pkgsEqual.contains(pkg)) return false;
            List<String> pkgsContains = Arrays.asList(getResources().getStringArray(R.array.fake_blacklist_contains));
            for (String p : pkgsContains)
                if (pkg.contains(p)) return false;
            if (!Utils.isUserApplication(pkg)) return false;
            return true;
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            PreferenceScreen screen = getPreferenceManager()
                    .createPreferenceScreen(getActivity());

            Preference appPreferenceOreo = EntityHeaderController.newInstance((AppCompatActivity)getActivity(),
                    this, null)
                    .setRecyclerView(getListView())
                    .setIcon(mApplicationItem.getIcon(getContext()))
                    .setLabel(mApplicationItem.getLabel(getContext()))
                    .setSummary(mApplicationItem.getPackageName())
                    .setPackageName(mApplicationItem.getPackageName())
                    .setButtonActions(EntityHeaderController.ActionType.ACTION_APP_INFO
                            , EntityHeaderController.ActionType.ACTION_NONE)
                    .done((AppCompatActivity)getActivity(), getContext());
            screen.addPreference(appPreferenceOreo);

            boolean suggestFake = suggestEnableFake(mApplicationItem.getPackageName());

            if (mApplicationItem.getRegisteredType() == 0) {
                InfoPreference preferenceStatus = new InfoPreference(getActivity(), null, moe.shizuku.preference.R.attr.preferenceStyle,
                        R.style.Preference_Material);
                preferenceStatus.setTitle(Html.fromHtml(getString(R.string.status_app_not_registered_title)));
                preferenceStatus.setSummary(Html.fromHtml(getString(
                        suggestFake ? R.string.status_app_not_registered_detail_with_fake_suggest :
                                R.string.status_app_not_registered_detail_without_fake_suggest
                )));
                Drawable iconError = ContextCompat.getDrawable(getActivity(), R.drawable.ic_error_outline_black_24dp);
                DrawableCompat.setTint(iconError, Color.parseColor("#D50000"));
                preferenceStatus.setIcon(iconError);
                screen.addPreference(preferenceStatus);
            }

            if (Build.VERSION.SDK_INT >= O) {
                Preference manageNotificationPreference = new Preference(getActivity());
                manageNotificationPreference.setTitle(R.string.settings_manage_app_notifications);
                manageNotificationPreference.setSummary(R.string.settings_manage_app_notifications_summary);
                manageNotificationPreference.setOnPreferenceClickListener(preference -> {
                    startActivity(new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                            .putExtra(EXTRA_APP_PACKAGE, Constants.SERVICE_APP_NAME)
                            .putExtra(EXTRA_CHANNEL_ID, getChannelIdByPkg(mApplicationItem.getPackageName())));
                    return true;
                });
                if (mApplicationItem.getRegisteredType() == 0) {
                    manageNotificationPreference.setEnabled(false);
                }
                screen.addPreference(manageNotificationPreference);
            }

            final SimpleMenuPreference preferenceRegisterMode =
                    new SimpleMenuPreference(getActivity(),
                            null, moe.shizuku.preference.simplemenu.R.attr.simpleMenuPreferenceStyle,
                            R.style.SimpleMenuPreference);
            preferenceRegisterMode.setEntries(R.array.register_types);
            preferenceRegisterMode.setEntryValues(R.array.register_entries);
            preferenceRegisterMode.setTitle(R.string.permission_register_type);
            preferenceRegisterMode.setOnPreferenceChangeListener((preference, newValue) -> {
                mApplicationItem.setType(Integer.parseInt(String.valueOf(newValue)));
                updateRegisterType(mApplicationItem.getType(),
                        preferenceRegisterMode);
                return true;
            });
            if (mApplicationItem.getRegisteredType() == 0) {
                preferenceRegisterMode.setEnabled(false);
            }
            updateRegisterType(mApplicationItem.getType(),
                    preferenceRegisterMode);

            Preference viewRecentActivityPreference = new Preference(getActivity());
            viewRecentActivityPreference.setTitle(R.string.recent_activity_view);
            viewRecentActivityPreference.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(getActivity(),
                        RecentActivityActivity.class)
                        .setData(Uri.parse(mApplicationItem.getPackageName())));
                return true;
            });

            if (mApplicationItem.getRegisteredType() == 0) {
                preferenceRegisterMode.setEnabled(false);
            }
            screen.addPreference(preferenceRegisterMode);
            screen.addPreference(viewRecentActivityPreference);

            // TODO: Switch HEAVY works to background thread
            SwitchPreferenceCompat fakeSwtich = addItem(new File(String.format(Constants.FAKE_CONFIGURATION_NAME_TEMPLATE,
                    UserHandleOverride.getUserHandleForUid(mApplicationItem.getUid(getActivity())).hashCode(),
                    mApplicationItem.getPackageName())).exists(),
                    (preference, newValue) -> {
                        changeFakeSettings = (boolean) newValue;
                        return true;
                    },
                    getString(R.string.fake_enable_title),
                    getString(suggestFake ? R.string.fake_enable_detail : R.string.fake_enable_detail_not_suggested),
                    screen);

            if (new File(Constants.FAKE_CONFIGURATION_GLOBAL).exists()) {
                fakeSwtich.setSummary(R.string.fake_enable_globe);
                fakeSwtich.setEnabled(false);
                fakeSwtich.setChecked(true);
            }


            addItem(mApplicationItem.isNotificationOnRegister(),
                    (preference, newValue) -> {
                        mApplicationItem.setNotificationOnRegister(((Boolean)newValue));
                        return true;
                    },
                    getString(R.string.permission_notification_on_register),
                    screen);

            PreferenceCategory category = new PreferenceCategory(getActivity(), null, moe.shizuku.preference.R.attr.preferenceCategoryStyle,
                    R.style.Preference_Category_Material);
            category.setTitle(R.string.permissions);
            if (mApplicationItem.getRegisteredType() == 0) {
                category.setEnabled(false);
            }
            screen.addPreference(category);

            addItem(mApplicationItem.getAllowReceivePush(),
                    (preference, newValue) -> {
                        mApplicationItem.setAllowReceivePush(((Boolean)newValue));
                        return true;
                    },
            getString(R.string.permission_allow_receive),
                    category);

            addItem(mApplicationItem.isAllowReceiveCommand(),
                    (preference, newValue) -> {
                        mApplicationItem.setAllowReceiveCommand(((Boolean)newValue));
                        return true;
                    },
                    getString(R.string.permission_allow_receive_command),
                    category);

            addItem(mApplicationItem.getAllowReceiveRegisterResult(),
                    (preference, newValue) -> {
                        mApplicationItem.setAllowReceiveRegisterResult(((Boolean)newValue));
                        return true;
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

        private SwitchPreferenceCompat addItem (boolean value, Preference.OnPreferenceChangeListener listener,
                              CharSequence title, CharSequence summary, PreferenceGroup parent) {
            SwitchPreferenceCompat preference = new SwitchPreferenceCompat(getActivity(),
                    null, moe.shizuku.preference.R.attr.switchPreferenceStyle,
                    R.style.Preference_SwitchPreferenceCompat);
            preference.setOnPreferenceChangeListener(listener);
            preference.setTitle(title);
            preference.setSummary(summary);
            preference.setChecked(value);
            parent.addPreference(preference);

            return preference;
        }

        /**
         * @deprecated Use {@link #addItem(boolean, Preference.OnPreferenceChangeListener, CharSequence, CharSequence, PreferenceGroup)} instead.
         */
        @Deprecated
        private void addItem (boolean value, Preference.OnPreferenceChangeListener listener
                , CharSequence title, PreferenceGroup parent) {
            addItem(value, listener, title, null, parent);
        }

        private class SaveTask extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                if (mApplicationItem != null && mApplicationItem.getRegisteredType() != 0) {
                    RegisteredApplicationDb.update(mApplicationItem,
                            getActivity());
                }
                if (changeFakeSettings != null) {
                    String path = String.format(Constants.FAKE_CONFIGURATION_NAME_TEMPLATE,
                            UserHandleOverride.getUserHandleForUid(mApplicationItem.getUid(getActivity())).hashCode(),
                            mApplicationItem.getPackageName());
                    Log.d(TAG, "path: " + path);
                    List<String> commands = new ArrayList<>(3);
                    if (changeFakeSettings) {
                        if (new File(FAKE_CONFIGURATION_PATH).isFile()) {
                            commands.add("rm -rf " + FAKE_CONFIGURATION_PATH);
                        }
                        if (!new File(FAKE_CONFIGURATION_PATH).exists()) {
                            commands.add("mkdir -p " + FAKE_CONFIGURATION_PATH);
                        }
                    }

                    if (changeFakeSettings) {
                        if (!new File(path).exists()) commands.add("touch " + path);
                    } else {
                        commands.add("rm " + path);
                    }
                    Log.i(TAG, "Final Commands: " + commands.toString());
                    // About permissions and groups: these commands below with root WILL make the file accessible (not editable) for all apps.
                    Log.d(TAG, "Exit: " + ShellUtils.execCmd(commands, true, true).toString());
                }
                return null;
            }

            @Override
            protected void onPostExecute (Void result) {
                getActivity().finish();
            }
        }
    }
}
