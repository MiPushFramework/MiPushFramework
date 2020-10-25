package top.trumeet.mipushframework.wizard;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.app.FragmentActivity;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.setupwizardlib.SetupWizardLayout;
import com.xiaomi.xmsf.R;

import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.disposables.CompositeDisposable;
import top.trumeet.mipushframework.control.CheckPermissionsUtils;

/**
 * Created by Trumeet on 2017/12/30.
 */

public abstract class PushControllerWizardActivity extends FragmentActivity {
    private static final String TAG = PushControllerWizardActivity.class.getSimpleName();

    public TextView mText;

    public SetupWizardLayout layout;
    private ConnectTask mConnectTask;
    private Bundle savedInstanceState;

    private AtomicBoolean mConnected = new AtomicBoolean(false);

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
        setContentView(layout);
    }

    @UiThread
    private void checkAndConnect() {
        log("checkAndConnect");

        layout.setHeaderText(R.string.wizard_connect);
        checkPermissionAndRun(() -> {
            log("Checking pass");
            if (mConnectTask != null && !mConnectTask.isCancelled()) {
                mConnectTask.cancel(true);
                mConnectTask = null;
            }
            mConnectTask = new ConnectTask();
            mConnectTask.execute();
        });
    }

    private void checkPermissionAndRun (@NonNull Runnable action) {
        composite.add(CheckPermissionsUtils.checkAndRun(result -> {
            switch (result) {
                case OK:
                    Log.d(TAG, "Check: OK");
                    action.run();
                    break;
                case PERMISSION_NEEDED:
                    Toast.makeText(this, getString(top.trumeet.common.R.string.request_permission), Toast.LENGTH_LONG)
                            .show();
                    Log.d(TAG, "Check: PERMISSION_NEEDED");
                    // Restart to request permissions again.
                    checkPermissionAndRun(action);
                    break;
                case PERMISSION_NEEDED_SHOW_SETTINGS:
                    Log.d(TAG, "Check: PERMISSION_NEEDED_SHOW_SETTINGS");
                    Toast.makeText(this, getString(top.trumeet.common.R.string.request_permission), Toast.LENGTH_LONG)
                            .show();
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            .setData(uri)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    break;
                case REMOVE_DOZE_NEEDED:
                    Log.d(TAG, "Check: REMOVE_DOZE_NEEDED");
                    showConnectFail(getString(R.string.connect_fail_doze), getString(R.string.request_battery_whitelist));
                    break;
            }
        }, throwable -> {
            Log.e(TAG, "check permissions", throwable);
        }, this));
    }

    @Override
    public void onDestroy () {
        if (mConnectTask != null && !mConnectTask.isCancelled()) {
            mConnectTask.cancel(true);
        }
        mConnectTask = null;
        super.onDestroy();
    }

    public void connect () {
        checkAndConnect();
    }

    private class ConnectTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute () {
            mConnected.set(false);
            showConnecting();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return true;
        }

        @Override
        protected void onPostExecute (final Boolean success) {
            layout.setProgressBarShown(false);
            mConnected.set(true);
            showConnectSuccess();
        }
    }

    private void showConnecting () {
        log("showConnecting");
        layout.setHeaderText(R.string.wizard_connect);
        layout.setProgressBarShown(true);
        mText.setText(null);
        layout.getNavigationBar()
                .getBackButton()
                .setVisibility(View.GONE);
        layout.getNavigationBar()
                .getNextButton()
                .setVisibility(View.GONE);
    }

    private void showConnectSuccess () {
        log("showConnectSuccess");
        layout.getNavigationBar()
                .getNextButton()
                .setVisibility(View.VISIBLE);
        layout.getNavigationBar()
                .getNextButton()
                .setText(com.android.setupwizardlib.R.string.suw_next_button_label);
        layout.getNavigationBar()
                .getBackButton()
                .setVisibility(View.VISIBLE);
        layout.post(() -> {
            log("connected");
            onConnected(savedInstanceState);
        });
    }



    private void showConnectFail(CharSequence title, CharSequence reason) {
        log("showConnectFail");
        layout.setHeaderText(title);
        mText.setText(reason);
        layout.getNavigationBar()
                .getNextButton()
                .setVisibility(View.VISIBLE);
        layout.getNavigationBar()
                .getNextButton()
                .setText(R.string.retry);
        layout.getNavigationBar()
                .getNextButton()
                .setOnClickListener(v -> connect());

    }


    public void onConnected(@Nullable Bundle savedInstanceState) {
        log("lifecycle: onConnected");
    }


    private void log (String message) {
        Log.d(TAG + "/" + getClass().getSimpleName(), message);
    }
}