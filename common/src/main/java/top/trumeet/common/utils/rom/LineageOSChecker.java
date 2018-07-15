package top.trumeet.common.utils.rom;

public class LineageOSChecker implements RomChecker {
    @Override
    public boolean check() {
        try {
            System.out.println("Class: " + Class.forName("org.lineageos.platform.internal.LineageSystemServer"));
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }
}
