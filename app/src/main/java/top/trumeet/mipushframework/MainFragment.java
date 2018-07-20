package top.trumeet.mipushframework;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import top.trumeet.common.push.PushController;
import top.trumeet.mipush.BuildConfig;
import top.trumeet.mipush.R;
import top.trumeet.mipushframework.control.FragmentBroadcast;
import top.trumeet.mipushframework.control.OnConnectStatusChangedListener;
import top.trumeet.mipushframework.event.EventFragment;
import top.trumeet.mipushframework.help.HelpActivity;
import top.trumeet.mipushframework.register.RegisteredApplicationFragment;
import top.trumeet.mipushframework.settings.SettingsFragment;

/**
 * @author Trumeet
 * @date 2017/12/30
 */

public class MainFragment extends Fragment implements OnConnectStatusChangedListener {
    private SwitchCompat mSwitchEnablePush;

    private FragmentBroadcast mBroadcaster;

    private static final String FRAGMENT_EVENT = "event";
    private static final String FRAGMENT_APPLICATIONS = "applications";
    private static final String FRAGMENT_SETTINGS = "settings";

    private PushController getPushController() {
        FragmentActivity fragmentActivity = getActivity();
        if (fragmentActivity == null) {
            return null;
        }
        return ((MainActivity) fragmentActivity)
                .getController();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mBroadcaster = new FragmentBroadcast();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_about) {
            ((TextView)(new AlertDialog.Builder(getContext())
                    .setView(R.layout.dialog_about)
                    .show().findViewById(R.id.text_version))).setText(getString(R.string.about_version,
                    BuildConfig.VERSION_NAME));
            return true;
        } else if (item.getItemId() == R.id.action_update) {
            startActivity(new Intent(Intent.ACTION_VIEW)
                    .setData(Uri.parse("https://github.com/Trumeet/MiPushFramework/releases")));
            Toast.makeText(getActivity(), R.string.update_toast, Toast.LENGTH_LONG)
                    .show();
        } else if (item.getItemId() == R.id.action_help) {
            Intent intent = new Intent();
            intent.setClass(this.getContext(), HelpActivity.class);
            startActivity(intent);

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.action_enable);
        item.setActionView(R.layout.switch_layout);
        mSwitchEnablePush = item.getActionView().findViewById(R.id.switchForActionBar);
        initSwitch();
    }


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_main, parent, false);

        final BottomNavigationView bottomNavigationView =
                view.findViewById(R.id.bottom_nav);
        final ViewPager viewPager = view.findViewById(R.id.viewPager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                bottomNavigationView.getMenu()
                        .getItem(0)
                        .setChecked(false);
                bottomNavigationView.getMenu()
                        .getItem(1)
                        .setChecked(false);
                bottomNavigationView.getMenu()
                        .getItem(2)
                        .setChecked(false);
                bottomNavigationView
                        .getMenu()
                        .getItem(position)
                        .setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        viewPager.setAdapter(new FragmentPagerAdapter(getChildFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        if (mBroadcaster.hasFragment(FRAGMENT_EVENT)) {
                            return mBroadcaster.getFragment(FRAGMENT_EVENT);
                        }
                        EventFragment eventFragment = new EventFragment();
                        mBroadcaster.registerFragment(FRAGMENT_EVENT, eventFragment);
                        return eventFragment;
                    case 1:
                        if (mBroadcaster.hasFragment(FRAGMENT_APPLICATIONS)) {
                            return mBroadcaster.getFragment(FRAGMENT_APPLICATIONS);
                        }
                        RegisteredApplicationFragment registeredApplicationFragment =
                                new RegisteredApplicationFragment();
                        mBroadcaster.registerFragment(FRAGMENT_APPLICATIONS, registeredApplicationFragment);
                        return registeredApplicationFragment;
                    case 2:
                        if (mBroadcaster.hasFragment(FRAGMENT_SETTINGS)) {
                            return mBroadcaster.getFragment(FRAGMENT_SETTINGS);
                        }
                        SettingsFragment settingsFragment = new SettingsFragment();
                        mBroadcaster.registerFragment(FRAGMENT_SETTINGS, settingsFragment);
                        return settingsFragment;
                    default:
                        return null;
                }
            }

            @Override
            public int getCount() {
                return 3;
            }
        });
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                viewPager.setCurrentItem(item.getOrder());
                return true;
            }
        });
        ViewCompat.setElevation(bottomNavigationView, 8f);
        viewPager.setCurrentItem(1);
        return view;
    }

    private void refreshStatus() {
        PushController controller = getPushController();
        if (controller != null && controller.isConnected()) {
            try {
                mSwitchEnablePush.setChecked(controller.isEnable(false));
                return;
            } catch (RuntimeException ignore) {
            }
        }
        mSwitchEnablePush.setEnabled(true);
    }

    private void initSwitch() {
        mSwitchEnablePush.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                getPushController().setEnable(b);
                Toast.makeText(getContext(), b ? R.string.msg_enable : R.string.msg_disable, Toast.LENGTH_SHORT).show();
            }
        });
        refreshStatus();
    }

    @Override
    public void onChange(@Status int newStatus) {
        refreshStatus();
        // Broadcast
        mBroadcaster.broadcast(newStatus);
    }

    @Override
    public void onDetach() {
        if (mBroadcaster != null) {
            mBroadcaster.unregisterAll();
        }
        super.onDetach();
    }
}
