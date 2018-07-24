package top.trumeet.common.ita;

import android.support.annotation.IntDef;
import android.support.annotation.RestrictTo;

public class AccessMode {
    public static final int USAGE_STATS = 0;
    public static final int ACCESSIBILITY = 1;

    @IntDef(value = {
            USAGE_STATS, ACCESSIBILITY
    })
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public @interface Mode {
    }
}
