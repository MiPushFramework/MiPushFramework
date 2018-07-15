package top.trumeet.common.utils.rom.h2os;

import top.trumeet.common.utils.rom.RomChecker;

public class H2OSChecker implements RomChecker {
    @Override
    public boolean check() {
        try {
            // 可能是只有 H2OS 才有的类（
            Class opFeatures = Class.forName("com.oneplus.sdk.utils.OpFeatures");
            System.out.println(opFeatures);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }
}
