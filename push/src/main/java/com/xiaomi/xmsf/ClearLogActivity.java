package com.xiaomi.xmsf;

import android.app.Activity;
import android.os.Bundle;

import com.xiaomi.xmsf.utils.LogUtils;

import me.pqpo.librarylog4a.Log4a;

public class ClearLogActivity extends Activity {
    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log4a.flush();
        LogUtils.clearLog(this);
        finish();
    }
}
