package top.trumeet.mipushframework.wizard;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.android.setupwizardlib.SetupWizardLayout;
import com.android.setupwizardlib.view.NavigationBar;
import top.trumeet.mipush.R;

/**
 * Created by Trumeet on 2017/8/24.
 * End of wizard
 */

public class FinishWizardActivity extends AppCompatActivity implements NavigationBar.NavigationBarListener {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WizardSPUtils.setShouldShowWizard(false, this);
        SetupWizardLayout layout = new SetupWizardLayout(this);
        layout.getNavigationBar()
                .setNavigationBarListener(this);
        layout.getNavigationBar().getBackButton().setVisibility(View.GONE);
        TextView textView = new TextView(this);
        textView.setText(Html.fromHtml(getString(R.string.wizard_descr_finish)));
        int padding = (int) getResources().getDimension(R.dimen.suw_glif_margin_sides);
        textView.setPadding(padding, padding, padding, padding);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        layout.addView(textView);
        layout.setHeaderText(R.string.app_name);
        setContentView(layout);
    }

    @Override
    public void onNavigateBack() {
    }

    @Override
    public void onNavigateNext() {
        WizardSPUtils.finishWizard(this);
    }

    @Override
    public void onBackPressed () {}
}
