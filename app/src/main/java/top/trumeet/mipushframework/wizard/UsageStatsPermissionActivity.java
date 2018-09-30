package top.trumeet.mipushframework.wizard;

import android.app.AppOpsManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;

import com.android.setupwizardlib.view.NavigationBar;

import top.trumeet.common.override.AppOpsManagerOverride;
import top.trumeet.common.push.PushController;
import top.trumeet.mipush.R;

/**
 * Created by Trumeet on 2017/8/25.
 * @author Trumeet
 */

public class UsageStatsPermissionActivity extends PushControllerWizardActivity implements NavigationBar.NavigationBarListener {
    private boolean allow;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            nextPage();
            finish();
            return;
        }
        connect();
    }

    @Override
    public void onResume () {
        super.onResume();
        PushController controller = getController();
        if (controller != null && controller.isConnected() && !isConnecting()) {
            check(controller);
            if (allow) {
                nextPage();
                finish();
            }
        }
    }

    private void check (PushController controller) {
        int result = controller.checkOp(AppOpsManagerOverride.OP_GET_USAGE_STATS);
        allow = (result == AppOpsManager.MODE_ALLOWED);
    }

    @Override
    public void onConnected(@NonNull PushController controller,
                            Bundle savedInstanceState) {
        super.onConnected(controller, savedInstanceState);
        check(controller);

        if (allow) {
            nextPage();
            finish();
            return;
        }
        layout.getNavigationBar()
                .setNavigationBarListener(this);
        mText.setText(Html.fromHtml(getString(R.string.wizard_title_stats_permission_text)));
        layout.setHeaderText(Html.fromHtml(getString(R.string.wizard_title_stats_permission)));
        setContentView(layout);
    }

    @Override
    public void onNavigateBack() {
        onBackPressed();
    }

    @Override
    public void onNavigateNext() {
        if (!allow) {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        } else {
            nextPage();
        }
    }

    private void nextPage () {
        startActivity(new Intent(this,
                FinishWizardActivity.class));
    }

}
