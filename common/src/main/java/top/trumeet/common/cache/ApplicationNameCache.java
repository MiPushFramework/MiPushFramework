package top.trumeet.common.cache;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import androidx.collection.LruCache;

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
                PackageManager pm = ctx.getPackageManager();
                try {
                    name = pm.getApplicationInfo(pkg, PackageManager.GET_UNINSTALLED_PACKAGES).loadLabel(pm);
                } catch (PackageManager.NameNotFoundException | Resources.NotFoundException ignored) { }

                return name;
            }
        }.get(pkg);
    }


}
