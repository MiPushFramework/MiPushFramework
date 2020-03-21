package top.trumeet.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;

/**
 * Created by Trumeet on 2017/8/24.
 * Constants
 */

public final class Constants {
    private Constants () {
        throw new UnsupportedOperationException("What are U doing");
    }

    /**
     * Default app log tag
     */
    public static final String TAG = "MiPushFramework";
    public static final String TAG_PUSH = Constants.TAG + "-Push";
    public static final String TAG_MANAGER = Constants.TAG + "-Manager";
    public static final String TAG_CONDOM = Constants.TAG + "-Condom";

    public static final String WIZARD_SP_NAME = "wizard";
    public static final String KEY_SHOW_WIZARD = "show_wizard";

    public static final String ACTION_RECEIVE_MESSAGE = "com.xiaomi.mipush.RECEIVE_MESSAGE";
    public static final String ACTION_MESSAGE_ARRIVED = "com.xiaomi.mipush.MESSAGE_ARRIVED";
    public static final String ACTION_ERROR = "com.xiaomi.mipush.ERROR";

    /**
     * Enable push.
     * @see top.trumeet.common.push.PushController
     */
    public static final String KEY_ENABLE_PUSH = "enable_push";

    /**
     * XMPush APP id
     * @see top.trumeet.common.push.PushController
     */
    public static String APP_ID = "1000271";

    /**
     * XMPush APP key
     * @see top.trumeet.common.push.PushController
     */
    public static String APP_KEY = "420100086271";

    /**
     * Every page item count
     * @see top.trumeet.mipushframework.event.EventFragment
     */
    public static final int PAGE_SIZE = 20;

    /**
     * Package name extra when register push {@link com.xiaomi.xmsf.push.service.XMPushService}
     */
    public static final String EXTRA_MI_PUSH_PACKAGE = "mipush_app_package";

    /**
     * Message type extra when receive push
     */
    public static final String EXTRA_MESSAGE_TYPE = "message_type";

    /**
     * Register push result type. {@link Constants#EXTRA_MESSAGE_TYPE}
     */
    public static final int MESSAGE_TYPE_REGISTER_RESULT = 3;

    /**
     * Push result type. {@link Constants#EXTRA_MESSAGE_TYPE}
     */
    public static final int MESSAGE_TYPE_PUSH = 1;

    /**
     * Use in wizard, finish activity when user click NEXT,
     * not go next page.
     * TODO: I18n for this button. Use "Finish" label is better.
     */
    public static final String EXTRA_FINISH_ON_NEXT = "top.trumeet.xmsf.EXTRA_FINISH_ON_NEXT";

    /**
     * Application log file, use {@link Context#getCacheDir()} as
     * prefix.
     */
    public static final String LOG_FILE = "/file.log";

    public static final String AUTHORITY_FILE_PROVIDER = "top.trumeet.mipushframework.fileprovider";

    public static final String SERVICE_APP_NAME = "com.xiaomi.xmsf";

    public static final String MANAGER_APP_NAME = "top.trumeet.mipush";

    public static final int PUSH_SERVICE_VERSION_CODE = Integer.parseInt(BuildConfig.PUSH_VERSION_CODE);

    /**
     * 用于 Manager 控制 Service 的控制服务的名字
     * @see top.trumeet.common.push.PushController
     */
    public static final String CONTROLLER_SERVICE_NAME = "com.xiaomi.xmsf.push.control.ControlService";

    public static final String SHARE_LOG_COMPONENT_NAME =
            SERVICE_APP_NAME + ".ShareLogActivity";

    public static final String KEEPLIVE_COMPONENT_NAME =
            SERVICE_APP_NAME + ".EmptyActivity";

    public static final String REMOVE_DOZE_COMPONENT_NAME =
            SERVICE_APP_NAME + ".RemoveDozeActivity";

    public static class permissions {
        public static final String BIND = "top.trumeet.mipush.permissions.BIND";
        public static final String READ_SETTINGS = "top.trumeet.mipush.permissions.READ_SETTINGS";
        public static final String WRITE_SETTINGS = "top.trumeet.mipush.permissions.WRITE_SETTINGS";
        public static final String USE_PUSH_MANAGER_API = "moe.yutua.mipush.permissions.USE_PUSH_MANAGER_API";
    }
    public static class permissions_old {
        public static final String GET_VERSION = "top.trumeet.mipush.permissions.GET_VERSION";
    }

    @SuppressLint("SdCardPath")
    public static final String FAKE_CONFIGURATION_PATH = Build.VERSION.SDK_INT >= 24 ? "/data/user_de/0/top.trumeet.mipush/packages/" : "/data/data/top.trumeet.mipush/packages/";
    public static final String FAKE_CONFIGURATION_NAME_TEMPLATE = FAKE_CONFIGURATION_PATH + "%1$s.%2$s";
    public static final String FAKE_CONFIGURATION_GLOBAL = Constants.FAKE_CONFIGURATION_PATH + "ALL";
}
