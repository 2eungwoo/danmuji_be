package com.back2basics.global.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PreventCacheStampede {

    String[] cacheNames();

    String key();

    long waitTime() default 5L;

    TimeUnit timeUnit() default TimeUnit.SECONDS;

    long retryDelay() default 200L;

    int retryCount() default 5;
}
