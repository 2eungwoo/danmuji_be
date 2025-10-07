package com.back2basics.global.cache;

import com.back2basics.global.config.CacheKeyProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service("dashboardCacheService")
@RequiredArgsConstructor
public class DashboardCacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private final CacheKeyProperties cacheKeyProperties;

    private static final String VERSION_PREFIX = "versions:";
    private static final long DEFAULT_TTL_DAYS = 7;

    public String generateKey(Long userId, String contentsName) {
        long version = getVersion(userId, contentsName);
        return cacheKeyProperties.getDashboard() + ":" + userId + ":" + contentsName + ":" + version;
    }

    public void incrementVersion(Long userId, String contentsName) {
        String versionKey = generateVersionKey(userId, contentsName);
        redisTemplate.opsForValue().increment(versionKey);
        redisTemplate.expire(versionKey, DEFAULT_TTL_DAYS, TimeUnit.DAYS);
    }

    private long getVersion(Long userId, String contentsName) {
        String versionKey = generateVersionKey(userId, contentsName);
        String version = redisTemplate.opsForValue().get(versionKey);
        return version == null ? 0L : Long.parseLong(version);
    }

    private String generateVersionKey(Long userId, String contentsName) {
        return VERSION_PREFIX + cacheKeyProperties.getDashboard() + ":" + userId + ":" + contentsName;
    }
}
