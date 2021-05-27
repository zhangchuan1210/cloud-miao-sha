package com.geekq.miaosha.common.biz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.geekq.miaosha.common.biz.entity.OrderInfo;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zhangc
 * @since 2021-03-28
 */
public interface OrderInfoService extends IService<OrderInfo> {

    boolean closeOrderByOrderInfo(int id);
    List<OrderInfo> selectOrderStatusByCreateTime(Integer valueOf, String dateToStr);
}
