package com.geekq.miaosha.order.service.impl;




import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.geekq.miaosha.common.biz.entity.MiaoshaGoods;
import com.geekq.miaosha.common.biz.service.MiaoshaGoodsService;
import com.geekq.miaosha.order.mapper.GoodsComposeMapper;
import com.geekq.miaosha.common.vo.GoodsExtVo;
import com.geekq.miaosha.order.service.IGoodsComposeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GoodsComposeService implements IGoodsComposeService {
	
	@Autowired
	GoodsComposeMapper goodsComposeMapper;
	@Autowired
	private MiaoshaGoodsService miaoshaGoodsService;




	public List<GoodsExtVo> listGoodsVo(){
		return goodsComposeMapper.listGoodsVo();
	}

	public GoodsExtVo getGoodsVoByGoodsId(long goodsId) {
		return goodsComposeMapper.getGoodsVoByGoodsId(goodsId);
	}

	//@Transactional(propagation=Propagation.REQUIRED)
	public boolean reduceStock(GoodsExtVo goods) {

		UpdateWrapper<MiaoshaGoods> temp=new UpdateWrapper<>();
		temp.lambda().eq(MiaoshaGoods::getGoodsId,goods.getId())
				      .setSql("stock_count=stock_count-1");
		return miaoshaGoodsService.update(temp);
	}


    public boolean addStock(GoodsExtVo goods) throws RuntimeException{

		UpdateWrapper<MiaoshaGoods> updateWrapper=new UpdateWrapper<>();
		updateWrapper.lambda().eq(MiaoshaGoods::getGoodsId,goods.getId())
				              .setSql("stock_count=stock_count+1");
        return miaoshaGoodsService.update(updateWrapper);

    }
}
