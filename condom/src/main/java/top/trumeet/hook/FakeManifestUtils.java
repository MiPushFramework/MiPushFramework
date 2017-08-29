package top.trumeet.hook;

import android.Manifest;
import android.content.pm.PackageInfo;
import android.content.pm.PermissionInfo;

/**
 * Created by Trumeet on 2017/8/29.
 * Util to pass XMPush sdk manifest check
 * @author Trumeet
 */

public class FakeManifestUtils {
    public static PackageInfo buildFakePackageInfo (PackageInfo info) {
        info.requestedPermissions = new String[]{Manifest.permission.VIBRATE,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.GET_TASKS,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                info.packageName + ".permission.MIPUSH_RECEIVE"};
        PermissionInfo permissionInfo = new PermissionInfo();
        permissionInfo.protectionLevel = PermissionInfo.PROTECTION_SIGNATURE;
        permissionInfo.name = info.packageName + ".permission.MIPUSH_RECEIVE";
        info.permissions = new PermissionInfo[]{permissionInfo};
        return info;
    }
}
