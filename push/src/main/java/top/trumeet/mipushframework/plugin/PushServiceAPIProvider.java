package top.trumeet.mipushframework.plugin;

import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import top.trumeet.common.Constants;
import top.trumeet.common.plugin.PluginManager;
import top.trumeet.common.utils.Utils;

/**
 * 用于推送提供服务使用 API 的 Provider。
 * 主要 API：显示通知
 *
 * @author Trumeet
 */
public class PushServiceAPIProvider extends ContentProvider {
    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Nullable
    @Override
    public Bundle call(@NonNull String method, @Nullable String arg, @Nullable Bundle extras) {

        if (extras == null)
            throw new NullPointerException("extras is null");
        ComponentName componentName = extras.getParcelable(PluginManager.ARG_COMPONENT);
        PluginAuth.enforceEnabled(componentName);

        // TODO: Add more APIs
        switch (method) {
            default:
                throw new IllegalArgumentException("Unsupported method " + method);
        }
    }


}




