package top.trumeet.mipushframework;

import android.animation.Animator;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewPropertyAnimator;

import com.xiaomi.xmsf.R;

import top.trumeet.common.Constants;
import top.trumeet.common.push.PushController;
import top.trumeet.common.utils.Utils;
import top.trumeet.common.widget.LinkAlertDialog;
import top.trumeet.mipushframework.control.ConnectFailUtils;
import top.trumeet.mipushframework.control.OnConnectStatusChangedListener;

import static top.trumeet.mipushframework.control.OnConnectStatusChangedListener.FAIL_REASON_LOW_VERSION;
import static top.trumeet.mipushframework.control.OnConnectStatusChangedListener.FAIL_REASON_NOT_INSTALLED;
import static top.trumeet.mipushframework.control.OnConnectStatusChangedListener.FAIL_REASON_UNKNOWN;

/**
 * Main activity
 * @author Trumeet
 */
public abstract class MainActivity extends AppCompatActivity {
    private PushController mController;
    private View mConnectProgress;
    private ViewPropertyAnimator mProgressFadeOutAnimate;
    private ConnectTask mConnectTask;
    private MainFragment mFragment;

    public PushController getController () {
        return mController;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        connect();
    }

    private void connect () {
        mConnectTask = new ConnectTask();
        mConnectTask.execute();
    }

    @Override
    public void  onConfigurationChanged(Configuration newConfiguration) {
        super.onConfigurationChanged(newConfiguration);
    }

    private void initRealView () {
        mConnectProgress.setVisibility(View.GONE);
        mConnectProgress = null;

        mFragment = new MainFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, mFragment)
                .commitAllowingStateLoss();
    }

    @Override
    public void onDestroy () {
        if (mController != null) {
            mController.disconnectIfNeeded();
            mController = null;
        }
        if (mConnectTask != null) {
            if (mConnectTask.isCancelled()) {
                mConnectTask.cancel(true);
            }
            mConnectTask = null;
        }
        if (mProgressFadeOutAnimate != null) {
            mProgressFadeOutAnimate.cancel();
            mProgressFadeOutAnimate = null;
        }
        super.onDestroy();
    }

    private class ConnectTask extends AsyncTask<Void, Void, Pair<Boolean /* success */, Integer /* reason */>> {
        @Override
        protected void onPreExecute () {
            mConnectProgress = LayoutInflater.from(MainActivity.this)
                    .inflate(R.layout.layout_progress, null);
            setContentView(mConnectProgress);
        }

        @Override
        protected Pair<Boolean, Integer> doInBackground(Void... voids) {
            if (mController == null || !mController.isConnected()) {
                mController = PushController.getConnected(MainActivity.this,
                        new PushController.OnReadyListener() {
                            @Override
                            public void onDisconnected() {
                                mFragment.onChange(OnConnectStatusChangedListener.DISCONNECTED);

                                connect();
                            }
                        });
            }
            boolean success = mController.isConnected();
            if (success) {
                int version = mController.getVersionCode();
                if (version != Constants.PUSH_SERVICE_VERSION_CODE)
                    return new Pair<>(false, FAIL_REASON_LOW_VERSION);
                return new Pair<>(true, null);
            }
            if (!Utils.isServiceInstalled())
                return new Pair<>(false, FAIL_REASON_NOT_INSTALLED);
            return new Pair<>(false, FAIL_REASON_UNKNOWN);
        }

        @Override
        protected void onPostExecute (Pair<Boolean, Integer> success) {
            if (mFragment != null) mFragment.onChange(success.first ? OnConnectStatusChangedListener.CONNECTED :
            OnConnectStatusChangedListener.DISCONNECTED);
            if (success.first) {
                mProgressFadeOutAnimate = mConnectProgress.animate()
                        .alpha(1f)
                        .setDuration(700)
                        .setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                initRealView();
                                mProgressFadeOutAnimate = null;
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {
                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {
                            }
                        });
            } else {
                showConnectFail(success.second);
            }
        }
    }

    private synchronized void showConnectFail (@OnConnectStatusChangedListener.FailReason
                                          int reason) {
        new LinkAlertDialog.Builder(this)
                .setTitle(ConnectFailUtils.getTitle(this, reason))
                .setMessage(ConnectFailUtils.getSummary(this, reason,
                        (mController != null && mController.isConnected()) ?
                mController.getVersionCode() : -1))
        .setCancelable(false)
        .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                connect();
            }
        })
        .setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        })
        .show();
        if (mController != null && mController.isConnected())
            mController.disconnectIfNeeded();
    }
}
