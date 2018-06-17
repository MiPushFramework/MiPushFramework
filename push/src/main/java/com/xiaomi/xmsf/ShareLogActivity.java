package com.xiaomi.xmsf;

import android.app.Activity;
import android.os.Bundle;

import com.xiaomi.xmsf.utils.LogUtils;

import me.pqpo.librarylog4a.Log4a;

/**
 *
 * @author Trumeet
 * @date 2017/12/29
 */

public class ShareLogActivity extends Activity {
    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log4a.flush();
        LogUtils.shareFile(this);
        finish();
    }
}
