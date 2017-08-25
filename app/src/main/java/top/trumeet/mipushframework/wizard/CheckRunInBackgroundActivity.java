package top.trumeet.mipushframework.wizard;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import com.android.setupwizardlib.SetupWizardLayout;
import com.android.setupwizardlib.view.NavigationBar;
import com.xiaomi.xmsf.R;

import top.trumeet.mipushframework.push.PushServiceAccessibility;
import top.trumeet.mipushframework.wizard.fake.FakeBuildActivity;

/**
 * Created by Trumeet on 2017/8/25.
 * @author Trumeet
 */

public class CheckRunInBackgroundActivity extends AppCompatActivity implements NavigationBar.NavigationBarListener {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (PushServiceAccessibility.checkAllowRunInBackground(this)) {
            nextPage();
            finish();
            return;
        }
        SetupWizardLayout layout = new SetupWizardLayout(this);
        layout.getNavigationBar()
                .setNavigationBarListener(this);
        TextView textView = new TextView(this);
        textView.setText(Html.fromHtml(getString(R.string.wizard_descr_run_in_background)));
        int padding = (int) getResources().getDimension(R.dimen.suw_glif_margin_sides);
        textView.setPadding(padding, padding, padding, padding);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        layout.addView(textView);
        layout.setHeaderText(R.string.wizard_title_run_in_background);
        setContentView(layout);
    }

    @Override
    public void onNavigateBack() {
        onBackPressed();
    }

    @Override
    public void onNavigateNext() {
        if (!PushServiceAccessibility.checkAllowRunInBackground(this) &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent intent = new Intent();
            String packageName = getPackageName();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + packageName));
            startActivity(intent);
        } else {
            nextPage();
        }
    }

    private void nextPage () {
        startActivity(new Intent(this,
                FakeBuildActivity.class));
    }
}
