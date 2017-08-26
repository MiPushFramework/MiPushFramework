package com.xiaomi.xmsf.push.service.receivers;

import com.xiaomi.mipush.sdk.PushMessageReceiver;

public class MiuiPushMessageReceiver extends PushMessageReceiver {
    // We needn't do anything. This receiver is never called.
    // XMPush will auto send broadcast to target app.
    // We use Condom to check push request.
    // This receiver is only use to pass XMPush manifest check.
}
