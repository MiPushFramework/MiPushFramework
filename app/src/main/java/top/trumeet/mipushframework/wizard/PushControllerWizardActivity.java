package top.trumeet.mipushframework.wizard;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.app.FragmentActivity;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.setupwizardlib.SetupWizardLayout;

import io.reactivex.disposables.CompositeDisposable;
import top.trumeet.common.Constants;
import top.trumeet.common.push.PushController;
import top.trumeet.common.utils.Utils;
import top.trumeet.common.utils.rom.RomUtils;
import top.trumeet.mipush.R;
import top.trumeet.mipushframework.control.CheckPermissionsUtils;
import top.trumeet.mipushframework.control.ConnectFailUtils;
import top.trumeet.mipushframework.control.OnConnectStatusChangedListener;

import static top.trumeet.mipushframework.control.OnConnectStatusChangedListener.FAIL_REASON_LOW_VERSION;
import static top.trumeet.mipushframework.control.OnConnectStatusChangedListener.FAIL_REASON_MIUI;
import static top.trumeet.mipushframework.control.OnConnectStatusChangedListener.FAIL_REASON_NOT_INSTALLED;
import static top.trumeet.mipushframework.control.OnConnectStatusChangedListener.FAIL_REASON_SECURITY_EXCEPTION;
import static top.trumeet.mipushframework.control.OnConnectStatusChangedListener.FAIL_REASON_UNKNOWN;

/**
 * Created by Trumeet on 2017/12/30.
 */

public abstract class PushControllerWizardActivity extends FragmentActivity {
    private static final String TAG = PushControllerWizardActivity.class.getSimpleName();

    public TextView mText;
    private PushController mController;
    public SetupWizardLayout layout;
    private ConnectTask mConnectTask;
    private Bundle savedInstanceState;

    private CompositeDisposable composite = new CompositeDisposable();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.savedInstanceState = savedInstanceState;

        layout = new SetupWizardLayout(this);
        mText = new TextView(this);
        //textView.setText(Html.fromHtml(getString(R.string.wizard_descr_run_in_background)));
        int padding = (int) getResources().getDimension(R.dimen.suw_glif_margin_sides);
        mText.setPadding(padding, padding, padding, padding);
        mText.setMovementMethod(LinkMovementMethod.getInstance());
        layout.addView(mText);
        layout.setHeaderText(R.string.wizard_connect);
        setContentView(layout);
    }

    @UiThread
    private void checkAndConnect() {
        Log.d("MainActivity", "checkAndConnect");
        if (!Utils.isServiceInstalled()) {
            showConnectFail(FAIL_REASON_NOT_INSTALLED);
            return;
        }
        composite.add(CheckPermissionsUtils.checkPermissionsAndStartAsync(this,
                (result) -> {
                    if (result.permissionResult.granted &&
                            result.activityResult.resultCode() == Activity.RESULT_OK) {
                        // Connect
                        if (mConnectTask != null) {
                            if (!mConnectTask.isCancelled()) {
                                mConnectTask.cancel(true);
                            }
                            mConnectTask = null;
                        }
                        mConnectTask = new PushControllerWizardActivity.ConnectTask();
                        mConnectTask.execute();
                    } else {
                        if (!result.permissionResult.granted) {
                            Toast.makeText(this, getString(top.trumeet.common.R.string.request_permission,
                                    result.permissionResult.name), Toast.LENGTH_LONG)
                                    .show();
                            if (!result.permissionResult.shouldShowRequestPermissionRationale) {
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                        .setData(uri)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                            } else {
                                checkAndConnect();
                            }
                        }
                        if (result.activityResult.resultCode() != Activity.RESULT_OK) {
                            Toast.makeText(this, getString(R.string.request_battery_whitelist), Toast.LENGTH_LONG)
                                    .show();
                            checkAndConnect();
                        }
                    }
                }, (throwable) -> {
                    Log.e(TAG, "CheckPermissions", throwable);
                }));
    }

    @Override
    public void onDestroy () {
        if (mConnectTask != null && !mConnectTask.isCancelled()) {
            mConnectTask.cancel(true);
        }
        mConnectTask = null;
        if (mController != null && mController.isConnected())
            mController.disconnectIfNeeded();
        super.onDestroy();
    }

    public void connect () {
        checkAndConnect();
    }

    private class ConnectTask extends AsyncTask<Void, Void, Pair<Boolean /* success */, Integer /* reason */>> {
        @Override
        protected void onPreExecute () {
            layout.setHeaderText(R.string.wizard_connect);
            layout.setProgressBarShown(true);
            mText.setText(null);
            layout.getNavigationBar()
                    .getBackButton()
                    .setVisibility(View.GONE);
            layout.getNavigationBar()
                    .getNextButton()
                    .setVisibility(View.GONE);
            layout.getNavigationBar()
                    .getNextButton()
                    .setText(R.string.retry);
            layout.getNavigationBar()
                    .getNextButton()
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            connect();
                        }
                    });
        }

        @Override
        protected Pair<Boolean, Integer> doInBackground(Void... voids) {
            SystemClock.sleep(1000);
            if (RomUtils.getOs() == RomUtils.ROM_MIUI) {
                return new Pair<>(false, FAIL_REASON_MIUI);
            }
            if (!Utils.isServiceInstalled()) {
                return new Pair<>(false, FAIL_REASON_NOT_INSTALLED);
            }
            if (mController == null || !mController.isConnected()) {
                try {
                    mController = PushController.getConnected(PushControllerWizardActivity.this,
                            new PushController.AbstractConnectionStatusListener() {
                                @Override
                                public void onDisconnected() {
                                    PushControllerWizardActivity.this.
                                            onDisconnected();
                                    onReConnect();
                                    connect();
                                }
                            });
                } catch (java.lang.SecurityException e) {
                    return new Pair<>(false, FAIL_REASON_SECURITY_EXCEPTION);
                }

            }
            boolean success =  (mController != null) && mController.isConnected();
            if (success) {
                int version = mController.getVersionCode();
                if (version != Constants.PUSH_SERVICE_VERSION_CODE)
                    return new Pair<>(false, FAIL_REASON_LOW_VERSION);
                return new Pair<>(true, null);
            }

            return new Pair<>(false, FAIL_REASON_UNKNOWN);
        }

        @Override
        protected void onPostExecute (final Pair<Boolean, Integer> success) {
            if (success.first) {
                layout.setProgressBarShown(false);
                layout.getNavigationBar()
                        .getNextButton()
                        .setVisibility(View.VISIBLE);
                layout.getNavigationBar()
                        .getNextButton()
                        .setText(com.android.setupwizardlib.R.string.suw_next_button_label);
                layout.getNavigationBar()
                        .getBackButton()
                        .setVisibility(View.VISIBLE);
                layout.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("", "connected");
                        onConnected(mController, savedInstanceState);
                    }
                });
            } else {
                layout.setProgressBarShown(false);
                showConnectFail(success.second);
            }
        }
    }

    private void showConnectFail (@OnConnectStatusChangedListener.FailReason
                                          int reason) {
        layout.setHeaderText(ConnectFailUtils.getTitle(this, reason));
        mText.setText(ConnectFailUtils.getSummary(this, reason,
                (mController != null && mController.isConnected()) ?
                        mController.getVersionCode() : -1));
        layout.getNavigationBar()
                .getNextButton()
                .setVisibility(View.VISIBLE);
        if (mController != null && mController.isConnected())
            mController.disconnectIfNeeded();
    }

    public PushController getController () {
        return mController;
    }

    @CallSuper
    public void onStartConnect () {}

    @CallSuper
    public void onReConnect () {}

    @CallSuper
    public void onDisconnected () {}

    public void onConnected (@NonNull PushController controller,
                             @Nullable Bundle savedInstanceState) {}

    public final boolean isConnecting () {
        return mConnectTask != null && !mConnectTask.isCancelled() &&
                mController != null && !mController.isConnected();
    }
}
