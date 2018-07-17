package com.xiaomi.helper;

import android.os.Build;

import com.xiaomi.helper.impl.ActivityAccessibilityImpl;
import com.xiaomi.helper.impl.ActivityUsageStatsImpl;
import com.xiaomi.helper.impl.FakeImpl;

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
