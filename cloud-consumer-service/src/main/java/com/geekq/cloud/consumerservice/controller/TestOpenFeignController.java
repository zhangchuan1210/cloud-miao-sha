package com.geekq.cloud.consumerservice.controller;

import com.geekq.cloud.consumerservice.service.ITestOpenFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/openfeign")
public class TestOpenFeignController {
    @Autowired
    private ITestOpenFeignService openFeignService;
    @RequestMapping(value = "/test",method = RequestMethod.GET)
    public String test(){
        String msg=openFeignService.test();
        return msg;
    }
}
