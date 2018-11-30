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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_empty);
        moveTaskToBack(false);
    }

    public static class OnePlus extends EmptyActivity {

    }

}
