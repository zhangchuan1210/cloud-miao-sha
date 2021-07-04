package com.geekq.miaosha.order.mq.imp;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.geekq.miaosha.order.mq.IMQService;
import com.geekq.miaosha.order.mq.MQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/*
 * 用户注册消息服务
 * */
@Component(value="registerMessageRabbitMQService")
public class RegisterMessageRabbitMQService implements IMQService {
    private Log  log= LogFactory.get();

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public String send(Object... params) {


        return null;
    }

    @Override
    public String receive(Object... params) {
        return null;
    }
}
