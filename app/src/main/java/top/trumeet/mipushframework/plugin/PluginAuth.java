package top.trumeet.mipushframework.plugin;

import android.content.ComponentName;
import androidx.annotation.Nullable;
import top.trumeet.common.plugin.PluginManager;

/**
 * 对于推送提供程序的鉴权
 */
public class PluginAuth {
    public static void enforceEnabled (@Nullable ComponentName name) throws SecurityException {
        if (!PluginManager.isEnabled(name))
            throw new SecurityException("Component " + name.toString() + " is not enabled by the user");
    }
}
