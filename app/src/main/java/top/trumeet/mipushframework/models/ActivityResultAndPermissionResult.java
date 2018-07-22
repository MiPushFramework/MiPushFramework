package top.trumeet.mipushframework.models;

import com.tbruyelle.rxpermissions2.Permission;

import rx_activity_result2.Result;

public class ActivityResultAndPermissionResult<Activity> {
    public final Result<Activity> activityResult;
    public final Permission permissionResult;

    public ActivityResultAndPermissionResult(Result<Activity> activityResult, Permission permissionResult) {
        this.activityResult = activityResult;
        this.permissionResult = permissionResult;
    }
}
