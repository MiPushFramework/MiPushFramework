package top.trumeet.common.db;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.CancellationSignal;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.util.Log;

import java.util.List;

import top.trumeet.common.Constants;
import top.trumeet.common.register.RegisteredApplication;
import top.trumeet.common.utils.DatabaseUtils;

import static top.trumeet.common.BuildConfig.DEBUG;
import static top.trumeet.common.register.RegisteredApplication.KEY_PACKAGE_NAME;

/**
 * Created by Trumeet on 2017/12/23.
 */

public class RegisteredApplicationDb {
    public static final String AUTHORITY = "top.trumeet.mipush.providers.AppProvider";
    public static final String BASE_PATH = "RegisteredApplication";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    private static DatabaseUtils getInstance (@NonNull Context context) {
        return new DatabaseUtils(CONTENT_URI, context.getContentResolver());
    }

    public static RegisteredApplication
    registerApplication (String pkg, boolean autoCreate, Context context,
                         CancellationSignal signal) {
        List<RegisteredApplication> list = getList(context, pkg, signal);
        if (DEBUG) {
            Log.d("RegisteredApplicationDb", "register -> existing list = " + list.toString());
        }
        return list.isEmpty() ?
                (autoCreate ? create(pkg, context) : null) : list.get(0);
    }


    public static List<RegisteredApplication>
    getList (Context context,
             @Nullable String pkg,
             CancellationSignal signal) {
        return getInstance(context)
                .queryAndConvert(signal, pkg != null ? KEY_PACKAGE_NAME + "=?" :
                        null,
                        pkg != null ? new String[]{pkg} : null, null, new DatabaseUtils.Converter<RegisteredApplication>() {
                            @SuppressLint("MissingPermission")
                            @android.support.annotation.NonNull
                            @Override
                            public RegisteredApplication convert
                                    (@android.support.annotation.NonNull Cursor cursor) {
                                return RegisteredApplication.create(cursor);
                            }
                        });
    }


    public static int update (RegisteredApplication application,
                               Context context) {
        return getInstance(context)
                .update(application.toValues(), DatabaseUtils.KEY_ID + "=?",
                        new String[]{application.getId().toString()});
    }


    private static RegisteredApplication create (String pkg,
                                                 Context context) {
        // TODO: Configurable defaults; use null for optional and global options?
        RegisteredApplication registeredApplication =
                new RegisteredApplication(null, pkg
                        , RegisteredApplication.Type.ASK,
                        true /* Allow push */,
                        true /* Allow receive result */,
                        true /* Allow receive command */,
                        0 /* registeredType Don't store to DB */,
                        true /* notification on register */);
        insert(registeredApplication, context);

        // Very bad
        return registerApplication(pkg, false, context,
                null);
    }


    private static Uri insert (RegisteredApplication application,
                                Context context) {
        return getInstance(context).insert(application.toValues());
    }
}
