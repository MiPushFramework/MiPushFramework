package top.trumeet.mipushframework.control;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.internal.functions.Functions;
import rx_activity_result2.RxActivityResult;
import top.trumeet.common.Constants;
import top.trumeet.common.push.PushServiceAccessibility;
import top.trumeet.mipushframework.MainActivity;
import top.trumeet.mipushframework.MiPushFramework;
import top.trumeet.mipushframework.models.ActivityResultAndPermissionResult;

import static top.trumeet.common.Constants.FAKE_CONFIGURATION_PATH;

public class CheckPermissionsUtils {
    public static Disposable checkPermissionsAndStartAsync (@NonNull FragmentActivity context) {
        return checkPermissionsAndStartAsync(context, Functions.emptyConsumer(), Functions.ON_ERROR_MISSING);
    }

    public static Disposable checkPermissionsAndStartAsync (@NonNull FragmentActivity context, @NonNull Consumer<ActivityResultAndPermissionResult> onNext) {
        return checkPermissionsAndStartAsync(context, onNext, Functions.ON_ERROR_MISSING);
    }

    public static Disposable checkPermissionsAndStartAsync (@NonNull FragmentActivity context,
                                                            @NonNull Consumer<ActivityResultAndPermissionResult> onNext,
                                                            @NonNull Consumer<Throwable> onError) {
        return Observable.zip(buildRemoveDozeObservable(context)
                , new RxPermissions(context)
                        .requestEachCombined(Constants.permissions.WRITE_SETTINGS),
                ActivityResultAndPermissionResult::new)
                .subscribe(onNext, onError);
    }



    private static Observable<Boolean> buildRemoveDozeObservable (@NonNull FragmentActivity context) {
        return Observable.<Boolean>create(emitter -> {
            if (PushServiceAccessibility.isInDozeWhiteList(context)) {
                emitter.onNext(true);
            }
            emitter.onComplete();
        }).switchIfEmpty(RxActivityResult.on(context)
                .startIntent(new Intent()
                        .setComponent(new ComponentName(Constants.SERVICE_APP_NAME,
                                Constants.REMOVE_DOZE_COMPONENT_NAME))).map(fragmentActivityResult -> {
                                    return /* Older push don't return OK if granted. */
                                            PushServiceAccessibility.isInDozeWhiteList(context);
                }));
    }

    public enum CheckResult {
        OK,
        PERMISSION_NEEDED,
        PERMISSION_NEEDED_SHOW_SETTINGS,
        REMOVE_DOZE_NEEDED,
        BASE_PATH_WRONG,
        BASE_PATH_MISSING
    }

    private static File getCanonicalFile(File file) {
        try {
            return file.getCanonicalFile();
        } catch (IOException e) {
            return file.getAbsoluteFile();
        }
    }

    public static Disposable checkAndRun (Consumer<CheckResult> action, Consumer<Throwable> onError, FragmentActivity context) {
        return CheckPermissionsUtils.checkPermissionsAndStartAsync(context,
                (result) -> {
                    final ApplicationInfo appInfo = context.getApplicationInfo();
                    final File baseDir = new File(FAKE_CONFIGURATION_PATH);
                    final File baseDirCanonical = getCanonicalFile(baseDir);
                    final File baseDirActual = new File(Build.VERSION.SDK_INT >= 24 ? appInfo.deviceProtectedDataDir : appInfo.dataDir);
                    final File baseDirActualCanonical = getCanonicalFile(baseDirActual);
                    if (result.permissionResult.granted &&
                            result.removeDozeResult) {
                        action.accept(CheckResult.OK);
                        return;
                    }
                    if (!result.permissionResult.granted) {
                        if (!result.permissionResult.shouldShowRequestPermissionRationale) {
                            action.accept(CheckResult.PERMISSION_NEEDED_SHOW_SETTINGS);
                        } else {
                            action.accept(CheckResult.PERMISSION_NEEDED);
                        }
                        return;
                    }
                    if (!result.removeDozeResult) {
                        action.accept(CheckResult.REMOVE_DOZE_NEEDED);
                    }
                    if (!baseDirCanonical.equals(baseDirActualCanonical)) {
                        action.accept(CheckResult.BASE_PATH_WRONG);
                    }
                    if (!baseDir.exists()) {
                        action.accept(CheckResult.BASE_PATH_MISSING);
                    }
                }, onError);
    }
}
