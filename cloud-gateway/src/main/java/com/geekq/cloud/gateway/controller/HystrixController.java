package com.geekq.cloud.gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hystrix")
public class HystrixController {

    @RequestMapping("/fallback")
    public Object hystrixFallback(){
        return "service is exception";

    }
}
