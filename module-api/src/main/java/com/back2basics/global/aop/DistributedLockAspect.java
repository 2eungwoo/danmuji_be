package com.back2basics.global.aop;

import com.back2basics.infra.exception.global.LockAcquisitionException;
import java.lang.reflect.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class DistributedLockAspect {

    private static final String LOCK_KEY_PREFIX = "LOCK:";
    private final RedissonClient redissonClient;
    private final CacheManager cacheManager;
    private final ApplicationContext applicationContext;

    @Around("@annotation(preventCacheStampede)")
    public Object applyLock(ProceedingJoinPoint joinPoint, PreventCacheStampede preventCacheStampede) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();

        Object cacheKey = generateKey(method, args, preventCacheStampede.key());
        String lockKey = LOCK_KEY_PREFIX + preventCacheStampede.cacheNames()[0] + ":" + cacheKey;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean isLocked = lock.tryLock(preventCacheStampede.waitTime(), preventCacheStampede.timeUnit());
            if (!isLocked) {
                log.warn("Failed to acquire lock: {}. Retrying to get from cache.", lockKey);
                return tryGetFromCache(preventCacheStampede, cacheKey);
            }
            log.info("Acquired lock: {}", lockKey);
            return joinPoint.proceed();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LockAcquisitionException();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("Released lock: {}", lockKey);
            }
        }
    }

    private Object tryGetFromCache(PreventCacheStampede preventCacheStampede, Object cacheKey) throws InterruptedException, LockAcquisitionException {
        String cacheName = preventCacheStampede.cacheNames()[0];
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            throw new IllegalArgumentException("캐시에 없음 " + cacheName);
        }

        for (int i = 0; i < preventCacheStampede.retryCount(); i++) {
            Thread.sleep(preventCacheStampede.retryDelay());
            Cache.ValueWrapper valueWrapper = cache.get(cacheKey);
            if (valueWrapper != null) {
                log.info("============== retry =================");
                return valueWrapper.get();
            }
        }
        throw new LockAcquisitionException();
    }

    private Object generateKey(Method method, Object[] args, String spelExpression) {
        ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
        ExpressionParser parser = new SpelExpressionParser();
        MethodBasedEvaluationContext context = new MethodBasedEvaluationContext(null, method, args, parameterNameDiscoverer);
        context.setBeanResolver(new BeanFactoryResolver(applicationContext));
        return parser.parseExpression(spelExpression).getValue(context);
    }
}