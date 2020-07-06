package top.trumeet.common.utils.rom;

import androidx.annotation.IntDef;
import androidx.annotation.RestrictTo;
import top.trumeet.common.utils.rom.h2os.H2OSChecker;
import top.trumeet.common.utils.rom.miui.MiuiChecker;

/**
 * Created by Trumeet on 2018/4/22.
 */

public class RomUtils {
    // AOSP 及类原生（除 CM / Los）
    public static final int ROM_AOSP = 0;
    // MIUI 官方（国际/中国版）、官改
    public static final int ROM_MIUI = 1;
    // 氢 OS
    public static final int ROM_H2OS = 2;
    // CyanogenMod
    public static final int ROM_CYANOGEN_MOD = 3;
    // LineageOS
    public static final int ROM_LINEAGE_OS = 4;
    // 未知（没有这个 case）
    public static final int ROM_UNKNOWN = -1;

    @IntDef(value = {
            ROM_UNKNOWN,
            ROM_AOSP,
            ROM_MIUI,
            ROM_H2OS,
            ROM_CYANOGEN_MOD,
            ROM_LINEAGE_OS
    })
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    private @interface RomType {
    }

    /**
     * 判断 ROM
     */
    @RomType
    public static int getOs() {
        // 先看看是不是厂商 ROM。因为类原生都类似。
        if (new MiuiChecker().check()) {
            return ROM_MIUI;
        }
        if (new H2OSChecker().check()) {
            return ROM_H2OS;
        }
        if (new CyanogenModChecker().check()) {
            return ROM_CYANOGEN_MOD;
        }
        if (new LineageOSChecker().check()) {
            return ROM_LINEAGE_OS;
        }
        return ROM_UNKNOWN;
    }

    /**
     * @deprecated {@link #getOs()}
     */
    @Deprecated
    public static boolean isMiui () {
        return new MiuiChecker().check();
    }
}
