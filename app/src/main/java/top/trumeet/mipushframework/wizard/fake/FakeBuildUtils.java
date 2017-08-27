package top.trumeet.mipushframework.wizard.fake;

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static top.trumeet.mipushframework.utils.ShellUtils.exec;

/**
 * Created by Trumeet on 2017/8/25.
 * Util to check and edit build.prop
 */

final class FakeBuildUtils {
    private static final Map<String, Class> MIUI_KEYS;

    static {
        MIUI_KEYS = new HashMap<>(1);

        // MIUI build.prop pivotal keys
        MIUI_KEYS.put("ro.miui.ui.version.name", String.class);
        MIUI_KEYS.put("ro.miui.ui.version.code", Integer.class);
        MIUI_KEYS.put("ro.miui.ui.version.name", String.class);
    }

    static boolean isMiuiBuild () {
        Set<String> keys = MIUI_KEYS.keySet();
        return hasProp(keys.toArray(new String[keys.size()]));
    }

    private static boolean hasProp (String... keys) {
        for (String key : keys) {
            if (TextUtils.isEmpty(getSystemProperty(key))) {
                return false;
            }
        }
        return true;
    }

    public static String getSystemProperty(String propName) {
        String line;
        BufferedReader input = null;
        try {
            java.lang.Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return line;
    }

    /**
     * Insert a line to build.prop
     * @param text line
     * @return success
     */
    private static boolean insert (String text) {
        String builder = "sed -i '" + "$a " +
                text +
                "' /system/build.prop";
        return exec(builder);
    }

    /**
     * Insert a key and value to build.prop
     * @param key Key
     * @param value Value
     * @return success
     */
    private static boolean insert (String key, String value) {
        if (hasProp(key)) {
            // Do not insert if already have
            return false;
        }
        return insert(key + "=" + value);
    }

    public static boolean insertMiui () {
        boolean result = mount("rw", "/system");
        result |= backup();
        result |= insert("# Fake MIUI build by XiaomiPushServiceFramework.");
        for (String s : MIUI_KEYS.keySet()) {
            String value;
            Class c = MIUI_KEYS.get(s);
            if (String.class.equals(c)) {
                value = "HELLO";
            } else if (Integer.class.equals(c)) {
                value = "00000";
            } else if (Boolean.class.equals(c)) {
                value = "false";
            } else {
                value = "HELLO";
            }
            result |= insert(s, value);
        }
        result |= fixPermission();
        result |= mount("ro", "/system");
        return result;
    }

    private static boolean fixPermission () {
        return exec("chmod 644 /system/build.prop");
    }

    private static boolean mount (String flag, String point) {
        StringBuilder builder = new StringBuilder();
        builder.append("mount -o ");
        builder.append(flag);
        builder.append(",remount,");
        builder.append(flag);
        builder.append(" ");
        builder.append(point);
        return exec(builder.toString());
    }

    private static boolean backup () {
        return exec("cp /system/build.prop \"/system/build.prop_" +
        new Date().toGMTString() + ".bak\"");
    }
}
