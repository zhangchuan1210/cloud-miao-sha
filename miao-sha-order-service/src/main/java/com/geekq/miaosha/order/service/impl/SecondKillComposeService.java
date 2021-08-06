package com.geekq.miaosha.order.service.impl;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.alibaba.fastjson.JSONObject;
import com.geekq.miaosha.common.biz.entity.MiaoshaOrder;
import com.geekq.miaosha.common.biz.entity.MiaoshaUser;
import com.geekq.miaosha.common.biz.entity.OrderInfo;
import com.geekq.miaosha.common.enums.enums.ResultStatus;
import com.geekq.miaosha.common.utils.MD5Utils;
import com.geekq.miaosha.common.utils.SpringContextUtil;
import com.geekq.miaosha.common.utils.UUIDUtil;
import com.geekq.miaosha.common.vo.GoodsExtVo;
import com.geekq.miaosha.order.init.RunningAfterStartUp;
import com.geekq.miaosha.order.mq.MQServiceFactory;
import com.geekq.miaosha.order.redis.GoodsKey;
import com.geekq.miaosha.order.redis.MiaoshaKey;
import com.geekq.miaosha.order.redis.OrderKey;
import com.geekq.miaosha.order.redis.RedisService;
import com.geekq.miaosha.order.redis.distributelock.RedisDistributeLock;
import com.geekq.miaosha.order.service.ISecondKillComposeService;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.geekq.miaosha.common.enums.enums.ResultStatus.*;

@Service
public class SecondKillComposeService implements ISecondKillComposeService {
	
	@Autowired
	GoodsComposeService goodsComposeService;
	@Autowired
	OrderComposeService orderComposeService;
	@Autowired
	RedisService redisService;

	private Log log= LogFactory.get();

	private AtomicInteger count=new AtomicInteger(0);

	private ConcurrentHashMap<Long,Boolean> localOverMap = new ConcurrentHashMap();

	@HystrixCommand(
			fallbackMethod = "checkBeforeSecondKillFallBack",
			groupKey="beforeSecondKill",
			threadPoolProperties = {
                   @HystrixProperty(name="coreSize",value="2"),
                   @HystrixProperty(name="maxQueueSize",value="3000"),
            },
			commandProperties={
					@HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds",value="1000"),
                    @HystrixProperty(name="metrics.rollingStats.timeInMilliseconds",value="16000"),
					// 最小请求数
					@HystrixProperty(name = "circuitBreaker.requestVolumeThreshold",value = "600"),
					// 失败次数占请求的50%
					@HystrixProperty(name = "circuitBreaker.errorThresholdPercentage",value = "50"),
					@HystrixProperty(name="circuitBreaker.sleepWindowInMilliseconds",value= "6000")
			}
	)
	public ResultStatus checkBeforeSecondKill(MiaoshaUser user, String path, long goodsId){
		log.info("check second kill.....");

		/*if (user == null) {
			checkResult=SESSION_ERROR.getMessage();
			return checkResult;
		}*/
		/*
		* 本地库存校验，挡住大部分请求
		* */
		boolean over=localOverMap.get(goodsId);
		if(over){
			return MIAO_SHA_OVER;

		}
		/*
		 * 校验秒杀url地址，防止刷流量
		 * */
		/*String pathValue=redisService.get(MiaoshaKey.getMiaoshaPath, ""+user.getNickname() + "_"+ goodsId,String.class);
		if(! pathValue.equals(path)){
			return checkResult;
		}*/

		return MIAO_SHA_CHECK_SUCCESS;
	}

	public ResultStatus synProcessSecondKill(MiaoshaUser user, String path, long goodsId, boolean syncOrder) throws InterruptedException {
		ResultStatus checkResult=this.doSecondKill(user,path,goodsId);
		if(MIAOSHA_SUCESS.getCode()==(checkResult.getCode())){
			this.afterSecondKill(user,goodsId,syncOrder);

		}
		return checkResult;
	}

	public ResultStatus asyProcessSecondKill(MiaoshaUser user, String path, long goodsId, boolean syncOrder){
		JSONObject paramsJson=new JSONObject();
		paramsJson.put("user",user);
		paramsJson.put("path",path);
		paramsJson.put("goodsId",goodsId);
		MQServiceFactory.create("rabbitmq","checkmiaosha").send(paramsJson.toString());
		return MIAOSHA_DOING;
	}

    @HystrixCommand(fallbackMethod = "doSecondKillFallback",
			groupKey="doSecondKill",
			threadPoolProperties = {
					@HystrixProperty(name="coreSize",value="2"),
					@HystrixProperty(name="maxQueueSize",value="300"),
			},
			commandProperties={
					@HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds",value="1000"),
					@HystrixProperty(name="metrics.rollingStats.timeInMilliseconds",value="16000"),
					// 最小请求数
					@HystrixProperty(name = "circuitBreaker.requestVolumeThreshold",value = "300"),
					// 失败次数占请求的50%
				    @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage",value = "50"),
					@HystrixProperty(name="circuitBreaker.sleepWindowInMilliseconds",value= "1000")
			}
	)
	public ResultStatus doSecondKill(MiaoshaUser user, String path, long goodsId){
        log.info("do second kill.......");
		String checkResult="success";

        /*判断是否重复秒杀
         *
         * */
        MiaoshaOrder miaoshaOrder= redisService.get(OrderKey.getMiaoshaOrderByUidGid,""+user.getNickname()+"_"+goodsId,MiaoshaOrder.class) ;;
        if(null !=miaoshaOrder){
            return REPEATE_MIAOSHA;
        }

        /*
          有人说先校验库存，再扣减库存，也可以直接使用decr操作，通过返回值判断是否售罄,这样会导致缓存的库存数一直递减。
          如果先查再减需要保证两者操作的原子性，可以lua脚本
         */
        List<String> keys=new ArrayList();
        keys.add(GoodsKey.getMiaoshaGoodsStock.getPrefix()+"" + goodsId);
		Long stock=(Long) redisService.execScript("lua/secondkillstock.lua",Long.class, keys);
        if (stock < 0) {
            localOverMap.put(goodsId, true);
			JSONObject jsonObject=new JSONObject();
			jsonObject.put("goodsId",goodsId);
			jsonObject.put("over",true);
			redisService.publishMessage(MiaoshaKey.isGoodsOver.getPrefix(),jsonObject.toString());

            return MIAO_SHA_OVER;
        }
        return MIAOSHA_SUCESS;
    }

        /*
        生成秒杀订单信息
        如果秒杀商品库存较少时，可以直接访问数据库
        如果秒杀商品库存量大，就需要使用消息对列队请求进行削峰
        *
        * */
		public boolean afterSecondKill(MiaoshaUser user, Long goodsId, boolean syncOrder) throws InterruptedException {
			boolean result = false;
			int expireTime = 30;

			if (syncOrder) {//同步订单

				result = this.syncFinishSecondKillInfoToDB(user, goodsId, expireTime);

			} else {//订单不同步,异步请求

				result = this.asyncFinishSecondKillInfoToDB(user, goodsId);
			}

			return result;
		}


	/*取消订单时，要注意数据库和缓存的数据最终一致性
	 *最好使用分布式事务
	 * */
	@Transactional
	@RedisDistributeLock(lockKey = "cancelorder",expireTime = 1000)
	public boolean cancelSecondKillOrder(OrderInfo orderInfo) {
		boolean deleteOrder=false;
		GoodsExtVo goods=new GoodsExtVo();
		goods.setId(orderInfo.getGoodsId());
		goods.setGoodsStock(orderInfo.getGoodsCount());
		boolean addSuccessOrNot=goodsComposeService.addStock(goods);
		if(addSuccessOrNot){
			redisService.incr(GoodsKey.getMiaoshaGoodsStock, "" + goods.getId());
			redisService.delete(OrderKey.getMiaoshaOrderByUidGid,""+orderInfo.getUserId()+"_"+orderInfo.getGoodsId());
			deleteOrder= orderComposeService.deleteOrder(orderInfo.getId());

		}
		return deleteOrder;
	}

	public long getSecondKillResult(Long userId, long goodsId) {
		MiaoshaOrder order = redisService.get(OrderKey.getMiaoshaOrderByUidGid,""+userId+"_"+goodsId,MiaoshaOrder.class) ;;
		if(order != null) {//秒杀成功
			return order.getOrderId();
		}else {
			boolean cacheStock=localOverMap.get(goodsId);
			if(cacheStock){
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


	public ResultStatus checkBeforeSecondKillFallBack(MiaoshaUser user, String path, long goodsId){
		log.error("触发秒杀校验熔断。。。。。");
		return MIAOSHA_FAIL;
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
	private boolean syncFinishSecondKillInfoToDB(MiaoshaUser user, Long goodsId, int expireTime) throws InterruptedException {
		log.info("finishSecondKill.......");
		boolean orderInfo=true;
		GoodsExtVo goods = goodsComposeService.getGoodsVoByGoodsId(goodsId);
        int stock = goods.getStockCount();
        if (stock > 0) {
            //判断是否已经秒杀到了
            MiaoshaOrder order = redisService.get(OrderKey.getMiaoshaOrderByUidGid, "" + Long.valueOf(user.getNickname()) + "_" + goodsId, MiaoshaOrder.class);
            if (order == null) {
                orderInfo = SpringContextUtil.getBean(SecondKillComposeService.class).saveAndDelaySecondKillOrder(user, goods, expireTime);

            }
        }
        return orderInfo;
	}

	/*
	 * 异步处理秒杀订单，异步处理不建议查询数据库，会是db崩溃
	 * */
	private boolean asyncFinishSecondKillInfoToDB(MiaoshaUser user, Long goodsId){
		boolean result=false;
		try{
		    MQServiceFactory.create("rabbitmq","miaoshamessage").send(user,goodsId);
			result=true;
		}catch(Exception e){
            e.printStackTrace();
            log.error(e);
		}
		return result;
	}
    @Transactional
    public boolean saveAndDelaySecondKillOrder(MiaoshaUser user, GoodsExtVo goodsVo, int expireTime) {
        boolean result = false;
        OrderInfo orderInfo = this.reduceStockAndCreateOrderToDB(user, goodsVo, expireTime);
        if (null != orderInfo) {
            String orderJsonStr = JSONObject.toJSONString(orderInfo);
            MQServiceFactory.create("rabbitmq", "cancelorder").send(orderJsonStr);
            result = true;
        }
        return result;
    }

	/*扣减数据库库存，并生成订单信息
	 *此处应使用分布式事务进行控制,暂时使用本地事务
	 * 失败需要重试,每次重试会不会清掉全局事务，这个地方分布式事务需要验证是否有问题
	 * */

	private OrderInfo reduceStockAndCreateOrderToDB(MiaoshaUser user, GoodsExtVo goodsVo, int expireTime)  {
		int retry = 2;
		OrderInfo orderInfo = null;
		boolean retrySuccess = false;
		while (!retrySuccess && retry-- > 0) {
			log.info("重试："+retry);
			try{
				boolean success = goodsComposeService.reduceStock(goodsVo);
				if (success) {
					orderInfo = orderComposeService.createOrderInfoAndMIaoShaOrder(user, goodsVo, expireTime);
				}
				if (null != orderInfo) {
					retrySuccess = true;
					continue;
				}
			}catch (Exception e){
                e.printStackTrace();
			}
			try {
				Thread.sleep(60);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (!retrySuccess) {
			log.warn("重试不成功，丢死信息请手动后台处理：{}，{}，{}", JSONObject.toJSONString(user), JSONObject.toJSONString(goodsVo), expireTime);
            System.out.println(count.getAndIncrement());
			throw new RuntimeException();
		}

		return orderInfo;
	}



}
