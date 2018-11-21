package com.xiaomi.xmsf;

import android.app.Activity;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import top.trumeet.common.Constants;

import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED;

/**
 * @author zts
 */
public class EmptyActivity extends Activity {

    public static final String ENABLE_LAUNCHER = "enableLauncher";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle intentExtras = getIntent().getExtras();
        if (intentExtras != null) {
            boolean enableLauncher = intentExtras.getBoolean(ENABLE_LAUNCHER, false);
            PackageManager pm = getPackageManager();
            ComponentName componentName = new ComponentName(this, OnePlus.class);

            pm.setComponentEnabledSetting(componentName,
                    enableLauncher ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);

            String status = enableLauncher ? getString(R.string.enable) : getString(R.string.disable);

            Toast.makeText(this, status + "启动器图标", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_empty);
        moveTaskToBack(false);
    }

    public static class OnePlus extends EmptyActivity {

    }

}
