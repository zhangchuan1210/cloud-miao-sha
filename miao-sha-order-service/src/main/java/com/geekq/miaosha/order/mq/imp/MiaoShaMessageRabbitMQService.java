package com.geekq.miaosha.order.mq.imp;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.alibaba.fastjson.JSONObject;
import com.geekq.miaosha.common.biz.entity.MiaoshaUser;
import com.geekq.miaosha.common.biz.entity.OrderInfo;
import com.geekq.miaosha.order.mq.IMQService;
import com.geekq.miaosha.order.mq.MQConfig;
import com.geekq.miaosha.order.mq.MiaoShaMessage;
import com.geekq.miaosha.order.service.ISecondKillMessageService;
import com.geekq.miaosha.order.service.impl.SecondKillComposeService;
import com.geekq.miaosha.common.utils.StringBeanUtil;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Executable;
import java.util.concurrent.ExecutionException;

/*
* 秒杀消息服务
* */
@Component(value="miaoShaMessageRabbitMQService")
public class MiaoShaMessageRabbitMQService implements IMQService {
    private Log log= LogFactory.get();
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    SecondKillComposeService miaoShaComposeService;
    @Autowired
    ISecondKillMessageService secondKillMessageService;

    @Override
    public String send(Object... params) {
        MiaoshaUser user=(MiaoshaUser) params[0];
        long goodId=(long)params[1];
        MiaoShaMessage mm=new MiaoShaMessage();
        mm.setGoodsId(goodId);
        mm.setUser(user);
        String msg = StringBeanUtil.beanToString(mm);
        log.info("send message:{}"+msg);
        rabbitTemplate.convertAndSend(MQConfig.MIAOSHA_QUEUE,msg);

        return null;
    }

    @Override
    public String receive(Object... paramsJson) {
        return null;
    }


    @RabbitListener(queues=MQConfig.MIAOSHA_QUEUE)
    @RabbitHandler
    public String receive(String in, Channel channel, Message message) throws IOException {
        log.info("receive message:"+in);
        boolean save=false;
        try {
        MiaoShaMessage mm  = (MiaoShaMessage) JSONObject.parseObject(in,MiaoShaMessage.class);
        MiaoshaUser user = mm.getUser();
        long goodsId = mm.getGoodsId();
            //减库存 下订单 写入秒杀订单
            save = miaoShaComposeService.afterSecondKill(user, goodsId, true);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } finally {
            if(! save){//失败，入库保存，定时扫描
                log.info("保存失败消息:{}",in);
                boolean result=secondKillMessageService.saveFailMessage(in,0,"0",0);
            }

        }

        return null;
    }
}
