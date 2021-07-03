package com.geekq.miaosha.order.service.impl;

import com.geekq.miaosha.common.biz.entity.MiaoshaUser;
import com.geekq.miaosha.common.utils.VerifyCodeUtil;
import com.geekq.miaosha.order.redis.MiaoshaKey;
import com.geekq.miaosha.order.redis.RedisService;
import com.geekq.miaosha.order.service.IVerficateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;

@Service
public class VerficateService implements IVerficateService {
    @Autowired
    RedisService redisService;
    @Override
    public BufferedImage createVerifyCode(MiaoshaUser user, long goodsId) {
        if(user == null || goodsId <=0) {
            return null;
        }
        int rnd=0;
        BufferedImage image= VerifyCodeUtil.createVerifyCodeRegister(rnd);
        redisService.set(MiaoshaKey.getMiaoshaVerifyCode, user.getNickname()+","+goodsId, rnd);
        //输出图片
        return image;
    }

    /**
     * 注册时用的验证码
     * @param verifyCode
     * @return
     */
    @Override
    public boolean checkVerifyCodeRegister(int verifyCode) {
        Integer codeOld = redisService.get(MiaoshaKey.getMiaoshaVerifyCodeRegister,"regitser", Integer.class);
        if(codeOld == null || codeOld - verifyCode != 0 ) {
            return false;
        }
        redisService.delete(MiaoshaKey.getMiaoshaVerifyCode, "regitser");
        return true;
    }

    @Override
    public BufferedImage createVerifyCodeRegister() {
        int rnd=0;
        BufferedImage image=VerifyCodeUtil.createVerifyCodeRegister(rnd);
        redisService.set(MiaoshaKey.getMiaoshaVerifyCodeRegister,"regitser",rnd);
        //输出图片
        return image;
    }


    @Override
    public boolean checkVerifyCode(MiaoshaUser user, long goodsId, int verifyCode) {
        if(user == null || goodsId <=0) {
            return false;
        }
        Integer codeOld = redisService.get(MiaoshaKey.getMiaoshaVerifyCode, user.getNickname()+","+goodsId, Integer.class);
        if(codeOld == null || codeOld - verifyCode != 0 ) {
            return false;
        }
        redisService.delete(MiaoshaKey.getMiaoshaVerifyCode, user.getNickname()+","+goodsId);
        return true;
    }



}
