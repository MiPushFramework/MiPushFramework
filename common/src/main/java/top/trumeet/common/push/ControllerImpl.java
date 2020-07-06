package top.trumeet.common.push;

import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import top.trumeet.common.Constants;

/**
 * 使用 ContentProvider 作为 IPC 方式
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
class ControllerImpl extends PushController {
    static final String COMPONENT = Constants.SERVICE_APP_NAME + ".push.control.APIProvider";

    ControllerImpl(@NonNull Context context) {
        super(context);
    }

    private @Nullable Bundle call(@NonNull String method,
                @Nullable String arg, @Nullable Bundle extras) {
        return getContext().getContentResolver().call(AUTHORITIES, method, arg, extras);
    }

    @Override
    public void connect(@Nullable AbstractConnectionStatusListener listener) {
        if (listener != null) listener.onReady();
    }

    @SuppressLint("MissingPermission")
    @NonNull
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static PushController getConnected(@NonNull Context context, @Nullable final AbstractConnectionStatusListener disconnectListener) {
        PushController controller = new ControllerImpl(context);
        if (disconnectListener != null)
            new Handler(Looper.getMainLooper())
                .postDelayed(disconnectListener::onReady, 300);
        return controller;
    }

    @Override
    public boolean isEnable(boolean strict) {
        Bundle args = new Bundle();
        args.putBoolean(ARG_STRICT, strict);
        return call(METHOD_IS_ENABLE, null, args)
                .getBoolean(ARG_IS_ENABLE);
    }

    @Override
    public void setEnable(boolean enable) {
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_ENABLE, enable);
        call(METHOD_SET_ENABLE, null, args);
    }

    @Override
    public int checkOp(int op) {
        Bundle args = new Bundle();
        args.putInt(ARG_OP, op);
        return call(METHOD_CHECK_OP, null, args)
                .getInt(ARG_OP_STATUS, AppOpsManager.MODE_ERRORED);
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public int getVersionCode() {
        return call(METHOD_GET_VERSION, null, null)
                .getInt(ARG_VERSION, -1);
    }
}
