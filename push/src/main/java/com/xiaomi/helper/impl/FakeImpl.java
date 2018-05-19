package com.xiaomi.helper.impl;

import android.content.Context;

import com.xiaomi.helper.ITopActivity;

/**
 * Created by zts1993 on 2018/2/18.
 */

public class FakeImpl implements ITopActivity {
    static final String TAG = "FakeImpl";

    @Override
    public boolean isEnabled(Context context) {
        return true;
    }

    @Override
    public void guideToEnable(Context context) {
    }

    @Override
    public boolean isAppForeground(Context context, String packageName) {
        return false;
    }

}
