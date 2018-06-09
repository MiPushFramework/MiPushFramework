package top.trumeet.common.cache;

import android.support.v4.util.LruCache;

/**
 * @author zts
 */
abstract class AbstractCacheAspect<T> {
    private LruCache<String, T> cache;

    AbstractCacheAspect(LruCache<String, T> cache) {
        this.cache = cache;
    }

    public T get(String cacheKey) {
        T cached = cache.get(cacheKey);
        if (cached == null) {
            cached = gen();
            if (cached != null) {
                cache.put(cacheKey, cached);
            }
        }
        return cached;
    }

    /**
     * @return from DataSource
     */
    abstract T gen();
}