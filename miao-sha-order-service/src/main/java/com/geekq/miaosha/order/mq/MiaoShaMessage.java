package com.geekq.miaosha.order.mq;


import com.geekq.miaosha.common.biz.entity.MiaoshaUser;

public class MiaoShaMessage {
	private MiaoshaUser user;
	private long goodsId;
	public MiaoshaUser getUser() {
		return user;
	}
	public void setUser(MiaoshaUser user) {
		this.user = user;
	}
	public long getGoodsId() {
		return goodsId;
	}
	public void setGoodsId(long goodsId) {
		this.goodsId = goodsId;
	}
}
