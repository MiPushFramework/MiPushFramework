package top.trumeet.mipushframework.control;

import android.content.Context;

import top.trumeet.common.utils.Utils;
import top.trumeet.mipush.BuildConfig;
import top.trumeet.mipush.R;

import static top.trumeet.common.utils.Utils.getString;
import static top.trumeet.mipushframework.control.OnConnectStatusChangedListener.FAIL_REASON_LOW_VERSION;
import static top.trumeet.mipushframework.control.OnConnectStatusChangedListener.FAIL_REASON_MIUI;
import static top.trumeet.mipushframework.control.OnConnectStatusChangedListener.FAIL_REASON_NOT_INSTALLED;
import static top.trumeet.mipushframework.control.OnConnectStatusChangedListener.FAIL_REASON_SECURITY_EXCEPTION;
import static top.trumeet.mipushframework.control.OnConnectStatusChangedListener.FAIL_REASON_UNKNOWN;

/**
 * @author Trumeet
 * @date 2017/12/30
 */

public class ConnectFailUtils {
    private static final String MANAGER_APK_URL =
            "https://github.com/Trumeet/MiPushFramework/releases/download/" + BuildConfig.GIT_TAG +
                    "/manager.apk";

    private static final String SERVICE_APK_URL =
            "https://github.com/Trumeet/MiPushFramework/releases/download/" + BuildConfig.GIT_TAG +
                    "/xmsf_service.apk";

    private static final String SERVICE_PLATFORM_APK_URL =
            "https://github.com/Trumeet/MiPushFramework/releases/download/" + BuildConfig.GIT_TAG +
                    "/xmsf_service_platform.apk";

    public static CharSequence getTitle(Context context,
                                        @OnConnectStatusChangedListener.FailReason int reason) {
        switch (reason) {
            case FAIL_REASON_UNKNOWN:
            case FAIL_REASON_SECURITY_EXCEPTION:
                return getString(R.string.connect_fail_title_unknown, context);
            case FAIL_REASON_LOW_VERSION:
                return getString(R.string.connect_fail_title_low_version, context);
            case FAIL_REASON_NOT_INSTALLED:
                return getString(R.string.connect_fail_title_not_installed, context);
            case FAIL_REASON_MIUI:
                return getString(R.string.connect_fail_title_rom, context);
            default:
                return "";
        }
    }

    public static CharSequence getSummary(Context context,
                                          @OnConnectStatusChangedListener.FailReason int reason,
                                          int serviceVersionCode) {
        switch (reason) {
            case FAIL_REASON_SECURITY_EXCEPTION:
                return Utils.toHtml(context.getString(R.string.connect_fail_test_se, MANAGER_APK_URL));
            case FAIL_REASON_UNKNOWN:
                return getString(R.string.connect_fail_text_unknown, context,
                        getString(top.trumeet.common.R.string.push_service_name, context));
            case FAIL_REASON_NOT_INSTALLED:
                String original = context.getString(R.string.connect_fail_text_not_installed,
                        SERVICE_APK_URL);
//                if (PlatformUtils.isPlatformModeSupported()) {
//                    String platformModeNotice = context.getString(R.string.platform_not_installed,
//                            SERVICE_PLATFORM_APK_URL);
//                    original += platformModeNotice;
//                }
                return Utils.toHtml(original);
            case FAIL_REASON_LOW_VERSION:
                return getString(R.string.connect_fail_text_low_version, context,
                        String.valueOf(serviceVersionCode),
                        SERVICE_APK_URL);
            case FAIL_REASON_MIUI:
                return getString(R.string.connect_fail_text_is_miui, context);
            default:
                return "";
        }
    }
}
