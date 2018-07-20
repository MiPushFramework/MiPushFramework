package com.xiaomi.xmsf;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import top.trumeet.common.push.PushServiceAccessibility;

/**
 * @author Trumeet
 * @date 2018/1/24
 */

public class RemoveDozeActivity extends Activity {
    private static final int RC_REQUEST = 0;
    private static final String TAG = "RemoveDozeActivity";

    @Override
    @SuppressLint("BatteryLife")
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                PushServiceAccessibility.isInDozeWhiteList(this)) {
            finish();
            return;
        }
        Intent intent = new Intent();
        String packageName = getPackageName();
        intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        intent.setData(Uri.parse("package:" + packageName));
        try {
            startActivityForResult(intent, RC_REQUEST);
        } catch (android.content.ActivityNotFoundException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
            Toast.makeText(this, this.getString(R.string.common_err,
                    e.getMessage()), Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RC_REQUEST:
                finish();
                break;
        }
    }
}
