package top.trumeet.common;

import android.content.Context;

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
    public static final String TAG = "Xiaomi";

    public static final String TAG_CONDOM = Constants.TAG + "-Condom";

    public static final String WIZARD_SP_NAME = "wizard";
    public static final String KEY_SHOW_WIZARD = "show_wizard";

    /**
     * Check service is running, we will loop check for 20 times.
     * Every time will wait 5000 ms.
     * @see top.trumeet.mipushframework.wizard.CheckRunningStatusActivity
     */
    public static final int CHECK_RUNNING_TIMES = 4;

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
     */
    public static final String EXTRA_FINISH_ON_NEXT = "top.trumeet.xmsf.EXTRA_FINISH_ON_NEXT";

    /**
     * Application log file, use {@link Context#getCacheDir()} as
     * prefix.
     */
    public static final String LOG_FILE = "/file.log";

    public static final String AUTHORITY_FILE_PROVIDER = "top.trumeet.mipushframework.fileprovider";

    public static final String SERVICE_APP_NAME = "com.xiaomi.xmsf";

    public static final int PUSH_SERVICE_VERSION_CODE = 1;

    /**
     * 用于 Manager 控制 Service 的控制服务的名字
     * @see top.trumeet.common.push.PushController
     */
    public static final String CONTROLLER_SERVICE_NAME = "com.xiaomi.xmsf.push.control.ControlService";

    public static final String SHARE_LOG_COMPONENT_NAME =
            SERVICE_APP_NAME + ".ShareLogActivity";

    public static class permissions {
        private static String permission (String name) {
            return "top.trumeet.mipush.permissions." + name;
        }

        public static final String CHANGE_PUSH_ENABLE_SETTING =
                permission("CHANGE_PUSH_ENABLE_SETTING");

        public static final String GET_PUSH_ENABLE_SETTING =
                permission("GET_PUSH_ENABLE_SETTING");

        public static final String GET_VERSION =
                permission("GET_VERSION");

        public static final String CHECK_APP_OPS_STATUS =
                permission("CHECK_APP_OPS_STATUS");
    }
}
