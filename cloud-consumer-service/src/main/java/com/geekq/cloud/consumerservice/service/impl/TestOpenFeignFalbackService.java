package com.geekq.cloud.consumerservice.service.impl;

import com.geekq.cloud.consumerservice.service.ITestOpenFeignService;
import org.springframework.stereotype.Component;

@Component
public class TestOpenFeignFalbackService implements ITestOpenFeignService {
    @Override
    public String test() {
        return "openfeign fallback!!!!";
    }
}
