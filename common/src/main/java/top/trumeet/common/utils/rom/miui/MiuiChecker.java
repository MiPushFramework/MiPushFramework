package top.trumeet.common.utils.rom.miui;

import android.app.Application;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import top.trumeet.common.utils.rom.RomChecker;

/**
 * Created by Trumeet on 2018/4/22.
 */

public class MiuiChecker implements RomChecker {
    private static final String TAG = "MiuiChecker";

    private boolean m2a() {
        try {
            if (C0032c.isMiuiSystem() || C0033d.load(C0032c.getApkPath(null, "com.miui.core", "miui"), null, C0032c.getLibPath(null, "com.miui.core")
                    , Application.class.getClassLoader())) {
                return true;
            }
            // No sdk
            return false;
        } catch (Throwable th) {
            Log.e(TAG, th.getMessage());
            return false;
        }
    }

    private boolean m3b() {
        try {
            HashMap hashMap = new HashMap();
            int intValue = (Integer) C0030a.m37g().getMethod("initialize", new Class[]{Application.class, Map.class}).invoke(null, new Object[]{this, hashMap});
            if (intValue == 0) {
                return true;
            }
            Log.d(TAG, "initialize" + intValue);
            return false;
        } catch (Throwable th) {
            Log.e("MIUIChecker", th.getMessage());
            return false;
        }
    }

    private boolean m4c() {
        try {
            HashMap hashMap = new HashMap();
            int intValue = (Integer) C0030a.m37g().getMethod("start", new Class[]{Map.class}).invoke(null, new Object[]{hashMap});
            if (intValue == 1) {
                // Low sdk version
                return false;
            } else if (intValue == 0) {
                return true;
            } else {
                Log.e(TAG, "start" + intValue);
                return false;
            }
        } catch (Throwable th) {
            Log.e("MIUIChecker", th.getMessage());
            return false;
        }
    }

    @Override
    public boolean check() {
        return m2a() && m3b() && m4c();
    }
}
