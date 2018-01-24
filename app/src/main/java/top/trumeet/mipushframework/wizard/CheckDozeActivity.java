package top.trumeet.mipushframework.wizard;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.setupwizardlib.SetupWizardLayout;
import com.android.setupwizardlib.view.NavigationBar;
import com.xiaomi.xmsf.R;

import top.trumeet.common.Constants;
import top.trumeet.common.push.PushServiceAccessibility;

/**
 * Created by Trumeet on 2017/8/25.
 * @author Trumeet
 */

public class CheckDozeActivity extends AppCompatActivity implements NavigationBar.NavigationBarListener {
    private static final int RC_REQUEST = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (PushServiceAccessibility.isInDozeWhiteList(this)) {
            nextPage();
            finish();
            return;
        }
        SetupWizardLayout layout = new SetupWizardLayout(this);
        layout.getNavigationBar()
                .setNavigationBarListener(this);
        TextView textView = new TextView(this);
        textView.setText(Html.fromHtml(getString(R.string.wizard_descr_doze_whitelist)));
        int padding = (int) getResources().getDimension(R.dimen.suw_glif_margin_sides);
        textView.setPadding(padding, padding, padding, padding);
        textView.setMovementMethod(LinkMovementMethod.getInstance());

        Button button = new Button(this);
        button.setText(R.string.wizard_button_remove_doze);
        //button.setPadding(padding, padding, padding, padding);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = padding;
        params.leftMargin = padding;
        params.rightMargin = padding;
        params.bottomMargin = padding;
        button.setLayoutParams(params);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent()
                .setComponent(new ComponentName(Constants.SERVICE_APP_NAME,
                        Constants.REMOVE_DOZE_COMPONENT_NAME)), RC_REQUEST);
            }
        });

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        linearLayout.addView(textView);
        linearLayout.addView(button);

        layout.addView(linearLayout);
        layout.setHeaderText(R.string.wizard_title_doze_whitelist);
        setContentView(layout);
    }

    @Override
    public void onNavigateBack() {
        onBackPressed();
    }

    @SuppressLint("BatteryLife")
    @Override
    public void onNavigateNext() {
        nextPage();
    }

    private void nextPage () {
        if (getIntent().getBooleanExtra(Constants.EXTRA_FINISH_ON_NEXT,
                false)) {
            finish();
        } else {
            startActivity(new Intent(this,
                    FinishWizardActivity.class));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RC_REQUEST:
                if (PushServiceAccessibility.isInDozeWhiteList(this))
                    nextPage();
                break;
        }
    }
}
