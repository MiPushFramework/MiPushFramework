package com.xiaomi.xmsf;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;

import top.trumeet.common.push.PushServiceAccessibility;

/**
 * Created by Trumeet on 2018/1/24.
 */

public class RemoveDozeActivity extends Activity {
    private static final int RC_REQUEST = 0;

    @Override
    @SuppressLint("BatteryLife")
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M &&
                PushServiceAccessibility.isInDozeWhiteList(this)) {
            finish();
            return;
        }
        Intent intent = new Intent();
        String packageName = getPackageName();
        intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        intent.setData(Uri.parse("package:" + packageName));
        startActivityForResult(intent, RC_REQUEST);
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
