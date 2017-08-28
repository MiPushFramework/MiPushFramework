package top.trumeet.mipushframework.wizard.fake;

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
}
