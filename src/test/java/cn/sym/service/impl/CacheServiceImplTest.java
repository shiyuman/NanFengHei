package cn.sym.service.impl;

import cn.sym.common.constant.ResultCodeConstant;
import cn.sym.common.exception.BusinessException;
import cn.sym.service.impl.CacheServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CacheServiceImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @InjectMocks
    private CacheServiceImpl cacheService;

    @BeforeEach
    public void setUp() {
        // 初始化操作
    }

    @Test
    public void testGetCacheData_Hit() {
        when(redisTemplate.opsForValue().get(anyString())).thenReturn("testValue");
        String result = cacheService.getCacheData("testKey");
        assertEquals("testValue", result);
    }

    @Test
    public void testGetCacheData_Miss() {
        when(redisTemplate.opsForValue().get(anyString())).thenReturn(null);
        BusinessException exception = assertThrows(BusinessException.class, () -> cacheService.getCacheData("testKey"));
        assertEquals(ResultCodeConstant.CODE_000001, exception.getCode());
    }

    @Test
    public void testUpdateCacheData_Success() {
        cacheService.updateCacheData("testKey", "testValue");
        verify(redisTemplate.opsForValue(), times(1)).set("testKey", "testValue");
    }

    @Test
    public void testUpdateCacheData_Failure() {
        doThrow(new RuntimeException("Redis error")).when(redisTemplate.opsForValue()).set(anyString(), anyString());
        BusinessException exception = assertThrows(BusinessException.class, () -> cacheService.updateCacheData("testKey", "testValue"));
        assertEquals(ResultCodeConstant.CODE_999999, exception.getCode());
    }

    @Test
    public void testDeleteCacheData_Success() {
        cacheService.deleteCacheData("testKey");
        verify(redisTemplate, times(1)).delete("testKey");
    }

    @Test
    public void testDeleteCacheData_Failure() {
        doThrow(new RuntimeException("Redis error")).when(redisTemplate).delete(anyString());
        BusinessException exception = assertThrows(BusinessException.class, () -> cacheService.deleteCacheData("testKey"));
        assertEquals(ResultCodeConstant.CODE_999999, exception.getCode());
    }
}