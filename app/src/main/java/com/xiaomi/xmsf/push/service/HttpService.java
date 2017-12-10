package com.xiaomi.xmsf.push.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build.VERSION;
import android.os.IBinder;
import android.text.TextUtils;

import com.xiaomi.channel.commonutils.network.Network;
import com.xiaomi.xmsf.push.service.IHttpService.Stub;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpService extends Service {
    private static final List<String> acceptedHost = new ArrayList();
    private final Stub mBinder = new C00141();

    class C00141 extends Stub {
        C00141() {
        }

        public String doHttpPost(String str, Map map) {
            if (isUnmeteredNetworkConnected() && isAcceptedHost(str) && map != null) {
                Map hashMap = new HashMap();
                for (Object next : map.keySet()) {
                    if (!(next == null || map.get(next) == null)) {
                        hashMap.put(next.toString(), map.get(next).toString());
                    }
                }
                try {
                    return Network.doHttpPost(HttpService.this.getApplicationContext(), str, hashMap).responseString;
                } catch (Exception e) {
                }
            }
            return null;
        }

        public boolean isAcceptedHost(String str) {
            if (!TextUtils.isEmpty(str)) {
                try {
                    return HttpService.acceptedHost.contains(new URL(str).getHost());
                } catch (MalformedURLException e) {
                }
            }
            return false;
        }

        @SuppressLint({"NewApi"})
        public boolean isUnmeteredNetworkConnected() {
            ConnectivityManager connectivityManager = (ConnectivityManager) HttpService.this.getApplicationContext().getSystemService("connectivity");
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null) {
                if (activeNetworkInfo.getType() == 1) {
                    return true;
                }
                if (VERSION.SDK_INT >= 16) {
                    return !connectivityManager.isActiveNetworkMetered();
                }
            }
            return false;
        }
    }

    static {
        acceptedHost.add("data.mistat.xiaomi.com");
    }

    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }
}
