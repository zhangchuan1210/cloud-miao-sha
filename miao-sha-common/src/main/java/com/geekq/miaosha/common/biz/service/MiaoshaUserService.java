package com.geekq.miaosha.common.biz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.geekq.miaosha.common.biz.entity.MiaoshaUser;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zhangc
 * @since 2021-03-29
 */
public interface MiaoshaUserService extends IService<MiaoshaUser> {

    int getCountByUserName(String userName, int usertypeNormal);

    MiaoshaUser getByNickname(String nickName);

    boolean update(MiaoshaUser toBeUpdate);


}
