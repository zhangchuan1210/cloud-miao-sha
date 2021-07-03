package com.geekq.cloud.hystrix.service.impl;

import com.geekq.cloud.hystrix.service.ITestService;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.stereotype.Service;

@Service
public class TestService implements ITestService {
    @Override
    @HystrixCommand(fallbackMethod="testHystrix",commandProperties={@HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds",value="10000")})
    public String testOk() {

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "ok";
    }

    @Override
    public String testHystrix() {

        return "hystrix";
    }
}
