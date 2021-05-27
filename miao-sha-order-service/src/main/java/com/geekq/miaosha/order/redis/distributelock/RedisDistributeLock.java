package com.geekq.miaosha.order.redis.distributelock;

public @interface RedisDistributeLock {
      String lockKey() default "lockKey";
      long expireTime() default 10000;
}
