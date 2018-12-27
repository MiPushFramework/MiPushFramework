package com.xiaomi.xmsf.push.auth;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Html;
import android.view.WindowManager;

import com.elvishew.xlog.Logger;
import com.elvishew.xlog.XLog;
import com.xiaomi.xmsf.R;

import java.util.Objects;

import top.trumeet.common.cache.ApplicationNameCache;
import top.trumeet.common.db.RegisteredApplicationDb;
import top.trumeet.common.register.RegisteredApplication;

import static top.trumeet.common.Constants.EXTRA_MI_PUSH_PACKAGE;


/**
 * Created by Trumeet on 2017/8/27.
 *
 * @author Trumeet
 */

public class AuthActivity extends Activity {
    private Logger logger = XLog.tag("Auth").build();

    /**
     * Application details
     */
    public static final String EXTRA_REGISTERED_APPLICATION
            = AuthActivity.class.getName()
            + ".EXTRA_REGISTERED_APPLICATION";

    private RegisteredApplication application;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!getIntent().hasExtra(EXTRA_REGISTERED_APPLICATION)) {
            logger.e("Args not found.");
            finish();
            return;
        }

        application =  getIntent().getParcelableExtra(EXTRA_REGISTERED_APPLICATION);

        CharSequence name = ApplicationNameCache.getInstance().getAppName(
                this, application.getPackageName());
        if (name == null) {
            name = application.getPackageName();
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage(Html.fromHtml(getString(R.string.auth_message,
                        name)))
                .setPositiveButton(R.string.allow, (dialogInterface, i) -> setResultAndFinish(RegisteredApplication.Type.ALLOW))
                .setNegativeButton(R.string.deny, (dialogInterface, i) -> setResultAndFinish(RegisteredApplication.Type.DENY))
                .setNeutralButton(R.string.allow_once, (dialogInterface, i) -> setResultAndFinish(RegisteredApplication.Type.ALLOW_ONCE))
                .setCancelable(false)
                .setOnCancelListener(dialogInterface -> setResultAndFinish(RegisteredApplication.Type.DENY))
                .show();
        WindowManager.LayoutParams lp = Objects.requireNonNull(dialog.getWindow()).getAttributes();
        lp.dimAmount = 0.7f; // Dim level. 0.0 - no dim, 1.0 - completely opaque
        dialog.getWindow().setAttributes(lp);
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
    }

    /**
     * Update db and restart service.
     *
     * @param type Type
     */
    private void setResultAndFinish(@RegisteredApplication.Type int type) {
        logger.d(application.dump());

        application.setType(type);
        // DB operation in UI thread, too bad
        RegisteredApplicationDb.update(application, this);
        startService(new Intent(this, com.xiaomi.xmsf.push.service.XMPushService.class)
                .putExtra(EXTRA_MI_PUSH_PACKAGE, application.getPackageName()));
        finish();
    }
}
