package top.trumeet.mipushframework.settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.xiaomi.xmsf.R;

import top.trumeet.mipushframework.event.EventFragment;
import top.trumeet.mipushframework.push.PushController;
import top.trumeet.mipushframework.register.RegisteredApplicationFragment;
import top.trumeet.mipushframework.update.UpdateActivity;

/**
 * Main settings activity
 * @author Trumeet
 */
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final BottomNavigationView bottomNavigationView =
                findViewById(R.id.bottom_nav);
        final ViewPager viewPager = findViewById(R.id.viewPager);
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
        viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0 :
                        return new EventFragment();
                    case 1 :
                        return new RegisteredApplicationFragment();
                    case 2 :
                        return new SettingsFragment();
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
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
        if (item.getItemId() == R.id.action_about) {
            TextView textView = new TextView(this);
            int padding = (int) getResources()
                    .getDimension(android.support.v7.appcompat.R.dimen.abc_dialog_padding_material);
            textView.setPadding(padding, padding, padding, padding);
            textView.setMovementMethod(LinkMovementMethod.getInstance());
            textView.setText(Html.fromHtml(getString(R.string.about_descr)));
            new AlertDialog.Builder(this)
                    .setTitle(R.string.action_about)
                    .setView(textView)
                    .show();
            return true;
        } else if (item.getItemId() == R.id.action_update) {
            startActivity(new Intent(this, UpdateActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.action_enable);
        item.setActionView(R.layout.switch_layout);
        SwitchCompat mSwitchEnablePush = item.getActionView().findViewById(R.id.switchForActionBar);
        mSwitchEnablePush.setChecked(PushController.isAllEnable(this));
        mSwitchEnablePush.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                PushController.setAllEnable(b, MainActivity.this);
                Toast.makeText(MainActivity.this,
                        b ? R.string.msg_enable : R.string.msg_disable
                        , Toast.LENGTH_SHORT).show();
            }
        });
        return true;
    }
}
