package com.xiaomi.xmsf.utils;

import android.app.Notification;
import android.graphics.Bitmap;
import android.support.v7.graphics.Palette;


/**
 * Created by Trumeet on 2018/2/18.
 */

public class ColorUtils {
    public static int getIconColor(Bitmap bitmap) {
        return Palette.from(bitmap)
                .generate().getVibrantColor(Notification.COLOR_DEFAULT);
    }
}
