package top.trumeet.mipushframework.wizard;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.android.setupwizardlib.SetupWizardLayout;
import com.android.setupwizardlib.view.NavigationBar;
import com.xiaomi.xmsf.R;

import top.trumeet.mipushframework.MainActivity;

/**
 * Created by Trumeet on 2017/8/24.
 * Show FAQ when service start fail
 * @see CheckRunningStatusActivity
 */

public class StartFailFAQActivity extends AppCompatActivity implements NavigationBar.NavigationBarListener {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!WizardSPUtils.shouldShowWizard(this)) {
            startActivity (new Intent(this,
                    MainActivity.class));
            finish();
            return;
        }
        SetupWizardLayout layout = new SetupWizardLayout(this);
        layout.getNavigationBar()
                .setNavigationBarListener(this);
        TextView textView = new TextView(this);
        textView.setText(Html.fromHtml(getString(R.string.service_not_running_faq)));
        int padding = (int) getResources().getDimension(R.dimen.suw_glif_margin_sides);
        textView.setPadding(padding, padding, padding, padding);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        layout.addView(textView);
        layout.setHeaderText(R.string.wizard_title_start_fail);
        layout.getNavigationBar().getNextButton().setVisibility(View.GONE);
        setContentView(layout);
    }

    @Override
    public void onNavigateBack() {
        onBackPressed();
    }

    @Override
    public void onNavigateNext() {
    }
}
