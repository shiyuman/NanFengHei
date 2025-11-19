package cn.sym.service.impl;

import cn.sym.common.constant.ResultCodeConstant;
import cn.sym.common.exception.BusinessException;
import cn.sym.service.CacheService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CacheServiceImpl implements CacheService {

    private static final Logger log = LoggerFactory.getLogger(CacheServiceImpl.class);

    private final StringRedisTemplate redisTemplate;

    @Override
    public String getCacheData(String cacheKey) {
        String cacheData = redisTemplate.opsForValue().get(cacheKey);
        if (cacheData == null) {
            throw new BusinessException(ResultCodeConstant.CODE_000001, ResultCodeConstant.CODE_000001_MSG);
        }
        return cacheData;
    }

    @Override
    public void updateCacheData(String cacheKey, String cacheValue) {
        try {
            redisTemplate.opsForValue().set(cacheKey, cacheValue);
        } catch (Exception e) {
            log.error("更新缓存失败: ", e);
            throw new BusinessException(ResultCodeConstant.CODE_999999, ResultCodeConstant.CODE_999999_MSG);
        }
    }

    @Override
    public void deleteCacheData(String cacheKey) {
        try {
            redisTemplate.delete(cacheKey);
        } catch (Exception e) {
            log.error("删除缓存失败: ", e);
            throw new BusinessException(ResultCodeConstant.CODE_999999, ResultCodeConstant.CODE_999999_MSG);
        }
    }
}