package top.trumeet.common.ita;

import androidx.annotation.IntDef;
import androidx.annotation.RestrictTo;

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
