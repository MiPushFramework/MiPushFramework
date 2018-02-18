package com.xiaomi.push.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.oasisfeng.condom.CondomContext;
import com.oasisfeng.condom.CondomKit;
import com.xiaomi.smack.packet.Message;
import com.xiaomi.xmpush.thrift.ActionType;
import com.xiaomi.xmpush.thrift.PushMetaInfo;
import com.xiaomi.xmsf.R;
import com.xiaomi.xmsf.push.control.XMOutbound;
import com.xiaomi.xmsf.push.notification.OreoNotificationManager;

import org.apache.thrift.TBase;

import me.pqpo.librarylog4a.Log4a;

import static top.trumeet.common.Constants.TAG_CONDOM;

/**
 * Created by Trumeet on 2018/1/19.
 *
 * PayLoad 主要在 {@link com.xiaomi.mipush.sdk.PushServiceClient#sendMessage(TBase, ActionType, PushMetaInfo)} 等重载方法中处理。
 * 具体转换： {@link com.xiaomi.xmpush.thrift.XmPushThriftSerializeUtils#convertByteArrayToThriftObject(TBase, byte[])} 中。
 *
 * 0. 万恶之源
 * 主要 intent 处理是在 {@link XMPushService#handleIntent(Intent)}
 *
 * 1. 向服务器发送
 * <strong>向服务器</strong>发送消息是 {@link XMPushService#sendMessage(Intent)}（解析 Intent -> Package）
 * 对应服务启动方：{@link com.xiaomi.push.service.ServiceClient#sendMessage(Message, boolean)}
 * Action: {@link PushConstants#ACTION_SEND_MESSAGE}
 * Extras: {@link PushConstants#MIPUSH_EXTRA_PAYLOAD}, {@link PushConstants#MIPUSH_EXTRA_MESSAGE_CACHE}, {@link PushConstants#MIPUSH_EXTRA_APP_PACKAGE}
 *
 * {@link XMPushService#sendMessage(String, byte[], boolean)}（解析 payload (byte[]）-> 未知）
 * 对应启动方： {@link com.xiaomi.mipush.sdk.PushServiceClient#sendMessage(TBase, ActionType, PushMetaInfo)}
 * Action: {@link PushConstants#MIPUSH_ACTION_SEND_MESSAGE}
 *
 * {@link XMPushService#sendMessages(Intent)}（和第一个一样，批量）
 * 对应启动方： {@link com.xiaomi.push.service.ServiceClient#batchSendMessage(Message[], boolean)}
 * Action: {@link PushConstants#ACTION_BATCH_SEND_MESSAGE}
 *
 * 由此推测，XMPushService 具有<strong>向服务器</strong>发送消息的功能（三种途径），其控制方是 {@link com.xiaomi.push.service.ServiceClient} 和
 * {@link com.xiaomi.mipush.sdk.PushServiceClient}。
 *
 * 看具体的 Job（{@link com.xiaomi.push.service.SendMessageJob}， {@link com.xiaomi.push.service.BatchSendMessageJob}），
 * 都是 {@link XMPushService} 收到 Intent 之后，启动 job，然后 job 回掉 XMPushService 进行 send packet，然后应该是发送给服务器了..
 * 所以这里的发送消息是通过 {@link XMPushService} 向服务器发送 Packet / Blob。
 *
 * 2. 本地发送
 * 根据发送消息中出错的 Stacktrace，我们可以轻松找出发送消息的下游处理。
 * 首先，在 {@link XMPushService#connectBySlim} 连接时，注册了包监听器 {@link XMPushService#C00621}。
 * 它在监听到 Blob 和 Packet 后启动 {@link BlobReceiveJob} 和 {@link PacketReceiveJob}。
 * 这两个 Job 都将数据交给 {@link PacketSync} 处理。但两者最后都经过检测 / 处理，将数据交给 {@link ClientEventDispatcher#notifyPacketArrival} 处理。
 *
 * 3. 其他
 * 还有其他处理也在这里，比如注册 App（ {@link PushConstants#MIPUSH_ACTION_REGISTER_APP}）等，暂未支持。
 *
 * 监听解决方案：
 * 所有包都会经过 {@link com.xiaomi.smack.PacketListener}，官方有一个处理器（C00621），持有在非静态 Field mPacketListener 中。
 * 所以我们只需创造一个自己的 {@link ClientEventDispatcher}，重写相关方法，即可完成处理。
 * 这种方式相比自己反射修改 {@link com.xiaomi.smack.PacketListener}，更优雅（无需反射，同时小米为我们提供了 {@link XMPushService#createClientEventDispatcher()} 方法），
 * 同时还能通过官方提供的处理逻辑（{@link PacketSync}），直接处理消息通知包。
 */

public class PushServiceMain extends XMPushService {
    private static final String TAG = "PushService";

    public static final String CHANNEL_STATUS = "status";
    public static final String CHANNEL_WARNING = "warning";
    public static final int NOTIFICATION_ALIVE_ID = 0;

    private OreoNotificationManager mNotificationManager;

    @Override
    public void attachBaseContext (Context base) {
        Log4a.d(TAG, "attachBaseContext");
        super.attachBaseContext(CondomContext.wrap(base, TAG_CONDOM, XMOutbound.create(base,
                TAG)
        .addKit(new CondomKit() {
            @Override
            public void onRegister(CondomKitRegistry registry) {
                registry.registerSystemService(Context.NOTIFICATION_SERVICE,
                        new SystemServiceSupplier() {
                            @Override
                            public Object getSystemService(Context context, String name) {
                                if (Context.NOTIFICATION_SERVICE.equals(name)) {
                                    // 喂给服务一个这样的 Manager，可以区分包
                                    if (mNotificationManager == null)
                                        mNotificationManager = new OreoNotificationManager(context);
                                    return mNotificationManager;
                                }
                                return null;
                            }
                        });
            }
        })));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        NotificationManager manager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_STATUS,
                    getString(R.string.notification_category_alive),
                    NotificationManager.IMPORTANCE_MIN);
            manager.createNotificationChannel(channel);
        }
        Notification notification = new NotificationCompat.Builder(this,
                CHANNEL_STATUS)
                .setContentTitle(getString(R.string.notification_alive))
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setOngoing(true)
                .setShowWhen(false)
                .build();
        manager.notify(NOTIFICATION_ALIVE_ID, notification);
        startForeground(NOTIFICATION_ALIVE_ID, notification);
        return Service.START_STICKY;
    }

    @Override
    public ClientEventDispatcher createClientEventDispatcher() {
        return new MyClientEventDispatcher();
    }

    @Override
    public void onDestroy () {
        ((NotificationManager)getSystemService(NOTIFICATION_SERVICE))
                .cancel(NOTIFICATION_ALIVE_ID);
        super.onDestroy();
    }

    @Nullable
    public OreoNotificationManager getNotificationManager () {
        return mNotificationManager;
    }
}
