package com.xiaomi.xmsf.push.control;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.oasisfeng.condom.CondomOptions;
import com.oasisfeng.condom.OutboundJudge;
import com.oasisfeng.condom.OutboundType;
import com.oasisfeng.condom.kit.NullDeviceIdKit;

import me.pqpo.librarylog4a.Log4a;

/**
 * Created by Trumeet on 2018/1/19.
 */

public class XMOutbound implements OutboundJudge {
    private final String TAG;
    private final Context context;

    private XMOutbound (Context context, String tag) {
        this.context = context;
        this.TAG = tag;
    }

    public static CondomOptions create (Context context, String tag,
                                        boolean enableKit) {
        CondomOptions options = new CondomOptions()
                .preventBroadcastToBackgroundPackages(false)
                .setOutboundJudge(new XMOutbound(context, tag));
        if (enableKit)
            options.addKit(new NullDeviceIdKit())
                    .addKit(new AppOpsKit())
                .addKit(new NotificationManagerKit())
                    ;
        return options;
    }

    public static CondomOptions create (Context context, String tag) {
        return create(context, tag, true);
    }



    @Override
    public boolean shouldAllow(OutboundType type, @Nullable Intent intent, String target_package) {
        Log4a.d(TAG, "shouldAllow ->" + type.toString());
        return true;
    }
}
