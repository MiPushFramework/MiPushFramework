package top.trumeet.mipushframework.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Trumeet on 2017/9/6.
 * XMPush sdk will register push twice (sometimes lots of) times when init.
 * {@link com.xiaomi.xmsf.push.service.XMPushService} will start lots of times.
 */

public class RemoveTremblingUtils {
    /**
     * Min request interval (ms) for single app
     */
    public static final short MIN_REQUEST_INTERVAL = 1000;

    private Map<String, Date> mLocalTimeMap;

    public RemoveTremblingUtils () {

    }

    private void prepareMap () {
        if (mLocalTimeMap == null)
            mLocalTimeMap = new HashMap<>(0);
    }

    /**
     * Check allow package register
     * @param packageName Package name
     * @return Allow
     */
    private boolean shouldAllow (String packageName) {
        prepareMap();
        return !mLocalTimeMap.containsKey(packageName) || shouldAllow(mLocalTimeMap.get(packageName), new Date());
    }

    private boolean shouldAllow (Date inSet, Date now) {
        Calendar calendarCurrent = Calendar.getInstance();
        calendarCurrent.setTime(now);
        Calendar calendarServer = Calendar.getInstance();
        calendarServer.setTime(inSet);
        long time1 = calendarCurrent.getTimeInMillis();
        long time2 = calendarServer.getTimeInMillis();

        // Calculate difference in milliseconds
        long diff = time1 - time2;
        return diff >= MIN_REQUEST_INTERVAL;
    }

    /**
     * Call this method when register.
     * @param packageName Package name
     * @return Allow register status, allow: true
     */
    public boolean onCallRegister (String packageName) {
        prepareMap();
        if (!shouldAllow(packageName))
            return false;
        mLocalTimeMap.put(packageName, new Date());
        removeOldValues();
        return true;
    }

    /**
     * Auto remove expired values in map to reduce memory
     */
    private void removeOldValues () {
        prepareMap();
        Date date = new Date();
        Set<String> keys = new HashSet<>(mLocalTimeMap.keySet());
        for (String key : keys) {
            if (shouldAllow(mLocalTimeMap.get(key),
                    date)) {
                // Remove it to reduce memory
                mLocalTimeMap.remove(key);
            }
        }
    }
}
