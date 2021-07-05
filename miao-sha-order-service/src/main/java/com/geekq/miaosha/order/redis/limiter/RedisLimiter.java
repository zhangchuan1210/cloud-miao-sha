package com.geekq.miaosha.order.redis.limiter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface RedisLimiter {
    String count() default "10000";
    String expire() default "3000";

    String scriptLocation() default "";

    String scriptString() default "";

}
