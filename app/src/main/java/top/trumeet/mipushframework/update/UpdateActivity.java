package top.trumeet.mipushframework.update;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.android.setupwizardlib.GlifLayout;
import com.xiaomi.xmsf.BuildConfig;
import com.xiaomi.xmsf.R;
import com.zzhoujay.richtext.RichText;

/**
 * Created by Trumeet on 2017/8/30.
 * @author Trumeet
 */

public class UpdateActivity extends AppCompatActivity {
    private GlifLayout mLayout;
    private TextView mBodyText;
    private ActionView mRetryAction;
    private ActionView mDownloadAction;

    private CheckUpdateTask mTask;

    private UpdateResult mLatestResult;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        mLayout = findViewById(R.id.layout);
        mBodyText = findViewById(android.R.id.text1);
        mDownloadAction = findViewById(R.id.action_download);
        mRetryAction = findViewById(R.id.action_again);
        mRetryAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTask != null && !mTask.isCancelled()) {
                    mTask.cancel(true);
                    mTask = null;
                }

                mTask = new CheckUpdateTask(true, mListener);
                mTask.execute();
            }
        });
        mDownloadAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mLatestResult == null)
                    return;
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse(mLatestResult.getHtmlUrl())));
                } catch (Exception ignore) {}
            }
        });
        mTask = new CheckUpdateTask(true, mListener);
        mTask.execute();
    }

    @Override
    public void onDestroy () {
        if (mTask != null && !mTask.isCancelled()) {
            mTask.cancel(true);
            mTask = null;
        }

        super.onDestroy();
    }

    private CheckUpdateTask.CheckListener mListener = new CheckUpdateTask.CheckListener() {
        @Override
        public void done(final UpdateResult result) {
            if (result.getName().equals(BuildConfig.VERSION_NAME) ||
                    result.getName().equals(String.valueOf(BuildConfig.VERSION_CODE)) ||
                    result.getTag().equals(BuildConfig.VERSION_NAME) ||
                    result.getTag().equals(String.valueOf(BuildConfig.VERSION_CODE))) {
                hideProgress();
                mLayout.setHeaderText(R.string.update_none);
                mBodyText.setText(null);
                mLatestResult = null;
            } else {

                hideProgress();
                mLayout.setHeaderText(getString(R.string.update_available,
                        result.getName()));
                mDownloadAction.setVisibility(View.VISIBLE);
                mLatestResult = result;
                RichText.fromMarkdown(result.getBody()).into(mBodyText);
            }
        }

        @Override
        public void start () {
           showProgress();
        }
    };

    private void showProgress () {
        mLayout.setProgressBarShown(true);
        mLayout.setHeaderText(R.string.update_progress);
        mBodyText.setVisibility(View.GONE);
        mDownloadAction.setVisibility(View.GONE);
        mRetryAction.setVisibility(View.GONE);
    }

    private void hideProgress () {
        mLayout.setProgressBarShown(false);
        mBodyText.setVisibility(View.VISIBLE);
        mRetryAction.setVisibility(View.VISIBLE);
    }
}
