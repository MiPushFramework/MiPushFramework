package com.xiaomi.helper;

import android.accessibilityservice.AccessibilityService;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.accessibility.AccessibilityEvent;

import me.pqpo.librarylog4a.Log4a;

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
        return Service.START_STICKY_COMPATIBILITY; // 根据需要返回不同的语义值
    }


    /**
     * 重载辅助功能事件回调函数，对窗口状态变化事件进行处理
     *
     * @param event
     */
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            /*
             * 如果 与 DetectionService 相同进程，直接比较 foregroundPackageName 的值即可
             * 如果在不同进程，可以利用 Intent 或 bind service 进行通信
             */
            foregroundPackageName = event.getPackageName().toString();

            /*
             * 基于以下还可以做很多事情，比如判断当前界面是否是 Activity，是否系统应用等，
             * 与主题无关就不再展开。
             */
            ComponentName cName = new ComponentName(event.getPackageName().toString(),
                    event.getClassName().toString());
        }
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
    }


    public static boolean isAccessibilitySettingsOn(Context context) {
        int accessibilityEnabled = 0;
        try {
            accessibilityEnabled = Settings.Secure.getInt(context.getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            Log4a.e(TAG, e.getMessage(), e);
        }

        if (accessibilityEnabled == 1) {
            String services = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (services != null) {
                return services.toLowerCase().contains(context.getPackageName().toLowerCase());
            }
        }

        return false;
    }

}