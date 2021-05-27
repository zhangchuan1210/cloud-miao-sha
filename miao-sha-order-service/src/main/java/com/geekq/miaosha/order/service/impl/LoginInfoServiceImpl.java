package com.geekq.miaosha.order.service.impl;


import com.geekq.miaosha.order.mapper.LogininfoMapper;
import com.geekq.miaosha.order.service.LoginInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author 邱润泽
 */
@Service
public class LoginInfoServiceImpl implements LoginInfoService {

    @Autowired
    private LogininfoMapper logininfoMapper;
    @Override
    public String checkName() {
         logininfoMapper.selectAll();

        return "" ;
    }
}
