package com.geekq.miaosha.order.service.impl;


import com.geekq.miaosha.common.vo.PayOrderVo;
import com.geekq.miaosha.order.manager.ThirdPayService;
import com.geekq.miaosha.order.service.IPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PayService implements IPayService {

    @Autowired
    private ThirdPayService thirdPayService;

    public String pay(PayOrderVo payOrderVo) {


        return thirdPayService.pay(payOrderVo);
    }
}
