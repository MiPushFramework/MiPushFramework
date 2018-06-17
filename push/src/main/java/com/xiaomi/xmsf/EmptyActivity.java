package com.xiaomi.xmsf;

import android.os.Bundle;
import android.app.Activity;

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

}
