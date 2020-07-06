package top.trumeet.mipushframework.control;

import android.content.ComponentName;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import com.tbruyelle.rxpermissions2.RxPermissions;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.internal.functions.Functions;
import rx_activity_result2.RxActivityResult;
import top.trumeet.common.Constants;
import top.trumeet.common.push.PushServiceAccessibility;
import top.trumeet.mipushframework.models.ActivityResultAndPermissionResult;

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
            } else {
                // Let it empty to start activity
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
        REMOVE_DOZE_NEEDED
    }

    public static Disposable checkAndRun (Consumer<CheckResult> action, Consumer<Throwable> onError, FragmentActivity context) {
        return CheckPermissionsUtils.checkPermissionsAndStartAsync(context,
                (result) -> {
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
                }, onError);
    }
}
