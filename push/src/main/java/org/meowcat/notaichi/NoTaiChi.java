package org.meowcat.notaichi;

import android.app.Application;
import android.content.Context;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.util.Base64;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

import dalvik.system.DexFile;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class NoTaiChi {

    public static void checkTC(final XC_MethodHook.MethodHookParam param) {
        final String message = new String(Base64.decode("RG8gTk9UIHVzZSBUYWlDaGkgYW55d2F5XG7or7fkuI3opoHkvb/nlKjlpKrmnoHmiJbml6DmnoE=".getBytes(StandardCharsets.UTF_8), Base64.DEFAULT));
        new Thread(() -> {
            ClassLoader classLoader = XposedBridge.class.getClassLoader();
            Object[] dexElements = (Object[]) XposedHelpers.getObjectField(XposedHelpers.getObjectField(classLoader, "pathList"), "dexElements");
            for (Object entry : dexElements) {
                Enumeration<String> entries = ((DexFile) XposedHelpers.getObjectField(entry, "dexFile")).entries();
                while (entries.hasMoreElements()) {
                    String className = entries.nextElement();
                    if (className.matches(".+?(epic|weishu).+")) {
                        try {
                            if (param.args[0] instanceof Application) {
                                Toast.makeText((Context) param.args[0], message, Toast.LENGTH_LONG).show();
                            }
                            XposedBridge.log(message);
                            Os.kill(android.os.Process.myPid(), OsConstants.SIGKILL);
                        } catch (ErrnoException ignored) {
                        }
                    }
                }
            }
        }).start();
    }
}
