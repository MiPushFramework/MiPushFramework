package top.trumeet.mipushframework.control;

import android.content.Context;

import com.xiaomi.xmsf.R;

import static top.trumeet.common.utils.Utils.getString;
import static top.trumeet.mipushframework.control.OnConnectStatusChangedListener.FAIL_REASON_LOW_VERSION;
import static top.trumeet.mipushframework.control.OnConnectStatusChangedListener.FAIL_REASON_NOT_INSTALLED;
import static top.trumeet.mipushframework.control.OnConnectStatusChangedListener.FAIL_REASON_UNKNOWN;

/**
 * Created by Trumeet on 2017/12/30.
 */

public class ConnectFailUtils {
    private static final String SERVICE_APK_URL =
            "https://github.com/Trumeet/MiPushFramework/releases/download/" + com.xiaomi.xmsf.BuildConfig.VERSION_NAME +
                    "/xmsf_service.apk";

    public static CharSequence getTitle (Context context,
                                         @OnConnectStatusChangedListener.FailReason int reason) {
        switch (reason) {
            case FAIL_REASON_UNKNOWN:
                return getString(R.string.connect_fail_title_unknown, context);
            case FAIL_REASON_LOW_VERSION:
                return getString(R.string.connect_fail_title_low_version, context);
            case FAIL_REASON_NOT_INSTALLED:
                return getString(R.string.connect_fail_title_not_installed, context);
            default:
                return "";
        }
    }

    public static CharSequence getSummary (Context context,
                                           @OnConnectStatusChangedListener.FailReason int reason,
                                           int serviceVersionCode) {
        switch (reason) {
            case FAIL_REASON_UNKNOWN:
                return getString(R.string.connect_fail_text_unknown, context,
                        getString(top.trumeet.common.R.string.push_service_name, context));
            case FAIL_REASON_NOT_INSTALLED:
                return getString(R.string.connect_fail_text_not_installed, context,
                        SERVICE_APK_URL);
            case FAIL_REASON_LOW_VERSION:
                return getString(R.string.connect_fail_text_low_version, context,
                        String.valueOf(serviceVersionCode),
                        SERVICE_APK_URL);
            default:
                return "";
        }
    }
}
