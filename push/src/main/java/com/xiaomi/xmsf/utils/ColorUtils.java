package com.xiaomi.xmsf.utils;

import android.app.Notification;
import android.graphics.drawable.Drawable;
import android.support.v7.graphics.Palette;

import static top.trumeet.common.utils.Utils.drawableToBitmap;

/**
 * Created by Trumeet on 2018/2/18.
 */

public class ColorUtils {
    public static int getIconColor (Drawable icon) {
        return Palette.from(drawableToBitmap(icon))
                .generate().getVibrantColor(Notification.COLOR_DEFAULT);
    }
}
