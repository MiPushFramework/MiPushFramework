package top.trumeet.common.push;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.annotation.RestrictTo;
import android.util.Log;

import top.trumeet.common.Constants;

/**
 * @author Trumeet
 * @date 2017/12/22
 * The client of PushControllerService
 */

public abstract class PushController {
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static final Uri AUTHORITIES = Uri.parse("content://com.xiaomi.xmsf.push.control.APIProvider");

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static final String METHOD_IS_ENABLE = PushController.class.getName() + "_isEnable";
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static final String METHOD_SET_ENABLE = PushController.class.getName() + "_setEnable";
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static final String METHOD_GET_VERSION = PushController.class.getName() + "_getVersion";
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static final String METHOD_CHECK_OP = PushController.class.getName() + "_checkOp";

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static final String ARG_STRICT = PushController.class.getName() + ".ARG_STRICT";
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static final String ARG_OP = PushController.class.getName() + ".ARG_OP";
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static final String ARG_VERSION = PushController.class.getName() + ".ARG_VERSION";
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static final String ARG_IS_ENABLE = PushController.class.getName() + ".ARG_IS_ENABLE";
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static final String ARG_OP_STATUS = PushController.class.getName() + ".ARG_OP_STATUS";

    public static abstract class AbstractConnectionStatusListener {
        public void onReady() {
        }

        public void onDisconnected() {
        }
    }

    private Context mContext;

    @RequiresPermission(Constants.permissions.BIND)
    public static PushController getConnected(@NonNull Context context, @Nullable final LegacyImpl.AbstractConnectionStatusListener disconnectListener) {
        if (isLegacySupported(context)) return LegacyImpl.getConnected(context, disconnectListener);
        else return ControllerImpl.getConnected(context, disconnectListener);
    }

    private static boolean isLegacySupported (@NonNull Context context) {
        // 存在且不存在的时候走 Legacy
        boolean support =
                context.getPackageManager().resolveService(new Intent().setComponent(new ComponentName(Constants.SERVICE_APP_NAME,
                        Constants.CONTROLLER_SERVICE_NAME)), 0) != null &&
                context.getPackageManager().resolveContentProvider(ControllerImpl.COMPONENT, 0) == null;
        Log.i("PushController", "isLegacySupported: " + support);
        return support;
    }

    PushController (@NonNull Context context) {
        mContext = context;
    }

    public static PushController create (@NonNull Context context) {
        if (!isLegacySupported(context))
            return new ControllerImpl(context);
        else
            return new LegacyImpl(context);
    }

    @RequiresPermission(Constants.permissions.BIND)
    @Deprecated
    public void connect (@Nullable final LegacyImpl.AbstractConnectionStatusListener listener) {
    }

    public void disconnect () {
    }

    @RequiresPermission(value = Constants.permissions.READ_SETTINGS)
    public abstract boolean isEnable(boolean strict);

    @RequiresPermission(value = Constants.permissions.WRITE_SETTINGS)
    public abstract void setEnable(boolean enable);

    @RequiresPermission(value = Constants.permissions.READ_SETTINGS)
    public abstract int checkOp(int op);

    public boolean isConnected() { return false; }

    public void disconnectIfNeeded() {}

    @NonNull
    Context getContext () {
        return mContext;
    }

    @RequiresPermission(anyOf = {Constants.permissions.READ_SETTINGS, Constants.permissions_old.GET_VERSION})
    public abstract int getVersionCode();
}
