package com.xiaomi.push.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.elvishew.xlog.Logger;
import com.elvishew.xlog.XLog;
import com.oasisfeng.condom.CondomContext;
import com.xiaomi.smack.packet.Message;
import com.xiaomi.xmpush.thrift.ActionType;
import com.xiaomi.xmpush.thrift.PushMetaInfo;
import com.xiaomi.xmsf.BuildConfig;
import com.xiaomi.xmsf.R;
import com.xiaomi.xmsf.push.control.XMOutbound;

import org.apache.thrift.TBase;

import java.util.Timer;
import java.util.TimerTask;

import static com.xiaomi.xmsf.BuildConfig.DEBUG;
import static top.trumeet.common.Constants.TAG_CONDOM;

/**
 * @author Trumeet
 * @date 2018/1/19
 * <p>
 * PayLoad 主要在 {@link com.xiaomi.mipush.sdk.PushServiceClient#sendMessage(TBase, ActionType, PushMetaInfo)} 等重载方法中处理。
 * 具体转换： {@link com.xiaomi.xmpush.thrift.XmPushThriftSerializeUtils#convertByteArrayToThriftObject(TBase, byte[])} 中。
 * <p>
 * 0. 万恶之源
 * 主要 intent 处理是在 {@link XMPushService#handleIntent(Intent)}
 * <p>
 * 1. 向服务器发送
 * <strong>向服务器</strong>发送消息是 {@link XMPushService#sendMessage(Intent)}（解析 Intent -> Package）
 * 对应服务启动方：{@link com.xiaomi.push.service.ServiceClient#sendMessage(Message, boolean)}
 * Action: {@link PushConstants#ACTION_SEND_MESSAGE}
 * Extras: {@link PushConstants#MIPUSH_EXTRA_PAYLOAD}, {@link PushConstants#MIPUSH_EXTRA_MESSAGE_CACHE}, {@link PushConstants#MIPUSH_EXTRA_APP_PACKAGE}
 * <p>
 * {@link XMPushService#sendMessage(String, byte[], boolean)}（解析 payload (byte[]）-> 未知）
 * 对应启动方： {@link com.xiaomi.mipush.sdk.PushServiceClient#sendMessage(TBase, ActionType, PushMetaInfo)}
 * Action: {@link PushConstants#MIPUSH_ACTION_SEND_MESSAGE}
 * <p>
 * {@link XMPushService#sendMessages(Intent)}（和第一个一样，批量）
 * 对应启动方： {@link com.xiaomi.push.service.ServiceClient#batchSendMessage(Message[], boolean)}
 * Action: {@link PushConstants#ACTION_BATCH_SEND_MESSAGE}
 * <p>
 * 由此推测，XMPushService 具有<strong>向服务器</strong>发送消息的功能（三种途径），其控制方是 {@link com.xiaomi.push.service.ServiceClient} 和
 * {@link com.xiaomi.mipush.sdk.PushServiceClient}。
 * <p>
 * 看具体的 Job（{@link com.xiaomi.push.service.SendMessageJob}， {@link com.xiaomi.push.service.BatchSendMessageJob}），
 * 都是 {@link XMPushService} 收到 Intent 之后，启动 job，然后 job 回掉 XMPushService 进行 send packet，然后应该是发送给服务器了..
 * 所以这里的发送消息是通过 {@link XMPushService} 向服务器发送 Packet / Blob。
 * <p>
 * 2. 本地发送
 * 根据发送消息中出错的 Stacktrace，我们可以轻松找出发送消息的下游处理。
 * 首先，在 {@link XMPushService#connectBySlim} 连接时，注册了包监听器 {@link XMPushService#mPacketListener}。
 * 它在监听到 Blob 和 Packet 后启动 {@link BlobReceiveJob} 和 {@link PacketReceiveJob}。
 * 这两个 Job 都将数据交给 {@link PacketSync} 处理。但两者最后都经过检测 / 处理，将数据交给 {@link ClientEventDispatcher#notifyPacketArrival} 处理。
 * <p>
 * 3. 其他
 * 还有其他处理也在这里，比如注册 App（ {@link PushConstants#MIPUSH_ACTION_REGISTER_APP}）等，暂未支持。
 * <p>
 * 监听解决方案：
 * 所有包都会经过 {@link com.xiaomi.smack.PacketListener}，官方有一个处理器（C00621），持有在非静态 Field mPacketListener 中。
 * 所以我们只需创造一个自己的 {@link ClientEventDispatcher}，重写相关方法，即可完成处理。
 * 这种方式相比自己反射修改 {@link com.xiaomi.smack.PacketListener}，更优雅（无需反射，同时小米为我们提供了 {@link XMPushService#createClientEventDispatcher()} 方法），
 * 同时还能通过官方提供的处理逻辑（{@link PacketSync}），直接处理消息通知包。
 */

public class PushServiceMain extends XMPushService {
    private static final String TAG = "PushService";
    private Logger logger = XLog.tag(TAG).build();

    public static final String CHANNEL_STATUS = "status";
    public static final int NOTIFICATION_ALIVE_ID = 1;

    private Timer mStatTimer;
    private long startTime;
    private int nextUploadDuringMinutes = 20; // First 20, next 40, then 80... no longer than 6 hours (3600)
    private boolean skipFirstStartTimerTask = true; // Skip first run

    private void scheduleNextUpload () {
        if (mStatTimer != null) mStatTimer.cancel();
        skipFirstStartTimerTask = true;
        mStatTimer = null;
        mStatTimer = new Timer();
        mStatTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (skipFirstStartTimerTask) {
                    skipFirstStartTimerTask = false;
                    return;
                }
                nextUploadDuringMinutes = nextUploadDuringMinutes * 2;
                if (nextUploadDuringMinutes > 3600) {
                    nextUploadDuringMinutes = 3600;
                }
                // Report and log stable users
                long minute = (System.currentTimeMillis() - startTime) / (60 * 1000);
                logger.d("Recording stable running period:" + minute + ", next upload is " + nextUploadDuringMinutes +
                        " minutes later.");
               Answers.getInstance()
                       .logCustom(new CustomEvent("XMPush_stable_running")
                       .putCustomAttribute("running_time_min", minute));
                // Schedule next running and skip next "first" running
                scheduleNextUpload();
            }
        }, 0, nextUploadDuringMinutes * 1000 * 60);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        logger.d("Service started");
        startTime = System.currentTimeMillis();
        if (!DEBUG && !BuildConfig.FABRIC_KEY.equals("null"))
            scheduleNextUpload();
        startForeground();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) BackgroundActivityStartEnabler.initialize(this);
    }

    @Override
    public void attachBaseContext(Context base) {
        logger.d("attachBaseContext");
        super.attachBaseContext(CondomContext.wrap(base, TAG_CONDOM, XMOutbound.create(base,
                TAG)));
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return Service.START_STICKY;
    }

    @Override public void onConfigurationChanged(final Configuration newConfig) {
        startForeground();      // Apply locale change to foreground notification
    }

    @Override public ClientEventDispatcher createClientEventDispatcher() {
        return new MyClientEventDispatcher();
    }

    @Override
    public void onDestroy() {
        logger.d("Service stopped");
        stopForeground(true);

        if (mStatTimer != null) mStatTimer.cancel();
        super.onDestroy();
    }


    private void startForeground() {
        NotificationManager manager = (NotificationManager)
                getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_STATUS,
                    getString(R.string.notification_category_alive),
                    NotificationManager.IMPORTANCE_MIN);
            manager.createNotificationChannel(channel);
        }
        //if (ConfigCenter.getInstance().foregroundNotification || Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        {
            Notification notification = new NotificationCompat.Builder(this,
                    CHANNEL_STATUS)
                    .setContentTitle(getString(R.string.notification_alive))
                    .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .setOngoing(true)
                    .setShowWhen(true)
                    .build();

            startForeground(NOTIFICATION_ALIVE_ID, notification);
        }
    }
}
