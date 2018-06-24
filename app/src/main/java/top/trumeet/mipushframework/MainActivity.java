package top.trumeet.mipushframework;

import android.animation.Animator;
import android.content.DialogInterface;
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

import top.trumeet.common.Constants;
import top.trumeet.common.plugin.PlatformUtils;
import top.trumeet.common.push.PushController;
import top.trumeet.common.utils.PermissionUtils;
import top.trumeet.common.utils.Utils;
import top.trumeet.common.utils.rom.RomUtils;
import top.trumeet.common.widget.LinkAlertDialog;
import top.trumeet.mipush.R;
import top.trumeet.mipushframework.control.ConnectFailUtils;
import top.trumeet.mipushframework.control.OnConnectStatusChangedListener;

import static top.trumeet.mipush.BuildConfig.DEBUG;
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
public abstract class MainActivity extends AppCompatActivity implements PermissionUtils.PermissionGrantListener {
    private PushController mController;
    private View mConnectProgress;
    private ViewPropertyAnimator mProgressFadeOutAnimate;
    private ConnectTask mConnectTask;
    private MainFragment mFragment;

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
        Log.d("MainActivity", "checkAndConnect");
        if (!Utils.isServiceInstalled()) {
            showConnectFail(FAIL_REASON_NOT_INSTALLED);
            return;
        }
        PermissionUtils.requestPermissions(this, new String[]{Constants.permissions.WRITE_SETTINGS});
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
            if (RomUtils.isMiui()) {
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
                                    mFragment.onChange(OnConnectStatusChangedListener.CONNECTED);
                                }

                                @Override
                                public void onDisconnected() {
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
                .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        checkAndConnect();
                    }
                })
                .setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .show();
        if (mController != null && mController.isConnected()) {
            mController.disconnectIfNeeded();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (PermissionUtils.handle(this, requestCode, permissions,
                grantResults)) {
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onResult(boolean granted, boolean blocked, String permName) {
        if (DEBUG) {
            Log.d("MainActivity", "onResult -> " + granted + ", " + blocked + ", " + permName);
        }
        if (Constants.permissions.WRITE_SETTINGS.equalsIgnoreCase(permName)) {
            String permDisplayName = PermissionUtils.getName(permName);

            if (granted || (permDisplayName == null)) {
                connect();
            } else {
                Toast.makeText(this, getString(top.trumeet.common.R.string.request_permission, permDisplayName), Toast.LENGTH_LONG)
                        .show();
                if (blocked) {
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            .setData(uri)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                } else {
                    PermissionUtils.requestPermissionsIfNeeded(this,
                            new String[]{permName});
                }
            }
        }
    }

    private void checkAndShowPlatformNotice() {
        if (PlatformUtils.isPlatformModeSupported() &&
                !PlatformUtils.isServicePlatformSign()) {
            Toast.makeText(this, Utils.getString(R.string.platform_suggestion_toast,
                    this), Toast.LENGTH_LONG).show();
        }
    }
}
