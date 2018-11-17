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
    private LruCache<String, Integer> appColorCache;

    private IconCache() {
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSizes = maxMemory / 5;
        mIconMemoryCaches = new LruCache<>(cacheSizes);
        appColorCache = new LruCache<>(100);
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
                return new WhiteIconProcess().convert(ctx, rawIconBitmap);
            }
        }.get("white_" + pkg);
    }


    public int getAppColor(final Context ctx, final String pkg, Converter<Bitmap, Integer> callback) {
        return new AbstractCacheAspect<Integer>(appColorCache) {
            @Override
            Integer gen() {
                Bitmap rawIconBitmap = getRawIconBitmap(ctx, pkg);
                if (rawIconBitmap == null) {
                    return -1;
                }
                return callback.convert(ctx, rawIconBitmap);
            }
        }.get(pkg);
    }

    class WhiteIconProcess implements Converter<Bitmap, Bitmap> {
        @Override
        public Bitmap convert(Context ctx, Bitmap b) {
            if (b == null) {
                return null;
            }

            //scaleImage to 64dp
            int dip2px = dip2px(ctx, 64);
            return ImgUtils.scaleImage(ImgUtils.convertToTransparentAndWhite(b), dip2px, dip2px);
        }
    }

    public interface Converter<T,R> {
        R convert(Context ctx, T b);
    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

}
