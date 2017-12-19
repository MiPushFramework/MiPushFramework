package com.xiaomi.xmsf.push.service.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationEventReceiver extends BroadcastReceiver
{
    public void onReceive(Context paramContext, Intent paramIntent)
    {
        // TODO: ???
        /*
         *  boolean bool1 = paramIntent.hasExtra("type");
    boolean bool2 = paramIntent.hasExtra("data");
    if ((!bool1) || (!bool2));
    while (true)
    {
      return;
      String str1 = paramIntent.getStringExtra("type");
      String str2 = paramIntent.getStringExtra("data");
      if ((!TextUtils.isEmpty(str1)) && (!TextUtils.isEmpty(str2)))
        TinyDataManager.getInstance().innerUpload("systemui_event", str1, 1L, str2);
    }
         */
        /*
        boolean bool1 = paramIntent.hasExtra("type");
        boolean bool2 = paramIntent.hasExtra("data");
        while ((!bool1) || (!bool2))
        {
            String str1 = paramIntent.getStringExtra("type");
            String str2 = paramIntent.getStringExtra("data");
            if ((!TextUtils.isEmpty(str1)) && (!TextUtils.isEmpty(str2)))
                TinyDataManager.getInstance().innerUpload("systemui_event", str1, 1L, str2);
        }
        */
    }
}