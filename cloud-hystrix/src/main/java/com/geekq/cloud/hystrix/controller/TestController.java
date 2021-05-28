package com.geekq.cloud.hystrix.controller;

import com.geekq.cloud.hystrix.service.ITestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {
    @Autowired
    private ITestService testService;
    @RequestMapping(value = "/test",method = RequestMethod.GET)
    public String test(){
        String msg=testService.testOk();
        return msg;
    }

}
