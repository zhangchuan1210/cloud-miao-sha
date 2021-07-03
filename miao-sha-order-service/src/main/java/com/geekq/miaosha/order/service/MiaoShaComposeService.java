package com.geekq.miaosha.order.service;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.alibaba.fastjson.JSONObject;
import com.geekq.miaosha.common.biz.entity.MiaoshaOrder;
import com.geekq.miaosha.common.biz.entity.MiaoshaUser;
import com.geekq.miaosha.common.biz.entity.OrderInfo;
import com.geekq.miaosha.order.init.RunningAfterStartUp;
import com.geekq.miaosha.order.mq.MQServiceFactory;
import com.geekq.miaosha.order.mq.MiaoShaMessage;
import com.geekq.miaosha.order.redis.*;
import com.geekq.miaosha.order.redis.distributelock.RedisDistributeLock;
import com.geekq.miaosha.common.utils.StringBeanUtil;
import com.geekq.miaosha.common.utils.MD5Utils;
import com.geekq.miaosha.common.utils.UUIDUtil;
import com.geekq.miaosha.common.vo.GoodsExtVo;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;

import static com.geekq.miaosha.common.enums.enums.ResultStatus.*;

@Service
public class MiaoShaComposeService{
	
	@Autowired
	GoodsComposeService goodsComposeService;
	@Autowired
	OrderComposeService orderComposeService;
	@Autowired
	RedisService redisService;

	private Log log= LogFactory.get();

	private HashMap<Long, Boolean> localOverMap = new HashMap<Long, Boolean>();

	@HystrixCommand(
			fallbackMethod = "checkBeforeSecondKillFallBack",
			groupKey="beforeSecondKill",
			threadPoolProperties = {
                   @HystrixProperty(name="coreSize",value="2"),
                   @HystrixProperty(name="maxQueueSize",value="30"),
            },
			commandProperties={
					@HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds",value="1000"),
                    @HystrixProperty(name="metrics.rollingStats.timeInMilliseconds",value="16000"),
					// 最小请求数
					@HystrixProperty(name = "circuitBreaker.requestVolumeThreshold",value = "3"),
					// 失败次数占请求的50%
					@HystrixProperty(name = "circuitBreaker.errorThresholdPercentage",value = "50"),
					@HystrixProperty(name="circuitBreaker.sleepWindowInMilliseconds",value= "6000")
			}
	)
	public String checkBeforeSecondKill(MiaoshaUser user, String path, long goodsId){
		String checkResult="success";
		/*if (user == null) {
			checkResult=SESSION_ERROR.getMessage();
			return checkResult;
		}*/
		/*
		* 本地库存校验，挡住大部分请求
		* */
		//boolean over=localOverMap.get(goodsId);
		boolean over=false;
		if(over){
			checkResult=MIAO_SHA_OVER.getMessage();
			return checkResult;
		}
		/*
		 * 校验秒杀url地址，防止刷流量
		 * */
		/*String pathValue=redisService.get(MiaoshaKey.getMiaoshaPath, ""+user.getNickname() + "_"+ goodsId,String.class);
		if(! pathValue.equals(path)){
			return checkResult;
		}*/
		log.info("check second kill.....");
		return checkResult;
	}


	public String synProcessSecondKill(MiaoshaUser user, String path, long goodsId, boolean syncOrder){
		String checkResult=this.doSecondKill(user,path,goodsId);
		if("success".equals(checkResult)){
			OrderInfo orderInfo= this.afterSecondKill(user,goodsId,syncOrder);
		}else{

		}
		return checkResult;
	}

	public String asyProcessSecondKill(MiaoshaUser user, String path, long goodsId, boolean syncOrder){
		JSONObject paramsJson=new JSONObject();
		paramsJson.put("user",user);
		paramsJson.put("path",path);
		paramsJson.put("goodsId",goodsId);
		String checkResult=MQServiceFactory.create("rabbitmq","checkmiaosha").send(paramsJson.toString());
		return checkResult;
	}

    @HystrixCommand(fallbackMethod = "doSecondKillFallback",
			groupKey="doSecondKill",
			threadPoolProperties = {
					@HystrixProperty(name="coreSize",value="2"),
					@HystrixProperty(name="maxQueueSize",value="30"),
			},
			commandProperties={
					@HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds",value="1000"),
					@HystrixProperty(name="metrics.rollingStats.timeInMilliseconds",value="16000"),
					// 最小请求数
					@HystrixProperty(name = "circuitBreaker.requestVolumeThreshold",value = "3"),
					// 失败次数占请求的50%
				    @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage",value = "50"),
					@HystrixProperty(name="circuitBreaker.sleepWindowInMilliseconds",value= "10000")
			}
	)
	public String doSecondKill(MiaoshaUser user, String path, long goodsId){
        log.info("do second kill.......");
		String checkResult="success";

        /*判断是否重复秒杀
         *
         * */
        MiaoshaOrder miaoshaOrder= redisService.get(OrderKey.getMiaoshaOrderByUidGid,""+user.getId()+"_"+goodsId,MiaoshaOrder.class) ;;
        if(null !=miaoshaOrder){
            checkResult= REPEATE_MIAOSHA.getMessage();
            return checkResult;
        }

        /*
有人说先校验库存，再扣减库存，也可以直接使用decr操作，通过返回值判断是否售罄,这样会导致缓存的库存数一直递减。
如果先查再减需要保证两者操作的原子性，可以lua脚本
         */
        Long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock, "" + goodsId);
        if (stock < 0) {
            localOverMap.put(goodsId, true);
            checkResult=MIAO_SHA_OVER.getMessage();
            return checkResult;
        }
        return checkResult;
    }


	//生成秒杀订单信息
        /*如果秒杀商品库存较少时，可以直接访问数据库
        如果秒杀商品库存量大是，就需要使用消息对列队请求进行削峰
        *
        * */
	public OrderInfo afterSecondKill(MiaoshaUser user, Long goodsId, boolean syncOrder){
		OrderInfo orderInfo=new OrderInfo();
		int expireTime=30;
		if(syncOrder){//同步订单
			orderInfo=this.syncFinishSecondKillInfoToDB(user,goodsId,expireTime);
		}else{//订单不同步,异步请求
			orderInfo=this.asyncFinishSecondKillInfoToDB(user,goodsId);
		}
		return orderInfo;
	}


	/*取消订单时，要注意数据库和缓存的数据最终一致性
	 *
	 * */
	@Transactional
	@RedisDistributeLock(lockKey = "cancelorder",expireTime = 1000)
	public boolean cancelSecondKillOrder(OrderInfo orderInfo) {
		boolean deleteOrder=true;
		GoodsExtVo goods=new GoodsExtVo();
		goods.setId(orderInfo.getGoodsId());
		goods.setGoodsStock(orderInfo.getGoodsCount());
		//最好使用分布式锁,对资源的竞争修改导致数据不正确
		//这里应该保证原子性
		/*synchronized (this){
			boolean addSuccessOrNot=goodsComposeService.addStock(goods);
			if(addSuccessOrNot){
				redisService.incr(GoodsKey.getMiaoshaGoodsStock, "" + goods.getId());
				deleteOrder= orderComposeService.deleteOrder(orderInfo.getId());
			}
		}*/

		boolean addSuccessOrNot=goodsComposeService.addStock(goods);
		if(addSuccessOrNot){
			redisService.incr(GoodsKey.getMiaoshaGoodsStock, "" + goods.getId());
			deleteOrder= orderComposeService.deleteOrder(orderInfo.getId());
		}
		return deleteOrder;
	}

	public long getSecondKillResult(Long userId, long goodsId) {
		MiaoshaOrder order = redisService.get(OrderKey.getMiaoshaOrderByUidGid,""+userId+"_"+goodsId,MiaoshaOrder.class) ;;
		if(order != null) {//秒杀成功
			return order.getOrderId();
		}else {
			long cacheStock=redisService.get(GoodsKey.getMiaoshaGoodsStock, "" + goodsId,Long.class);

			if(cacheStock>0){
				return 0; //还有库存继续轮训
			}else{
				return -1; //没有库存，返回失败
			}
		}
	}

	public boolean checkPath(MiaoshaUser user, long goodsId, String path) {
		if(user == null || path == null) {
			return false;
		}
		String pathOld = redisService.get(MiaoshaKey.getMiaoshaPath, ""+user.getNickname() + "_"+ goodsId, String.class);
		return path.equals(pathOld);
	}

	public String createSecondKillPath(MiaoshaUser user, long goodsId) {
		if(user == null || goodsId <=0) {
			return null;
		}
		String str = MD5Utils.md5(UUIDUtil.uuid()+"123456");
		redisService.set(MiaoshaKey.getMiaoshaPath, ""+user.getNickname() + "_"+ goodsId, str);
		return str;
	}


	public String checkBeforeSecondKillFallBack(MiaoshaUser user, String path, long goodsId){
		log.error("触发秒杀校验熔断。。。。。");
		return MIAOSHA_FAIL.getMessage();
	}

	public String doSecondKillFallback(MiaoshaUser user, String path, long goodsId){
		log.error("触发秒杀中熔断。。。。。");
		return MIAOSHA_FAIL.getMessage();
	}

	@RunningAfterStartUp
	public void prepareSecondKillStockToRedis(){
		log.info("second kill save to redis.....");
		List<GoodsExtVo> goodsList = goodsComposeService.listGoodsVo();
		if (goodsList == null) {
			return;
		}
		for (GoodsExtVo goods : goodsList) {
			redisService.set(GoodsKey.getMiaoshaGoodsStock, "" + goods.getId(), goods.getStockCount());
			localOverMap.put(goods.getId(), false);
		}
	}

	/*
	 * 同步秒杀订单。
	 * 订单量较小时，可查询数据库。
	 * 可根据订单量设置是否读写分离
	 * */
	private OrderInfo syncFinishSecondKillInfoToDB(MiaoshaUser user, Long goodsId, int expireTime){
		OrderInfo orderInfo=new OrderInfo();
		GoodsExtVo goods = goodsComposeService.getGoodsVoByGoodsId(goodsId);
		int stock = goods.getStockCount();
		if(stock <= 0) {
			return null;
		}
		//判断是否已经秒杀到了
		MiaoshaOrder order = redisService.get(OrderKey.getMiaoshaOrderByUidGid,""+Long.valueOf(user.getNickname())+"_"+goodsId,MiaoshaOrder.class) ;;
		if(order != null) {
			return  null;
		}
		orderInfo=this.reduceStockAndCreateOrderToDB(user,goods,expireTime);
		return orderInfo;
	}

	/*
	 * 异步处理秒杀订单，异步处理不建议查询数据库，会是db崩溃
	 * */
	private OrderInfo asyncFinishSecondKillInfoToDB(MiaoshaUser user, Long goodsId){
		OrderInfo orderInfo=new OrderInfo();
		MiaoShaMessage mm = new MiaoShaMessage();
		mm.setGoodsId(goodsId);
		mm.setUser(user);
		String msg = StringBeanUtil.beanToString(mm);
		MQServiceFactory.create("rabbit","miaoshamessage").send(msg);
		return orderInfo;
	}
	/*扣减数据库库存，并生成订单信息
	 *
	 * */
	private OrderInfo reduceStockAndCreateOrderToDB(MiaoshaUser user, GoodsExtVo goodsVo, int expireTime){
		OrderInfo orderInfo=new OrderInfo();
		boolean success=false;
		success = goodsComposeService.reduceStock(goodsVo);
		if(success){
			orderInfo= orderComposeService.createOrderInfoAndMIaoShaOrder(user,goodsVo,expireTime);
		}else{

			/*要对失败原因进行具体分析，不一定是因为库存不足导致的失败*/

			//redisService.incr(GoodsKey.getMiaoshaGoodsStock, "" + goodsVo.getId());
			setGoodsOver(goodsVo.getId());
		}
		return orderInfo;
	}

	private void setGoodsOver(Long goodsId) {
		redisService.set(MiaoshaKey.isGoodsOver, ""+goodsId, true);
	}

	private boolean getGoodsOver(long goodsId) {
		return redisService.exists(MiaoshaKey.isGoodsOver, ""+goodsId);
	}


}
