package com.xiaomi.xmsf.push.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * XMPush sdk will register push twice (sometimes lots of) times when init.
 * @author Trumeet
 * @date 2017/9/6
 * {@link com.xiaomi.xmsf.push.service.XMPushService} will start lots of times.
 */

public class RemoveTremblingUtils {
    /**
     * Min request interval (ms) for single app
     */
    private static final short MIN_REQUEST_INTERVAL = 1000;

    private Map<String, Date> mLocalTimeMap;


    private static RemoveTremblingUtils instance = null;

    public static RemoveTremblingUtils getIntance() {
        if (instance == null) {
            synchronized (RemoveTremblingUtils.class) {
                if (instance == null) {
                    instance = new RemoveTremblingUtils();
                }
            }
        }

        return instance;
    }


    private RemoveTremblingUtils() {
        mLocalTimeMap = new ConcurrentHashMap<>(10);
    }


    /**
     * Check allow package register
     *
     * @param packageName Package name
     * @return Allow
     */
    private boolean shouldAllow(String packageName) {
        return !mLocalTimeMap.containsKey(packageName) || shouldAllow(mLocalTimeMap.get(packageName), new Date());
    }

    private boolean shouldAllow(Date inSet, Date now) {
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
     *
     * @param packageName Package name
     * @return Allow register status, allow: true
     */
    public boolean onCallRegister(String packageName) {
        if (!shouldAllow(packageName)) {
            return false;
        }
        mLocalTimeMap.put(packageName, new Date());
        removeOldValues();
        return true;
    }

    /**
     * Auto remove expired values in map to reduce memory
     */
    private void removeOldValues() {
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
