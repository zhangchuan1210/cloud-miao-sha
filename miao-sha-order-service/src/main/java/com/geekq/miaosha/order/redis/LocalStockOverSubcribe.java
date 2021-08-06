package com.geekq.miaosha.order.redis;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

public class LocalStockOverSubcribe implements MessageListener {


    @Override
    public void onMessage(Message message, byte[] bytes) {

        System.out.println("接收订阅消息......!");

    }
}
