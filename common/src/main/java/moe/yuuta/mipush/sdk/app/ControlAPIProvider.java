package moe.yuuta.mipush.sdk.app;

import android.app.AppOpsManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import top.trumeet.common.Constants;
import top.trumeet.common.override.AppOpsManagerOverride;
import top.trumeet.common.utils.Utils;

import static android.content.Context.APP_OPS_SERVICE;
import static top.trumeet.common.push.PushController.ARG_IS_ENABLE;
import static top.trumeet.common.push.PushController.ARG_OP;
import static top.trumeet.common.push.PushController.ARG_OP_STATUS;
import static top.trumeet.common.push.PushController.ARG_VERSION;
import static top.trumeet.common.push.PushController.METHOD_CHECK_OP;
import static top.trumeet.common.push.PushController.METHOD_GET_VERSION;
import static top.trumeet.common.push.PushController.METHOD_IS_ENABLE;
import static top.trumeet.common.push.PushController.METHOD_SET_ENABLE;

public abstract class ControlAPIProvider extends ContentProvider {
    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public final Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public final String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public final Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public final int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public final int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    private static boolean checkPermission (String permName) {
        return Utils.getApplication().checkPermission(permName, Binder.getCallingPid(),
                Binder.getCallingUid()) == PackageManager.PERMISSION_GRANTED;
    }

    private static void enforcePermission (String permName) {
        if (!checkPermission(permName)) {
            throw new SecurityException("Uid " + Binder.getCallingUid() + " doesn't have permission " + permName);
        }
    }

    @Nullable
    @Override
    public final Bundle call(@NonNull String method, @Nullable String arg, @Nullable Bundle extras) {
        if (METHOD_GET_VERSION.equals(method)) {
            enforcePermission(Constants.permissions.READ_SETTINGS);
            Bundle bundle = new Bundle();
            bundle.putInt(ARG_VERSION, getVersion(extras));
            return bundle;
        }
        if (METHOD_CHECK_OP.equals(method)) {
            enforcePermission(Constants.permissions.READ_SETTINGS);
            int op = extras.getInt(ARG_OP);
            int mode = AppOpsManagerOverride.checkOpNoThrow(
                    op
                    , Process.myUid(), getContext().getPackageName(), ((AppOpsManager)getContext().getSystemService(APP_OPS_SERVICE)));
            Bundle bundle = new Bundle();
            bundle.putInt(ARG_OP_STATUS, mode);
            return bundle;
        }
        if (METHOD_IS_ENABLE.equals(method)) {
            enforcePermission(Constants.permissions.READ_SETTINGS);
            Bundle bundle = new Bundle();
            bundle.putBoolean(ARG_IS_ENABLE, isEnable(extras));
            return bundle;
        }
        if (METHOD_SET_ENABLE.equals(method)) {
            enforcePermission(Constants.permissions.WRITE_SETTINGS);
            boolean enable = extras.getBoolean(ARG_IS_ENABLE);
            setEnable(enable, extras);
            return null;
        }
        throw new UnsupportedOperationException();
    }

    public abstract int getVersion (@Nullable Bundle args);
    public abstract boolean isEnable (@Nullable Bundle args);
    public abstract void setEnable (boolean enable, @Nullable Bundle args);
}