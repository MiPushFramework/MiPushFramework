package com.xiaomi.xmsf;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.xiaomi.xmsf.utils.LogUtils;



/**
 *
 * @author Trumeet
 * @date 2017/12/29
 */

public class ShareLogActivity extends Activity {
    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = LogUtils.getShareIntent(this);
        if (intent == null) {
            Toast.makeText(this, R.string.log_none
                    , Toast.LENGTH_SHORT).show();
        } else {
            if (getPackageManager().resolveActivity(intent, 0) == null) {
                Toast.makeText(this, R.string.activity_intent_not_found
                        , Toast.LENGTH_SHORT).show();
            } else {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(Intent.createChooser(intent, getString(R.string.log_share_title)));
            }
        }
        finish();
    }
}
