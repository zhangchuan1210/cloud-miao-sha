package com.geekq.miaosha.order.mapper;

import com.geekq.miaosha.common.vo.GoodsExtVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 邱润泽
 */
public interface GoodsComposeMapper {

    public List<GoodsExtVo> listGoodsVo();

    public GoodsExtVo getGoodsVoByGoodsId(@Param("goodsId") long goodsId);



}
