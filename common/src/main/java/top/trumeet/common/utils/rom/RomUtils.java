package top.trumeet.common.utils.rom;

import top.trumeet.common.utils.rom.miui.MiuiChecker;

/**
 * Created by Trumeet on 2018/4/22.
 */

public class RomUtils {
    public static boolean isMiui () {
        return new MiuiChecker().check();
    }
}
