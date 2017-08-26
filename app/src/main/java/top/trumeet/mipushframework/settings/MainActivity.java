package top.trumeet.mipushframework.settings;

import android.os.Bundle;
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

/**
 * Main settings activity
 * @author Trumeet
 */
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar()
                .setDisplayHomeAsUpEnabled(true);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(android.R.id.content,
                            new EventFragment())
                    .commitAllowingStateLoss();
        }
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_about) {
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
