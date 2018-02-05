package top.trumeet.common.override;

import android.annotation.TargetApi;
import android.app.AppOpsManager;

import static android.os.Build.VERSION_CODES.N;

/**
 * Created by Trumeet on 2018/2/5.
 */

public class AppOpsManagerOverride {

    /** @hide Control whether an application is allowed to run in the background. */
    @TargetApi(N)
    public static final int OP_RUN_IN_BACKGROUND = AppOpsManager.OP_RUN_IN_BACKGROUND;

    /** @hide */
    public static final int OP_POST_NOTIFICATION = AppOpsManager.OP_POST_NOTIFICATION;

    public static int checkOpNoThrow(int op, int uid, String packageName,
                              AppOpsManager manager) {
        return manager.checkOpNoThrow(op, uid, packageName);
    }
}
