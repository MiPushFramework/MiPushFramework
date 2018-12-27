package com.xiaomi.xmsf.push.control;

import android.app.AppOpsManager;
import android.app.AppOpsManagerExtender;
import android.content.Context;

import com.elvishew.xlog.Logger;
import com.elvishew.xlog.XLog;
import com.oasisfeng.condom.CondomKit;

import top.trumeet.common.override.AppOpsManagerOverride;
import top.trumeet.common.override.ManifestOverride;

/**
 * Created by Trumeet on 2018/1/19.
 */

public class AppOpsKit implements CondomKit, CondomKit.SystemServiceSupplier {
    private Logger logger = XLog.tag("AppOpsKit").build();

    @Override
    public void onRegister(CondomKitRegistry registry) {
        registry.addPermissionSpoof(ManifestOverride.permission.GET_APP_OPS_STATS);
        registry.registerSystemService(Context.APP_OPS_SERVICE, this);
    }


    @Override public Object getSystemService(final Context context, final String name) {
        if (Context.APP_OPS_SERVICE.equals(name))
            return new CondomAppOpsManager(context);
        return null;
    }

    class CondomAppOpsManager extends AppOpsManagerExtender {
        CondomAppOpsManager (Context context) {
            super(context);
        }

        @Override
        public int checkOpNoThrow(int op, int uid, String packageName) {
            if (op == AppOpsManagerOverride.OP_POST_NOTIFICATION) {
                logger.d("check post notification op: " + uid + ", " + packageName);
                // TODO: add a option is better.
                return AppOpsManager.MODE_ALLOWED;
            }
            return super.checkOpNoThrow(op, uid, packageName);
        }
    }
}
