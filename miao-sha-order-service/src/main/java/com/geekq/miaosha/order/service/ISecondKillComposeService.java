package com.geekq.miaosha.order.service;

import com.geekq.miaosha.common.biz.entity.MiaoshaUser;
import com.geekq.miaosha.common.enums.enums.ResultStatus;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;

public interface ISecondKillComposeService {
    @HystrixCommand(
            fallbackMethod = "checkBeforeSecondKillFallBack",
            groupKey="beforeSecondKill",
            commandProperties={
                    @HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds",value="1000"),
                    // 最小请求数
                    @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold",value = "3"),
                    // 失败次数占请求的50%
                    @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage",value = "50"),
                    @HystrixProperty(name="circuitBreaker.sleepWindowInMilliseconds",value= "10000")
            }
    )
    ResultStatus checkBeforeSecondKill(MiaoshaUser user, String path, long goodsId);
}
