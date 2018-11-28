package top.trumeet.common.cache;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.util.LruCache;

/**
 * @author zts
 */
public class ApplicationNameCache {
    private volatile static ApplicationNameCache cache = null;
    private LruCache<String, CharSequence> cacheInstance;


    private ApplicationNameCache() {
        cacheInstance = new LruCache<>(100);
    }

    public static ApplicationNameCache getInstance() {
        if (cache == null) {
            synchronized (ApplicationNameCache.class) {
                if (cache == null) {
                    cache = new ApplicationNameCache();
                }
            }
        }
        return cache;
    }

    public CharSequence getAppName(final Context ctx, final String pkg) {

        return new AbstractCacheAspect<CharSequence>(cacheInstance) {
            @Override
            CharSequence gen() {
                CharSequence name = pkg;
                try {
                    name = ctx.getPackageManager().getApplicationInfo(pkg, 0).loadLabel(ctx.getPackageManager());
                } catch (PackageManager.NameNotFoundException ignored) { }

                return name;
            }
        }.get(pkg);
    }


}
