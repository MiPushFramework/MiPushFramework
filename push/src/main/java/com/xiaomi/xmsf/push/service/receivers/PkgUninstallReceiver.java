package com.xiaomi.xmsf.push.service.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import com.xiaomi.channel.commonutils.logger.MyLog;
import com.xiaomi.push.service.GeoFenceUtils;
import com.xiaomi.push.service.PushServiceConstants;
import com.xiaomi.push.service.XMPushService;

public class PkgUninstallReceiver extends BroadcastReceiver {
    public PkgUninstallReceiver() {
    }

    public void onReceive(Context var1, Intent var2) {
        if (var2 != null && var2.getExtras() !=null&& "android.intent.action.PACKAGE_REMOVED".equals(var2.getAction())) {
            boolean var3 = var2.getExtras().getBoolean("android.intent.extra.REPLACING");
            Uri var4 = var2.getData();
            if (var4 != null && !var3) {
                try {
                    Intent var5 = new Intent(var1, XMPushService.class);
                    var5.setAction(PushServiceConstants.ACTION_UNINSTALL);
                    var5.putExtra("uninstall_pkg_name", var4.getEncodedSchemeSpecificPart());
                    var1.startService(var5);
                    GeoFenceUtils.appIsUninstalled(var1.getApplicationContext(), var4.getEncodedSchemeSpecificPart());
                } catch (Exception var7) {
                    MyLog.e(var7);
                }
            }
        }

    }
}
