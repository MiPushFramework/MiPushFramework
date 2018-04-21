package com.xiaomi.helper;

import com.xiaomi.helper.impl.ActivityAccessibilityImpl;
import com.xiaomi.helper.impl.ActivityUsageStatsImpl;

/**
 * Created by zts1993 on 2018/2/18.
 */

public class TopActivityFactory {

    public static ITopActivity newInstance(int accessMode) {
        if (accessMode == AccessMode.USAGE_STATS) {
            return new ActivityUsageStatsImpl();
        } else if (accessMode == AccessMode.ACCESSIBILITY) {
            return new ActivityAccessibilityImpl();
        } else {
            return null; //should never be here

        }

    }
}
