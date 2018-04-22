package top.trumeet.mipushframework.control;

import android.content.Intent;
import android.content.ServiceConnection;
import android.support.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Trumeet on 2017/12/30.
 */

public interface OnConnectStatusChangedListener {
    @Retention(RetentionPolicy.CLASS)
    @Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE, ElementType.METHOD,
    ElementType.FIELD, ElementType.PARAMETER})
    @IntDef({CONNECTED, DISCONNECTED})
    @interface Status {
    }

    @Retention(RetentionPolicy.CLASS)
    @Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE, ElementType.METHOD,
            ElementType.FIELD, ElementType.PARAMETER})
    @IntDef({FAIL_REASON_NOT_INSTALLED, FAIL_REASON_UNKNOWN,
    FAIL_REASON_LOW_VERSION, FAIL_REASON_MIUI})
    @interface FailReason {
    }


    /**
     * Push service not installed
     */
    int FAIL_REASON_NOT_INSTALLED = -1;

    /**
     * 未知原因，如 {@link android.content.Context#bindService(Intent, ServiceConnection, int)} 返回 false
     */
    int FAIL_REASON_UNKNOWN = 0;

    /**
     * 版本太低
     */
    int FAIL_REASON_LOW_VERSION = 1;

    /**
     * MIUI 用户
     */
    int FAIL_REASON_MIUI = 2;

    int CONNECTED = 0;
    int DISCONNECTED = 1;

    void onChange (@Status int newStatus);
}
