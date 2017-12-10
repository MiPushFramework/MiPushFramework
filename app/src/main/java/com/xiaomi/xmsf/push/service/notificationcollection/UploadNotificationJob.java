package com.xiaomi.xmsf.push.service.notificationcollection;

import android.content.Context;

import com.xiaomi.channel.commonutils.file.IOUtils;
import com.xiaomi.channel.commonutils.misc.ScheduledJobManager.Job;
import com.xiaomi.channel.commonutils.network.Network;
import com.xiaomi.mipush.sdk.PushServiceClient;
import com.xiaomi.xmpush.thrift.ActionType;
import com.xiaomi.xmpush.thrift.NotificationBarInfo;
import com.xiaomi.xmpush.thrift.NotificationType;
import com.xiaomi.xmpush.thrift.XmPushActionNotification;
import com.xiaomi.xmpush.thrift.XmPushThriftSerializeUtils;

public class UploadNotificationJob extends Job {
    private Context mContext;

    public UploadNotificationJob(Context context) {
        this.mContext = context;
    }

    public int getJobId() {
        return 12;
    }

    public void run() {
        if (Network.isWIFIConnected(this.mContext)) {
            NotificationBarInfo takeNotificationInfo = NotificationListener.takeNotificationInfo();
            if (takeNotificationInfo.getDataSize() != 0) {
                XmPushActionNotification xmPushActionNotification = new XmPushActionNotification("-1", false);
                xmPushActionNotification.setType(NotificationType.NotificationBarInfo.value);
                xmPushActionNotification.setBinaryExtra(IOUtils.gZip(XmPushThriftSerializeUtils.convertThriftObjectToBytes(takeNotificationInfo)));
                PushServiceClient.getInstance(this.mContext).sendMessage(xmPushActionNotification, ActionType.Notification, null);
            }
        }
    }
}
