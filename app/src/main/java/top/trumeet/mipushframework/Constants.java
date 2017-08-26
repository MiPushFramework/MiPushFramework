package top.trumeet.mipushframework;

/**
 * Created by Trumeet on 2017/8/24.
 * Constants
 */

public final class Constants {
    /**
     * Default app log tag
     */
    public static final String TAG = "Xiaomi";

    public static final String WIZARD_SP_NAME = "wizard";
    public static final String KEY_SHOW_WIZARD = "show_wizard";

    /**
     * Check service is running, we will loop check for 20 times.
     * Every time will wait 1000 ms.
     * @see top.trumeet.mipushframework.wizard.CheckRunningStatusActivity
     */
    public static final int CHECK_RUNNING_TIMES = 20;

    public static final String ACTION_RECEIVE_MESSAGE = "com.xiaomi.mipush.RECEIVE_MESSAGE";

    /**
     * Enable push.
     * @see top.trumeet.mipushframework.push.PushController
     */
    public static final String KEY_ENABLE_PUSH = "enable_push";

    /**
     * XMPush APP id
     * @see top.trumeet.mipushframework.push.PushController
     */
    public static String APP_ID = "1000271";

    /**
     * XMPush APP key
     * @see top.trumeet.mipushframework.push.PushController
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
}
