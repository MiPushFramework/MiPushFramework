package com.xiaomi.helper;

import android.accessibilityservice.AccessibilityService;
import android.app.Service;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;

/**
 * Created by zts1993 on 2018/2/9.
 */
public class DetectionService extends AccessibilityService {

    final static String TAG = "DetectionService";

    static String foregroundPackageName;

    public static String getForegroundPackageName() {
        return foregroundPackageName;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY_COMPATIBILITY;
    }


    /**
     * 重载辅助功能事件回调函数，对窗口状态变化事件进行处理
     *
     * @param event
     */
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            foregroundPackageName = event.getPackageName().toString();

        }
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
    }


}