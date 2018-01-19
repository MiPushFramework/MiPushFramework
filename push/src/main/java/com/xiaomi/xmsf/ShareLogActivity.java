package com.xiaomi.xmsf;

import android.app.Activity;
import android.os.Bundle;

import com.xiaomi.xmsf.utils.LogUtils;

/**
 * Created by Trumeet on 2017/12/29.
 */

public class ShareLogActivity extends Activity {
    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.shareFile(this);
        finish();
    }
}
