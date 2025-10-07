package com.back2basics.global.cache;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CacheVersionService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String VERSION_PREFIX = "versions:";
    private static final long DEFAULT_TTL_DAYS = 7;

    public long getVersion(Long userId, String contentsName) {
        String versionKey = generateVersionKey(userId, contentsName);
        String version = redisTemplate.opsForValue().get(versionKey);
        return version == null ? 0L : Long.parseLong(version);
    }

    public void incrementVersion(Long userId, String contentsName) {
        String versionKey = generateVersionKey(userId, contentsName);
        redisTemplate.opsForValue().increment(versionKey);
        redisTemplate.expire(versionKey, DEFAULT_TTL_DAYS, TimeUnit.DAYS);
    }

    private String generateVersionKey(Long userId, String contentsName) {
        return VERSION_PREFIX + userId + ":" + contentsName;
    }
}
