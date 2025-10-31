package cn.sym.service;


public interface CacheService {

    /**
     * 根据cacheKey从Redis中获取缓存数据
     * @param cacheKey 缓存key
     * @return 缓存数据
     */
    String getCacheData(String cacheKey);

    /**
     * 将指定的键值对写入Redis缓存中
     * @param cacheKey 缓存key
     * @param cacheValue 缓存value
     */
    void updateCacheData(String cacheKey, String cacheValue);

    /**
     * 根据cacheKey从Redis中删除对应缓存项
     * @param cacheKey 缓存key
     */
    void deleteCacheData(String cacheKey);
}