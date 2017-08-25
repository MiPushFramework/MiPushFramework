package top.trumeet.mipushframework.wizard;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.setupwizardlib.SetupWizardLayout;
import com.android.setupwizardlib.view.NavigationBar;
import com.xiaomi.xmsf.R;

import top.trumeet.mipushframework.Constants;
import top.trumeet.mipushframework.push.PushController;
import top.trumeet.mipushframework.wizard.support.CheckAppSupportActivity;

/**
 * Created by Trumeet on 2017/8/24.
 * Check XMPushService running status
 */

public class CheckRunningStatusActivity extends AppCompatActivity implements NavigationBar.NavigationBarListener {
    private TextView mTvStatus;
    private SetupWizardLayout mLayout;

    private CheckRunningTask mTask;
    private boolean mResult;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLayout = new SetupWizardLayout(this);
        mLayout.getNavigationBar()
                .setNavigationBarListener(this);
        mTvStatus = new TextView(this);
        mTvStatus.setText(Html.fromHtml(getString(R.string.wizard_descr_check_running)));
        int padding = (int) getResources().getDimension(R.dimen.suw_glif_margin_sides);
        mTvStatus.setPadding(padding, padding, padding, padding);
        mLayout.addView(mTvStatus);
        mLayout.setHeaderText(R.string.wizard_title_check_running);
        setContentView(mLayout);

        mTask = new CheckRunningTask();
        mTask.execute();
    }

    @Override
    public void onDestroy () {
        if (mTask != null && !mTask.isCancelled())
            mTask.cancel(true);
        super.onDestroy();
    }

    @Override
    public void onNavigateBack() {
        onBackPressed();
    }

    @Override
    public void onNavigateNext() {
        startActivity(new Intent(this,
                mResult ? CheckAppSupportActivity.class :
                        StartFailFAQActivity.class));
    }

    private class CheckRunningTask extends AsyncTask<Void, Void, Boolean> {
        private static final String TAG = "CheckRunning";

        @Override
        protected void onPreExecute () {
            mLayout.setProgressBarShown(true);
            mTvStatus.setText(R.string.wizard_descr_check_running);
            mLayout.getNavigationBar().setVisibility(View.GONE);
        }

        @Override
        protected void onPostExecute (Boolean running) {
            mLayout.setProgressBarShown(false);
            mTvStatus.setText(running ? R.string.wizard_descr_check_running_ok :
            R.string.wizard_descr_check_running_fail);
            mLayout.getNavigationBar().setVisibility(View.VISIBLE);
            mResult = running;
        }

        @Override
        protected Boolean doInBackground (Void... params) {
            if (!PushController.isAllEnable(CheckRunningStatusActivity.this)) {
                Log.w(TAG, "Service not enable, start it now!");
                PushController.setServiceEnable(true, CheckRunningStatusActivity.this);
                SystemClock.sleep(1000);
            }
            // Because some tools will kill service later after it starts.
            // So we have to check it too much times.
            for (int i = 0; i < Constants.CHECK_RUNNING_TIMES; i ++) {
                Log.d(TAG, "Loop -> " + i);
                if (!PushController.isServiceRunning(CheckRunningStatusActivity.this)) {
                    return false;
                }
                SystemClock.sleep(1000);
            }
            return true;
        }
    }
}
