package com.geekq.miaosha.order.redis.limiter;

import cn.hutool.core.collection.CollUtil;
import com.geekq.miaosha.order.redis.RedisService;
import com.geekq.miaosha.order.redis.distributelock.RedisDistributeLock;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Aspect
@Component
public class RedisLimiterAspect {
    @Autowired
    private RedisService redisService;

    @Pointcut("@annotation(com.geekq.miaosha.order.redis.limiter.RedisLimiter)")
    public void limiter(){}
    @Before(value ="limiter()" )
    public void beforeMethod(JoinPoint joinPoint) throws Throwable {
        long result=0;
        Class<?> aClass = joinPoint.getTarget().getClass();
        String name = joinPoint.getSignature().getName();//获取该切点所在方法的名称
        Method method =null;
        Method[] methods=aClass.getDeclaredMethods();
        for(int i=0,j=methods.length;i<j;++i){
            if(methods[i].getName()==name){
                method=methods[i];
                break;
            }
        }
        if(null!=method){
            RedisLimiter annotation = method.getAnnotation(RedisLimiter.class);
            String count=annotation.count();
            String expireTime=annotation.expire();
            String scriptLocation=annotation.scriptLocation();
            String scriptStr=annotation.scriptString();
            String limiterMethodName=System.currentTimeMillis()/1000+"_"+name;
            List<String> keys=new ArrayList<>();
            keys.add(limiterMethodName);
            if(StringUtils.isNotEmpty(scriptLocation) || StringUtils.isNotEmpty(scriptStr)) {
                result=(Long)redisService.execScript(scriptLocation,Long.class,keys,count,expireTime);
            }
            if(result<=0){
                return ;
            }
        }


    }





}
