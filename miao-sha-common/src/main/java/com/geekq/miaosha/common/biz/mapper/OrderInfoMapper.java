package com.geekq.miaosha.common.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.geekq.miaosha.common.biz.entity.OrderInfo;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zhangc
 * @since 2021-03-28
 */

public interface OrderInfoMapper extends BaseMapper<OrderInfo> {

    public int insert(OrderInfo orderInfo);

    public int closeOrderByOrderInfo();
}
