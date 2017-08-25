package top.trumeet.mipushframework.wizard.support;

import android.support.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Created by Trumeet on 2017/8/24.
 * @author Trumeet
 */

public class SupportStatus {
    @IntDef({Status.OK, Status.FAIL, Status.FAIL_CAN_FIX})
    @Retention(SOURCE)
    @Target({ElementType.PARAMETER, ElementType.TYPE,
    ElementType.FIELD, ElementType.METHOD})
    public @interface Status {
        int OK = 0;
        int FAIL_CAN_FIX = 1;
        int FAIL = 2;
    }

    public final String pkgName;
    @Status
    public final int status;

    public SupportStatus(String pkgName, int status) {
        this.pkgName = pkgName;
        this.status = status;
    }

    @Override
    public String toString() {
        return "SupportStatus{" +
                "pkgName='" + pkgName + '\'' +
                ", status=" + status +
                '}';
    }
}
