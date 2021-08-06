package com.geekq.cloud.consumerservice.controller;

import com.geekq.cloud.consumerservice.entity.StorageTbl;
import com.geekq.cloud.consumerservice.service.ITestOpenFeignService;
import com.geekq.cloud.consumerservice.service.StorageTblService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/openfeign")
public class TestOpenFeignController {
    @Autowired
    private ITestOpenFeignService openFeignService;

    @Autowired
    private StorageTblService storageTblService;
    @RequestMapping(value = "/test",method = RequestMethod.GET)
    public String test(){
        String msg=openFeignService.test();
        return msg;
    }

    @RequestMapping(value = "/testStorage",method = RequestMethod.GET)
    public String testStorage(){
        storageTblService.testStorage();

        return "ok";
    }

}
