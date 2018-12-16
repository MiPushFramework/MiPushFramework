package com.xiaomi.xmsf;

import android.app.Activity;
import android.os.Bundle;

/**
 * @author zts
 */
public class EmptyActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_empty);
    }

    public static class OnePlus extends EmptyActivity {

    }

}
