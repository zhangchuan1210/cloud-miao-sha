package com.geekq.miaosha.order.controller;


import com.geekq.miaosha.order.service.IPayService;
import com.geekq.miaosha.common.vo.PayOrderVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pay")
public class PayController {
    @Autowired
    private IPayService payService;

    @RequestMapping(value="",method = RequestMethod.GET)
    @ResponseBody
    public String pay(PayOrderVo payOrderVo){
        return payService.pay(payOrderVo);
    }

}
