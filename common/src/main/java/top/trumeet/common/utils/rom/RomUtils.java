package top.trumeet.common.utils.rom;

import android.support.annotation.IntDef;
import android.support.annotation.RestrictTo;

import top.trumeet.common.utils.rom.miui.MiuiChecker;

/**
 * Created by Trumeet on 2018/4/22.
 */

public class RomUtils {
    // AOSP 及类原生
    public static final int ROM_AOSP = 0;
    // MIUI 官方（国际/中国版）、官改
    public static final int ROM_MIUI = 1;
    // 氢 OS
    public static final int ROM_H2OS = 2;

    @IntDef(value = {
            ROM_AOSP,
            ROM_MIUI,
            ROM_H2OS
    })
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    private @interface RomType {
    }

    /**
     * 判断 ROM
     * @return
     */
    @RomType
    public static int getOs() {
        // 先看看是不是厂商 ROM。因为类原生都类似。
        if (new MiuiChecker().check()) {
            return ROM_MIUI;
        }

        // 权当是 AOSP 了
        return ROM_AOSP;
    }

    /**
     * @deprecated {@link #getOs()}
     */
    @Deprecated
    public static boolean isMiui () {
        return new MiuiChecker().check();
    }
}
