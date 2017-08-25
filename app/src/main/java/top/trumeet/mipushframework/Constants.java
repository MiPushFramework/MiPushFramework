package top.trumeet.mipushframework;

/**
 * Created by Trumeet on 2017/8/24.
 * Constants
 */

public final class Constants {
    public static final String WIZARD_SP_NAME = "wizard";
    public static final String KEY_SHOW_WIZARD = "show_wizard";

    /**
     * Check service is running, we will loop check for 20 times.
     * Every time will wait 1000 ms.
     * @see top.trumeet.mipushframework.wizard.CheckRunningStatusActivity
     */
    public static final int CHECK_RUNNING_TIMES = 20;

    public static final String ACTION_RECEIVE_MESSAGE = "com.xiaomi.mipush.RECEIVE_MESSAGE";
}
