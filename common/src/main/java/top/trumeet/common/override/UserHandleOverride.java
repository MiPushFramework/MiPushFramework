package top.trumeet.common.override;

import android.os.Build;
import android.os.UserHandle;

import static android.os.Build.VERSION_CODES.N;
import static android.os.UserHandle.getUserId;

public class UserHandleOverride {
    public static UserHandle getUserHandleForUid (int uid) {
        if (Build.VERSION.SDK_INT >= N) {
            // Use system api
            return UserHandle.getUserHandleForUid(uid);
        } else {
            // Use compat api
            return new UserHandle(getUserId(uid));
        }
    }
}
