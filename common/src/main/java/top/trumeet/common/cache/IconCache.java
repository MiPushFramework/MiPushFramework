package top.trumeet.common.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.util.LruCache;

import top.trumeet.common.utils.ImgUtils;

import static top.trumeet.common.utils.ImgUtils.drawableToBitmap;

public class IconCache {

    private volatile static IconCache cache = null;
    private LruCache<String, Bitmap> mIconMemoryCaches;

    private IconCache() {
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSizes = maxMemory / 5;
        mIconMemoryCaches = new LruCache<>(cacheSizes);
    }

    public static IconCache getInstance() {
        if (cache == null) {
            synchronized (IconCache.class) {
                if (cache == null) {
                    cache = new IconCache();
                }
            }
        }
        return cache;
    }

    public Bitmap getRawIconBitmap(Context ctx, String pkg) {
        String cacheKey = "raw_" + pkg;
        Bitmap iconBitmap = mIconMemoryCaches.get(cacheKey);
        if (iconBitmap == null) {
            try {
                Drawable icon = ctx.getPackageManager().getApplicationIcon(pkg);
                iconBitmap = drawableToBitmap(icon);
            } catch (Exception ignored) {
            }

            if (iconBitmap != null) {
                mIconMemoryCaches.put(cacheKey, iconBitmap);
            }
        }
        return iconBitmap;
    }


    public Bitmap getWhiteIconBitmap(Context ctx, String pkg) {
        String cacheKey = "white_" + pkg;
        Bitmap whiteBitmap = mIconMemoryCaches.get(cacheKey);
        if (whiteBitmap == null) {
            Bitmap rawIconBitmap = getRawIconBitmap(ctx, pkg);
            if (rawIconBitmap != null) {
                whiteBitmap = ImgUtils.convertToTransparentAndWhite(rawIconBitmap);
                mIconMemoryCaches.put(cacheKey, whiteBitmap);
            }
        }
        return whiteBitmap;
    }


}
