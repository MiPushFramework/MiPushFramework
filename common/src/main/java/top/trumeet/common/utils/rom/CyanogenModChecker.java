package top.trumeet.common.utils.rom;

public class CyanogenModChecker implements RomChecker {
    @Override
    public boolean check() {
        try {
            System.out.println("Class: " + Class.forName("org.cyanogenmod.platform.internal.CMSystemServer"));
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }
}
