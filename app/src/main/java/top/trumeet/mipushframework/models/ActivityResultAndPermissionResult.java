package top.trumeet.mipushframework.models;

import com.tbruyelle.rxpermissions2.Permission;

public class ActivityResultAndPermissionResult {
    public final boolean removeDozeResult;
    public final Permission permissionResult;

    public ActivityResultAndPermissionResult(boolean removeDozeResult, Permission permissionResult) {
        this.removeDozeResult = removeDozeResult;
        this.permissionResult = permissionResult;
    }
}
