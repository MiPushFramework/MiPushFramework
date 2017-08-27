package top.trumeet.mipushframework.wizard.fake;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.Html;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.setupwizardlib.SetupWizardLayout;
import com.android.setupwizardlib.view.NavigationBar;
import com.xiaomi.xmsf.R;

import top.trumeet.mipushframework.Constants;
import top.trumeet.mipushframework.wizard.FinishWizardActivity;

/**
 * Created by Trumeet on 2017/8/25.
 * @author Trumeet
 */

public class FakeBuildActivity extends AppCompatActivity implements NavigationBar.NavigationBarListener {
    private SwitchCompat mSwitch;
    private LinearLayout mLayout;
    private SetupWizardLayout mWizard;

    private CheckTask mCheckTask;
    private ModifyTask mModifyTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWizard = new SetupWizardLayout(this);
        mWizard.getNavigationBar()
                .setNavigationBarListener(this);
        mWizard.setHeaderText(R.string.wizard_title_fake_build);
        setContentView(mWizard);
    }

    @Override
    public void onNavigateBack() {
        onBackPressed();
    }

    @Override
    public void onNavigateNext() {
        if (mModifyTask != null && !mModifyTask.isCancelled()) {
            mModifyTask.cancel(true);
            mModifyTask = null;
        }
        if (mSwitch != null)  {
            if (mSwitch.isChecked()) {
                mModifyTask = new ModifyTask();
                mModifyTask.execute();
            } else {
                nextPage();
            }
        }
    }

    private void initContent () {
        if (mLayout != null) {
            mLayout.setVisibility(View.VISIBLE);
            return;
        }
        TextView textView = new TextView(this);
        textView.setText(Html.fromHtml(getString(R.string.wizard_descr_fake_build)));
        int padding = (int) getResources().getDimension(R.dimen.suw_glif_margin_sides);

        mLayout = new LinearLayout(this);
        mLayout.setOrientation(LinearLayout.VERTICAL);

        mSwitch = new SwitchCompat(this);
        mSwitch.setText(R.string.enable);

        mLayout.addView(textView);
        mLayout.addView(mSwitch);

        mLayout.setPadding(padding, padding, padding, padding);
        mWizard.addView(mLayout);
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
    public void onStart () {
        super.onStart();
        if (mModifyTask != null && !mModifyTask.isCancelled()) {
            mModifyTask.cancel(true);
            mModifyTask = null;
        }
        if (mCheckTask != null && !mCheckTask.isCancelled()) {
            mCheckTask.cancel(true);
            mCheckTask = null;
        }
        mCheckTask = new CheckTask();
        mCheckTask.execute();
    }

    private abstract class BaseAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
        @Override
        protected void onPreExecute () {
            mWizard.setHeaderText(R.string.loading);
            mWizard.getNavigationBar().setVisibility(View.GONE);
            mWizard.setProgressBarShown(true);
            if (mLayout != null)
                mLayout.setVisibility(View.GONE);
        }

        @Override
        @CallSuper
        protected void onPostExecute (Result result) {
            mWizard.getNavigationBar().setVisibility(View.VISIBLE);
            mWizard.setProgressBarShown(false);
        }
    }

    private class CheckTask extends BaseAsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            return FakeBuildUtils.isMiuiBuild();
        }

        @Override
        protected void onPostExecute (Boolean result) {
            super.onPostExecute(result);
            if (result) {
                if (getIntent().getBooleanExtra(Constants.EXTRA_FINISH_ON_NEXT,
                        false)) {
                    Toast.makeText(FakeBuildActivity.this, R.string.toast_fake_build_already_enable
                            , Toast.LENGTH_SHORT).show();
                }
                nextPage();
            } else {
                mWizard.setHeaderText(R.string.wizard_title_fake_build);
                initContent();
            }
        }
    }

    private class ModifyTask extends BaseAsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            return FakeBuildUtils.insertMiui();
        }

        @Override
        protected void onPostExecute (Boolean result) {
            super.onPostExecute(result);
            if (result) nextPage();
            else {
                mWizard.setHeaderText(R.string.wizard_title_fake_build);
                initContent();
            }
            Toast.makeText(FakeBuildActivity.this,
                    result ? R.string.success : R.string.fail,
                    Toast.LENGTH_SHORT).show();
        }
    }
}
