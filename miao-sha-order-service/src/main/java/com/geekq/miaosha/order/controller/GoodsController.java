package com.geekq.miaosha.order.controller;


import com.geekq.miaosha.common.biz.entity.MiaoshaUser;
import com.geekq.miaosha.common.vo.GoodsVoOrder;
import com.geekq.miaosha.order.interceptor.RequireLogin;
import com.geekq.miaosha.order.redis.GoodsKey;
import com.geekq.miaosha.order.redis.RedisService;
import com.geekq.miaosha.order.service.impl.GoodsComposeService;
import com.geekq.miaosha.order.service.impl.MiaoShaUserComposeService;
import com.geekq.miaosha.common.enums.resultbean.ResultGeekQ;
import com.geekq.miaosha.common.vo.GoodsDetailVo;
import com.geekq.miaosha.common.vo.GoodsExtVo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@RequestMapping("/goods")
public class GoodsController extends BaseController {
    private static Logger log = LoggerFactory.getLogger(GoodsController.class);

    @Autowired
    private MiaoShaUserComposeService userService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private GoodsComposeService goodsComposeService;



    @Autowired
    ThymeleafViewResolver viewResolver;

    @Autowired
    ApplicationContext applicationContext;

    /**
     * QPS:1267 load:15 mysql
     * 5000 * 10
     * QPS:2884, load:5
     * */
    @RequireLogin(seconds = 5, maxCount = 5, needLogin = true)
    @RequestMapping(value="/to_list", produces="text/html")
    @ResponseBody
    public String list(HttpServletRequest request, HttpServletResponse response, Model model, MiaoshaUser user)  {
        model.addAttribute("user", user);

        List<GoodsExtVo> goodsList= goodsComposeService.listGoodsVo();

        //订单服务化接口 miaosha-order
       /* ResultGeekQOrder<List<GoodsVoOrder>> resultGoods = goodsServiceRpc.listGoodsVo();

        if(!AbstractResultOrder.isSuccess(resultGoods)){
           throw new GlobleException(ResultStatus.SYSTEM_ERROR);
        }
        List<GoodsVoOrder> goodsList = resultGoods.getData();
        */

        model.addAttribute("goodsList", goodsList);
        return render(request,response,model,"goods_list", GoodsKey.getGoodsList,"");
    }

    @RequestMapping(value="/to_detail2/{goodsId}",produces="text/html")
    @ResponseBody
    public String detail2(HttpServletRequest request, HttpServletResponse response, Model model, MiaoshaUser user,
                          @PathVariable("goodsId")long goodsId) {
        model.addAttribute("user", user);

        //取缓存
        String html = redisService.get(GoodsKey.getGoodsDetail, ""+goodsId, String.class);
        if(!StringUtils.isEmpty(html)) {
            return html;
        }
        //手动渲染
        GoodsExtVo goods = goodsComposeService.getGoodsVoByGoodsId(goodsId);
        /**
         * rpc服务化接口
         */
        //ResultGeekQOrder<GoodsVoOrder> goodsVoOrderResultGeekQOrder = goodsServiceRpc.getGoodsVoByGoodsId(goodsId);
       /* if(!AbstractResultOrder.isSuccess(goodsVoOrderResultGeekQOrder)){
            throw new GlobleException(ResultStatus.SESSION_ERROR);
        }*/
        model.addAttribute("goods", goods);

        long startAt = goods.getStartDate().getTime();
        long endAt = goods.getEndDate().getTime();
        long now = System.currentTimeMillis();

        int miaoshaStatus = 0;
        int remainSeconds = 0;
        if(now < startAt ) {//秒杀还没开始，倒计时
            miaoshaStatus = 0;
            remainSeconds = (int)((startAt - now )/1000);
        }else  if(now > endAt){//秒杀已经结束
            miaoshaStatus = 2;
            remainSeconds = -1;
        }else {//秒杀进行中
            miaoshaStatus = 1;
            remainSeconds = 0;
        }
        model.addAttribute("miaoshaStatus", miaoshaStatus);
        model.addAttribute("remainSeconds", remainSeconds);
//        return "goods_detail";

        WebContext ctx = new WebContext(request, response,
                request.getServletContext(), request.getLocale(), model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goods_detail", ctx);

//        WebContext ctx = new WebContext(request,response,
//                request.getServletContext(),request.getLocale(),
//                model.asMap(), applicationContext );

//        html = viewResolver.getTemplateEngine().process("goods_detail", ctx);
        if(!StringUtils.isEmpty(html)) {
            redisService.set(GoodsKey.getGoodsDetail, ""+goodsId, html);
        }
        return html;
    }

    /**
     * 数据库很少使用long的　，　id 正常使一般使用　snowflake 分布式自增id
     * @param model
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value="/detail/{goodsId}")
    @ResponseBody
    public ResultGeekQ<GoodsDetailVo> detail(HttpServletRequest request, HttpServletResponse response, Model model, MiaoshaUser user,
                                             @PathVariable("goodsId")long goodsId) {
        ResultGeekQ<GoodsDetailVo> result = ResultGeekQ.build();



        GoodsExtVo goodsVo= goodsComposeService.getGoodsVoByGoodsId(goodsId);
        GoodsVoOrder goods=new GoodsVoOrder();
        BeanUtils.copyProperties(goodsVo,goods);


        /**
         * 服务化rpc接口
         */
       /* ResultGeekQOrder<GoodsVoOrder> goodsVoOrderResultGeekQOrder = goodsServiceRpc.getGoodsVoByGoodsId(goodsId);
        if(!AbstractResultOrder.isSuccess(goodsVoOrderResultGeekQOrder)){
            throw new GlobleException(ResultStatus.SESSION_ERROR);
        }

        GoodsVoOrder goods = goodsVoOrderResultGeekQOrder.getData();*/




        long startAt = goods.getStartDate().getTime();
        long endAt = goods.getEndDate().getTime();
        long now = System.currentTimeMillis();
        int miaoshaStatus = 0;
        int remainSeconds = 0;
        if(now < startAt ) {//秒杀还没开始，倒计时
            miaoshaStatus = 0;
            remainSeconds = (int)((startAt - now )/1000);
        }else  if(now > endAt){//秒杀已经结束
            miaoshaStatus = 2;
            remainSeconds = -1;
        }else {//秒杀进行中
            miaoshaStatus = 1;
            remainSeconds = 0;
        }
        GoodsDetailVo vo = new GoodsDetailVo();
        vo.setGoods(goods);
        vo.setUser(user);
        vo.setRemainSeconds(remainSeconds);
        vo.setMiaoshaStatus(miaoshaStatus);
        result.setData(vo);
        return result;
    }
}
