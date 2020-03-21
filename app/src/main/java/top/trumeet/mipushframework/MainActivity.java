package top.trumeet.mipushframework;

import android.animation.Animator;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.UiThread;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.Toast;

import io.reactivex.disposables.CompositeDisposable;
import top.trumeet.common.Constants;
import top.trumeet.common.push.PushController;
import top.trumeet.common.utils.Utils;
import top.trumeet.common.utils.rom.RomUtils;
import top.trumeet.common.widget.LinkAlertDialog;
import top.trumeet.mipush.R;
import top.trumeet.mipushframework.control.CheckPermissionsUtils;
import top.trumeet.mipushframework.control.ConnectFailUtils;
import top.trumeet.mipushframework.control.OnConnectStatusChangedListener;

import static top.trumeet.common.Constants.TAG_PUSH;
import static top.trumeet.mipushframework.control.OnConnectStatusChangedListener.FAIL_REASON_LOW_VERSION;
import static top.trumeet.mipushframework.control.OnConnectStatusChangedListener.FAIL_REASON_MIUI;
import static top.trumeet.mipushframework.control.OnConnectStatusChangedListener.FAIL_REASON_NOT_INSTALLED;
import static top.trumeet.mipushframework.control.OnConnectStatusChangedListener.FAIL_REASON_SECURITY_EXCEPTION;
import static top.trumeet.mipushframework.control.OnConnectStatusChangedListener.FAIL_REASON_UNKNOWN;

/**
 * Main activity
 *
 * @author Trumeet
 */
public abstract class MainActivity extends AppCompatActivity {
    public static final String TAG = TAG_PUSH;

    private PushController mController;
    private View mConnectProgress;
    private ViewPropertyAnimator mProgressFadeOutAnimate;
    private ConnectTask mConnectTask;
    private MainFragment mFragment;
    private CompositeDisposable composite = new CompositeDisposable();

    public PushController getController() {
        return mController;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkAndConnect();
        //checkAndShowPlatformNotice();
    }

    @UiThread
    private void checkAndConnect() {
        Log.d(MainActivity.TAG, "MainActivity: checkAndConnect");
        if (!Utils.isServiceInstalled()) {
            showConnectFail(FAIL_REASON_NOT_INSTALLED);
            return;
        }
        composite.add(CheckPermissionsUtils.checkAndRun(result -> {
            switch (result) {
                case OK:
                    connect();
                    break;
                case PERMISSION_NEEDED:
                    Toast.makeText(this, getString(top.trumeet.common.R.string.request_permission), Toast.LENGTH_LONG)
                            .show();
                    // Restart to request permissions again.
                    checkAndConnect();
                    break;
                case PERMISSION_NEEDED_SHOW_SETTINGS:
                    Toast.makeText(this, getString(top.trumeet.common.R.string.request_permission), Toast.LENGTH_LONG)
                            .show();
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            .setData(uri)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    break;
                case REMOVE_DOZE_NEEDED:
                    Toast.makeText(this, getString(R.string.request_battery_whitelist), Toast.LENGTH_LONG)
                            .show();
                    checkAndConnect();
                    break;
                case BASE_PATH_WRONG:
                    Toast.makeText(this, getString(R.string.wrong_base_directory), Toast.LENGTH_LONG)
                            .show();
                    break;
                case BASE_PATH_MISSING:
                    Toast.makeText(this, getString(R.string.missing_base_directory), Toast.LENGTH_LONG)
                            .show();
                    break;
            }
        }, throwable -> {
            Log.e(MainActivity.TAG, "check permissions", throwable);
        }, this));
    }

    private void connect() {
        mConnectTask = new ConnectTask();
        mConnectTask.execute();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfiguration) {
        super.onConfigurationChanged(newConfiguration);
    }

    private void initRealView() {
        if (mConnectProgress != null) {
            mConnectProgress.setVisibility(View.GONE);
            mConnectProgress = null;
        }

        mFragment = new MainFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, mFragment)
                .commitAllowingStateLoss();

    }

    @Override
    public void onDestroy() {
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
        // Activity request should cancel in onPause?
        if (composite != null && !composite.isDisposed()) {
            composite.dispose();
        }
        super.onDestroy();
    }

    private class ConnectTask extends AsyncTask<Void, Void, Pair<Boolean /* success */, Integer /* reason */>> {
        @Override
        protected void onPreExecute() {
            mConnectProgress = LayoutInflater.from(MainActivity.this)
                    .inflate(R.layout.layout_progress, null);
            setContentView(mConnectProgress);
        }

        @Override
        protected Pair<Boolean, Integer> doInBackground(Void... voids) {
            if (RomUtils.getOs() == RomUtils.ROM_MIUI) {
                return new Pair<>(false, FAIL_REASON_MIUI);
            }
            if (!Utils.isServiceInstalled()) {
                return new Pair<>(false, FAIL_REASON_NOT_INSTALLED);
            }

            if (mController == null || !mController.isConnected()) {
                try {
                    mController = PushController.getConnected(MainActivity.this,
                            new PushController.AbstractConnectionStatusListener() {

                                @Override
                                public void onReady() {
                                    if (mFragment != null)
                                        mFragment.onChange(OnConnectStatusChangedListener.CONNECTED);
                                }

                                @Override
                                public void onDisconnected() {
                                    if (mFragment != null)
                                        mFragment.onChange(OnConnectStatusChangedListener.DISCONNECTED);
                                    checkAndConnect();
                                }

                            });
                } catch (java.lang.SecurityException e) {
                    return new Pair<>(false, FAIL_REASON_SECURITY_EXCEPTION);
                }
            }

            boolean success = (mController != null) && mController.isConnected();
            if (success) {
                int version = mController.getVersionCode();
                if (version != Constants.PUSH_SERVICE_VERSION_CODE) {
                    return new Pair<>(false, FAIL_REASON_LOW_VERSION);
                }
                return new Pair<>(true, null);
            }

            return new Pair<>(false, FAIL_REASON_UNKNOWN);
        }

        @Override
        protected void onPostExecute(Pair<Boolean, Integer> success) {
            if (mFragment != null) {
                mFragment.onChange(success.first ? OnConnectStatusChangedListener.CONNECTED :
                        OnConnectStatusChangedListener.DISCONNECTED);
            }
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

    private synchronized void showConnectFail(@OnConnectStatusChangedListener.FailReason
                                                      int reason) {
        new LinkAlertDialog.Builder(this)
                .setTitle(ConnectFailUtils.getTitle(this, reason))
                .setMessage(ConnectFailUtils.getSummary(this, reason,
                        (mController != null && mController.isConnected()) ?
                                mController.getVersionCode() : -1))
                .setCancelable(false)
                .setPositiveButton(R.string.retry, (dialog, which) -> checkAndConnect())
                .setNegativeButton(R.string.exit, (dialog, which) -> finish())
                .show();
        if (mController != null && mController.isConnected()) {
            mController.disconnectIfNeeded();
        }
    }
}
