package com.xiaomi.xmsf.push.control;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Process;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.oasisfeng.condom.CondomContext;
import com.taobao.android.dexposed.DexposedBridge;
import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.xiaomi.channel.commonutils.android.MIUIUtils;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.xiaomi.push.service.PushServiceMain;
import com.xiaomi.xmsf.push.service.receivers.BootReceiver;

import java.util.ArrayList;
import java.util.List;

import me.pqpo.librarylog4a.Log4a;
import top.trumeet.common.Constants;
import top.trumeet.common.utils.ServiceRunningChecker;

import static top.trumeet.common.Constants.APP_ID;
import static top.trumeet.common.Constants.APP_KEY;
import static top.trumeet.common.Constants.TAG_CONDOM;

/**
 * Created by Trumeet on 2017/8/25.
 * @author Trumeet
 */

@SuppressLint("WrongConstant")
public class PushControllerUtils {
    private static final String TAG = PushControllerUtils.class.getSimpleName();
    
    private static SharedPreferences getPrefs (Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }

    /**
     * Get is user enable push in settings.
     * @see Constants#KEY_ENABLE_PUSH
     * @param context Context param
     * @return is enable
     */
    public static boolean isPrefsEnable (Context context) {
        return getPrefs(context)
                .getBoolean(Constants.KEY_ENABLE_PUSH, true);
    }

    /**
     * Set push enable
     * @see Constants#KEY_ENABLE_PUSH
     * @param value is enable
     * @param context Context param
     */
    public static void setPrefsEnable (boolean value, Context context) {
        getPrefs(context)
                .edit()
                .putBoolean(Constants.KEY_ENABLE_PUSH, value)
                .apply();
    }

    /**
     * Check is in main app process
     * @param context Context param
     * @return is in main process
     */
    public static boolean isAppMainProc(Context context) {
        for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : ((ActivityManager)
                context.getSystemService(Context.ACTIVITY_SERVICE))
                .getRunningAppProcesses()) {
            if (runningAppProcessInfo.pid == Process.myPid() && runningAppProcessInfo.processName.equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Set XMPush sdk enable
     * @param enable enable
     * @param context context param
     */
    public static void setServiceEnable (boolean enable, Context context) {
        if (enable && isAppMainProc(context)) {
            Log4a.d(TAG, "Starting...");
            MiPushClient.registerPush(wrapContext(context), APP_ID, APP_KEY);
        } else {
            Log4a.d(TAG, "Stopping...");
            MiPushClient.unregisterPush(wrapContext(context));
            // Force stop and disable services.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
                scheduler.cancelAll();
            }
            context.stopService(new Intent(context, PushServiceMain.class));
        }
    }

    /**
     * Set SP and XMPush enable
     * @param enable is enable
     * @param context Context param
     */
    public static void setAllEnable (boolean enable, Context context) {
        setPrefsEnable(enable, context);
        setServiceEnable(enable, context);
        setBootReceiverEnable(enable, context);
    }

    /**
     * Check SP and service is enable
     * @param context Context param
     * @return is all enable
     */
    public static boolean isAllEnable (Context context) {
        return isPrefsEnable(context) && isServiceRunning (context)
                && isBootReceiverEnable(context);
    }

    public static boolean isBootReceiverEnable (Context context) {
        return context.getPackageManager()
                .getComponentEnabledSetting(new ComponentName(context,
                        BootReceiver.class)) ==
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
    }

    /**
     * Check push service is running
     * @see com.xiaomi.push.service.XMPushService
     * @param context context param
     * @return is running
     */
    public static boolean isServiceRunning (Context context) {
        return ServiceRunningChecker.isServiceRunning(context, PushServiceMain.class);
    }

    @SuppressLint("WrongConstant")
    private static void setBootReceiverEnable (boolean enable, Context context) {
        context.getPackageManager()
                .setComponentEnabledSetting(new ComponentName(context,
                                BootReceiver.class),
                        enable ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);
    }

    public static Context wrapContext (final Context context) {
        return CondomContext.wrap(context, TAG_CONDOM, XMOutbound.create(context,
                TAG_CONDOM));
    }

    /**
     * Hook Push SDK
     */
    @NonNull
    public static XC_MethodHook.Unhook[] hookSdk () {
        List<XC_MethodHook.Unhook> unhooks = new ArrayList<>(0);
        if (Build.VERSION.SDK_INT >= 26) {
            // TODO: ArtHook does not support
            return unhooks.toArray(new XC_MethodHook.Unhook[unhooks.size()]);
        }
        try {
            unhooks.add(DexposedBridge.findAndHookMethod(Class.forName("com.xiaomi.channel.commonutils.android.MIUIUtils"),
                    "getIsMIUI", new XC_MethodReplacement() {
                        @Override
                        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                            return MIUIUtils.IS_MIUI;
                        }
                    }));
        } catch (Throwable e) {
            Log4a.e(TAG, "Hook", e);
        }
        return unhooks.toArray(new XC_MethodHook.Unhook[unhooks.size()]);
    }
}
