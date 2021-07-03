package com.geekq.miaosha.order.service;

import com.geekq.miaosha.common.biz.entity.MiaoshaUser;

import java.awt.image.BufferedImage;

public interface IVerficateService {
    BufferedImage createVerifyCode(MiaoshaUser user, long goodsId);

    boolean checkVerifyCodeRegister(int verifyCode);

    BufferedImage createVerifyCodeRegister();

    boolean checkVerifyCode(MiaoshaUser user, long goodsId, int verifyCode);
}
