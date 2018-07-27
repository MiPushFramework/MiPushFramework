package top.trumeet.common.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.util.LruCache;

import top.trumeet.common.utils.ImgUtils;

import static top.trumeet.common.utils.ImgUtils.drawableToBitmap;

/**
 * Author: TimothyZhang023
 * Icon Cache
 */
public class IconCache {

    private volatile static IconCache cache = null;
    private LruCache<String, Bitmap> mIconMemoryCaches;

    private IconCache() {
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSizes = maxMemory / 5;
        mIconMemoryCaches = new LruCache<>(cacheSizes);
        //TODO check cacheSizes is correct ?
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

    public Bitmap getRawIconBitmapWithoutLoader(final Context ctx, final String pkg) {
        return mIconMemoryCaches.get("raw_" + pkg);
    }

    public Bitmap getRawIconBitmap(final Context ctx, final String pkg) {
        return new AbstractCacheAspect<Bitmap>(mIconMemoryCaches) {
            @Override
            Bitmap gen() {
                try {
                    Drawable icon = ctx.getPackageManager().getApplicationIcon(pkg);
                    return drawableToBitmap(icon);
                } catch (Exception ignored) {
                    return null;
                }
            }
        }.get("raw_" + pkg);
    }

    public Bitmap getWhiteIconBitmap(final Context ctx, final String pkg) {
        return new AbstractCacheAspect<Bitmap>(mIconMemoryCaches) {
            @Override
            Bitmap gen() {
                Bitmap rawIconBitmap = getRawIconBitmap(ctx, pkg);
                return rawIconBitmap == null ? null : ImgUtils.convertToTransparentAndWhite(rawIconBitmap);
            }
        }.get("white_" + pkg);
    }



}
