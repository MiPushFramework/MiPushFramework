package top.trumeet.mipushframework.wizard;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.util.Log;
import android.view.View;

import com.android.setupwizardlib.view.NavigationBar;
import com.xiaomi.xmsf.R;

import top.trumeet.common.Constants;
import top.trumeet.common.push.PushController;

/**
 * Created by Trumeet on 2017/8/24.
 * Check XMPushService running status
 */

public class CheckRunningStatusActivity extends PushControllerWizardActivity implements NavigationBar.NavigationBarListener {
    private CheckRunningTask mTask;
    private boolean mResult;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        connect();
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
        if (getIntent().getBooleanExtra(Constants.EXTRA_FINISH_ON_NEXT,
                false)) {
            finish();
        } else {
            if (mResult) {
                if (getIntent().getBooleanExtra(Constants.EXTRA_FINISH_ON_NEXT,
                        false)) {
                    finish();
                } else {
                    startActivity(new Intent(this, CheckRunInBackgroundActivity.class));
                }
            } else {
                startActivity(new Intent(this, StartFailFAQActivity.class));
            }
        }
    }

    private class CheckRunningTask extends AsyncTask<Void, Void, Boolean> {
        private static final String TAG = "CheckRunning";

        @Override
        protected void onPreExecute () {
            layout.setProgressBarShown(true);
            mText.setText(R.string.wizard_descr_check_running);
            layout.getNavigationBar().setVisibility(View.GONE);
        }

        @Override
        protected void onPostExecute (Boolean running) {
            layout.setProgressBarShown(false);
            mText.setText(running ? R.string.wizard_descr_check_running_ok :
            R.string.wizard_descr_check_running_fail);
            layout.getNavigationBar().setVisibility(View.VISIBLE);
            mResult = running;
        }

        @Override
        protected Boolean doInBackground (Void... params) {
            if (!getController().isEnable(true)) {
                Log.w(TAG, "Service not enable, start it now!");
                getController().setEnable(true);
                SystemClock.sleep(2500);
            }
            // Because some tools will kill service later after it starts.
            // So we have to check it too much times.
            for (int i = 0; i < Constants.CHECK_RUNNING_TIMES; i ++) {
                //Log.d(TAG, "Loop -> " + i);
                if (!getController().isEnable(false)) {
                    return false;
                }
                SystemClock.sleep(5000);
            }
            return true;
        }
    }

    @Override
    public void onConnected (@NonNull PushController controller,
                             @Nullable Bundle savedInstanceState) {
        //super.onConnected(controller, savedInstanceState);
        mText.setText(Html.fromHtml(getString(R.string.wizard_descr_check_running)));
        layout.setHeaderText(R.string.wizard_title_check_running);
        layout.getNavigationBar()
                .setNavigationBarListener(this);
        mTask = new CheckRunningTask();
        mTask.execute();
    }
}
