package com.geekq.miaosha.order.mq.imp;

import com.alibaba.fastjson.JSONObject;
import com.geekq.miaosha.common.biz.entity.MiaoshaUser;
import com.geekq.miaosha.common.biz.entity.OrderInfo;
import com.geekq.miaosha.common.enums.enums.ResultStatus;
import com.geekq.miaosha.order.mq.IMQService;
import com.geekq.miaosha.order.mq.MQConfig;
import com.geekq.miaosha.order.service.impl.SecondKillComposeService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.geekq.miaosha.common.enums.enums.ResultStatus.MIAOSHA_SUCESS;

@Component(value="checkMiaoShaRabbitMQService")
public class CheckMiaoShaRabbitMQService  implements IMQService {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    SecondKillComposeService miaoShaComposeService;
    @Override
    public String send(Object... params) {
        rabbitTemplate.convertAndSend(MQConfig.CHECK_MIAOSHA_EXCHANGE,"checkmiaosha" , params);
        return "排队秒杀中，请稍后。。。。。";
    }

    @Override
    @RabbitListener(queues= MQConfig.CHECK_MIAOSHA_QUEUE)
    public String receive(Object... params) {
        String paramsJson=(String) params[0];
        JSONObject param=JSONObject.parseObject(paramsJson);
        ResultStatus checkResult=miaoShaComposeService.doSecondKill(JSONObject.parseObject(param.get("user").toString(),MiaoshaUser.class),param.getString("path"),param.getLong("goodsId"));
        if(MIAOSHA_SUCESS.getCode()==(checkResult.getCode())){
            try {
                boolean orderInfo= miaoShaComposeService.afterSecondKill(JSONObject.parseObject(param.get("user").toString(),MiaoshaUser.class),param.getLong("goodsId"),true);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
