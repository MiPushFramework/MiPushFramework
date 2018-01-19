package top.trumeet.mipushframework.wizard;

import android.app.AppOpsManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.widget.Toast;

import com.android.setupwizardlib.view.NavigationBar;
import com.xiaomi.xmsf.R;

import top.trumeet.common.Constants;
import top.trumeet.common.push.PushController;
import top.trumeet.common.utils.Utils;
import top.trumeet.mipushframework.utils.ShellUtils;
import top.trumeet.mipushframework.wizard.fake.FakeBuildActivity;

/**
 * Created by Trumeet on 2017/8/25.
 * @author Trumeet
 */

public class CheckRunInBackgroundActivity extends PushControllerWizardActivity implements NavigationBar.NavigationBarListener {
    private boolean allow;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT < 19) {
            nextPage();
            finish();
            return;
        }
        connect();
    }

    @Override
    public void onConnected(@NonNull PushController controller,
                            Bundle savedInstanceState) {
        super.onConnected(controller, savedInstanceState);
        layout.getNavigationBar()
                .setNavigationBarListener(this);
        mText.setText(Html.fromHtml(getString(R.string.wizard_descr_run_in_background, Build.VERSION.SDK_INT >= 26 ?
                        "" : (Utils.isAppOpsInstalled() ? getString(R.string.run_in_background_rikka_appops) :
                        getString(R.string.run_in_background_appops_root)))));
        layout.setHeaderText(R.string.wizard_title_run_in_background);
        setContentView(layout);

        int result = controller.checkOp(AppOpsManager.OP_RUN_IN_BACKGROUND);
        allow = (result == AppOpsManager.MODE_ALLOWED);

        if (allow) {
            nextPage();
            finish();
        }
    }

    @Override
    public void onNavigateBack() {
        onBackPressed();
    }

    @Override
    public void onNavigateNext() {
        if (!allow) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent intent = new Intent();
                String packageName = Constants.SERVICE_APP_NAME;
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
            } else if (Utils.isAppOpsInstalled()) {
                startActivity(getPackageManager()
                .getLaunchIntentForPackage("rikka.appops"));
                Toast.makeText(this, Utils.getString(R.string.rikka_appops_help_toast,
                        this), Toast.LENGTH_LONG).show();
            } else {
                if (ShellUtils.exec("appops set --user " + Utils.myUid() +
                " " + getPackageName() + " " + AppOpsManager.OP_RUN_IN_BACKGROUND +
                " " + AppOpsManager.MODE_ALLOWED))
                    nextPage();
                else
                    Toast.makeText(this, R.string.fail
                            , Toast.LENGTH_SHORT).show();
            }
        } else {
            nextPage();
        }
    }

    private void nextPage () {
        startActivity(new Intent(this,
                FakeBuildActivity.class));
    }
}
