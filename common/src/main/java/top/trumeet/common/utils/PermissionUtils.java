package top.trumeet.common.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Trumeet on 2018/4/15.
 */

public class PermissionUtils {
    private static final int RC_REQUEST = 1000;

    public static boolean hasPermission (String permName, Context context) {
        return ContextCompat.checkSelfPermission(context, permName)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasPermission (String permName) {
        return hasPermission(permName, Utils.getApplication());
    }

    public static void requestPermissions (Activity activity, String[] perm) {
        ActivityCompat.requestPermissions(activity, perm, RC_REQUEST);
    }

    public static boolean handle (Activity activity, int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == RC_REQUEST && activity instanceof PermissionGrantListener &&
                permissions != null) {
            for (int i = 0; i < permissions.length ; i ++) {
                ((PermissionGrantListener) activity).onResult(grantResults[i] == PackageManager.PERMISSION_GRANTED,
                        ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions[i]),
                        permissions[i]);
            }
            return true;
        }
        return false;
    }

    public static void requestPermissionsIfNeeded (Activity activity, String[] perm) {
        if (perm == null)
            return;
        List<String> needRequest = new ArrayList<>(perm.length);
        for (String permName : perm) {
            if (hasPermission(permName, activity)) {
                if (activity instanceof PermissionGrantListener) {
                    runCallback((PermissionGrantListener) activity,
                            true, false, permName);
                }
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                        permName)) {
                    if (activity instanceof PermissionGrantListener) {
                        runCallback((PermissionGrantListener) activity,
                                false, true, permName);
                    }
                } else {
                    needRequest.add(permName);
                }
            }
        }
        if (needRequest.size() > 0)
            requestPermissions(activity, needRequest.toArray(new String[needRequest.size()]));
    }

    private static void runCallback (final PermissionGrantListener listener, final boolean granted
            , final boolean blocked, final String permName) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                listener.onResult(granted, blocked, permName);
            }
        });
    }

    public interface PermissionGrantListener {
        void onResult (boolean granted, boolean blocked, String permName);
    }

    public static String getName (String permName) {
        try {
            return Utils.getPackageManager().getPermissionInfo(permName,
                    0).name;
        } catch (PackageManager.NameNotFoundException ignored) {
            return null;
        }
    }
}
