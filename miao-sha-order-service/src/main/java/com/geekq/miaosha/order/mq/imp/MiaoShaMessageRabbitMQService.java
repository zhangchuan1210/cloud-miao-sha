package com.geekq.miaosha.order.mq.imp;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.geekq.miaosha.common.biz.entity.MiaoshaUser;
import com.geekq.miaosha.order.mq.IMQService;
import com.geekq.miaosha.order.mq.MQConfig;
import com.geekq.miaosha.order.mq.MiaoShaMessage;
import com.geekq.miaosha.order.service.MiaoShaComposeService;
import com.geekq.miaosha.common.utils.StringBeanUtil;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/*
* 秒杀消息服务
* */
@Component(value="miaoShaMessageRabbitMQService")
public class MiaoShaMessageRabbitMQService implements IMQService {
    private Log log= LogFactory.get();
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    MiaoShaComposeService miaoShaComposeService;

    @Override
    public String send(String msg) {

        log.info("send message:"+msg);
        rabbitTemplate.convertAndSend(MQConfig.MIAOSHA_QUEUE, msg);

        return null;
    }

    @Override
    @RabbitListener(queues=MQConfig.MIAOSHA_QUEUE)
    public String receive(String message) {
        log.info("receive message:"+message);
        MiaoShaMessage mm  = StringBeanUtil.stringToBean(message, MiaoShaMessage.class);
        MiaoshaUser user = mm.getUser();
        long goodsId = mm.getGoodsId();
        //减库存 下订单 写入秒杀订单
        miaoShaComposeService.afterSecondKill(user, goodsId,true);
        return null;
    }
}
