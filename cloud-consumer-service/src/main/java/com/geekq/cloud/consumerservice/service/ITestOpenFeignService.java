package com.geekq.cloud.consumerservice.service;

import com.geekq.cloud.consumerservice.service.impl.TestOpenFeignFalbackService;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(value="cloud-consumer2-service",fallback = TestOpenFeignFalbackService.class)
@Component
public interface ITestOpenFeignService {
    @RequestMapping(value = "/orderTbl/testOrder",method = RequestMethod.GET)
    String test();
}
