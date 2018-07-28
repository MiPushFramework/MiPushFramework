package com.xiaomi.xmsf.utils;

import android.app.Notification;
import android.graphics.Bitmap;
import android.support.v7.graphics.Palette;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;


/**
 *
 * @author Trumeet
 * @date 2018/2/18
 */

public class ColorUtil {
    public static int getIconColor(Bitmap bitmap) {
        return Palette.from(bitmap)
                .generate().getVibrantColor(Notification.COLOR_DEFAULT);
    }


    public static Spannable createColorSubtext(CharSequence appName, int color) {
        final Spannable amened = new SpannableStringBuilder(appName);
        amened.setSpan(new ForegroundColorSpan(color),
                0, amened.length(), 0);
        return amened;
    }
}
