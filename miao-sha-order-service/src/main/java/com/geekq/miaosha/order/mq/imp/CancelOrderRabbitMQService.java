package com.geekq.miaosha.order.mq.imp;

import com.alibaba.fastjson.JSONObject;
import com.geekq.miaosha.common.biz.entity.OrderInfo;
import com.geekq.miaosha.order.mq.IMQService;
import com.geekq.miaosha.order.mq.MQConfig;
import com.geekq.miaosha.order.service.impl.SecondKillComposeService;
import com.geekq.miaosha.common.utils.StringBeanUtil;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

/*
 * 订单延时服务
 * */
@Component(value = "cancelOrderRabbitMQService")
public class CancelOrderRabbitMQService implements IMQService {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    SecondKillComposeService miaoShaComposeService;
    @Override
    public String send(String msg) {

        rabbitTemplate.convertAndSend(MQConfig.DELAYED_EXCHANGE, MQConfig.DELAY_QUEUE_1, msg, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                int delay_time=5*60*1000;
                message.getMessageProperties().setHeader("x-delay",delay_time);
                return message;
            }
        });

        return null;
    }

    @Override
    @RabbitListener(queues = MQConfig.DELAY_QUEUE_1)
    public String receive(String message) {
        OrderInfo orderDetailVo= StringBeanUtil.stringToBean(message, OrderInfo.class);
        Date expireDate=orderDetailVo.getExpireDate();
        Long id=orderDetailVo.getId();
        Integer status=orderDetailVo.getStatus();
        if(status.equals(0) ){
            miaoShaComposeService.cancelSecondKillOrder(orderDetailVo);
        }

        return null;
    }
   /* @RabbitListener(queues = MQConfig.DELAY_QUEUE_1)
    public String receive(String message, Channel channel) throws IOException {
        Message msg= JSONObject.parseObject(message,Message.class);
        long tag=msg.getMessageProperties().getDeliveryTag();
        OrderInfo orderDetailVo= StringBeanUtil.stringToBean(message, OrderInfo.class);
        Integer status=orderDetailVo.getStatus();
        if(status.equals(0) ){
           boolean delete= miaoShaComposeService.cancelSecondKillOrder(orderDetailVo);
           if(delete){
               channel.basicAck(tag,false);
           }else{
               channel.basicNack(tag,false,true);
           }

        }else{
            channel.basicAck(tag,false);
        }

        return null;
    }*/


}
