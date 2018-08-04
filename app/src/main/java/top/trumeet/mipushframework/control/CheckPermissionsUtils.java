package top.trumeet.mipushframework.control;

import android.content.ComponentName;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.internal.functions.Functions;
import rx_activity_result2.RxActivityResult;
import top.trumeet.common.Constants;
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
        return Observable.zip(RxActivityResult.on(context)
                        .startIntent(new Intent()
                                .setComponent(new ComponentName(Constants.SERVICE_APP_NAME,
                                        Constants.REMOVE_DOZE_COMPONENT_NAME)))
                , new RxPermissions(context)
                        .requestEachCombined(Constants.permissions.WRITE_SETTINGS),
                ActivityResultAndPermissionResult::new)
                .subscribe(onNext, onError);
    }
}
