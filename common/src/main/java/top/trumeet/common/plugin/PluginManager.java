package top.trumeet.common.plugin;

import android.content.ComponentName;
import androidx.annotation.Nullable;

/**
 * 提供对于推送提供程序的检索功能
 */
public class PluginManager {
    public static final String ARG_COMPONENT = "moe.yuuta.mipush.plugin.ARG_COMPONENT";

    // TODO

    /**
     * 检索该推送提供程序的推送管理服务是否可用且已启用。只有已启用的服务才能使用 API
     * @param name 对方服务 ComponentName
     * @return 是否启用
     */
    public static boolean isEnabled (@Nullable ComponentName name) {
        // TODO
        return true;
    }

    /**
     * 检查该组件是否可用（配置正确）
     */
    public static boolean verifyComponent (ComponentName name) {
        // TODO
        return true;
    }
}
