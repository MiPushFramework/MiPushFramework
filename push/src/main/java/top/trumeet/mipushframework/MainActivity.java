package top.trumeet.mipushframework;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.UiThread;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.Toast;

import com.xiaomi.xmsf.R;

import io.reactivex.disposables.CompositeDisposable;
import top.trumeet.mipushframework.control.CheckPermissionsUtils;

/**
 * Main activity
 *
 * @author Trumeet
 */
public abstract class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private View mConnectProgress;
    private ViewPropertyAnimator mProgressFadeOutAnimate;
    private MainFragment mFragment;
    private CompositeDisposable composite = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkAndConnect();
    }

    @UiThread
    private void checkAndConnect() {
        Log.d("MainActivity", "checkAndConnect");
        composite.add(CheckPermissionsUtils.checkAndRun(result -> {
            switch (result) {
                case OK:
                    initRealView();
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
            }
        }, throwable -> {
            Log.e(TAG, "check permissions", throwable);
        }, this));
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

}
