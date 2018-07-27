package top.trumeet.common.ita;

import android.os.Build;

import top.trumeet.common.ita.impl.ActivityAccessibilityImpl;
import top.trumeet.common.ita.impl.ActivityUsageStatsImpl;
import top.trumeet.common.ita.impl.FakeImpl;

/**
 * Created by zts1993 on 2018/2/18.
 */

public class TopActivityFactory {

    public static ITopActivity newInstance(int accessMode) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return new ActivityAccessibilityImpl();
        }

        if (accessMode == AccessMode.ACCESSIBILITY) {
            return new ActivityAccessibilityImpl();
        }

        if (accessMode == AccessMode.USAGE_STATS) {
            return new ActivityUsageStatsImpl();
        }

        return new FakeImpl(); //should never be here


    }
}
