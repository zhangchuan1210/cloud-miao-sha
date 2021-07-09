package com.geekq.miaosha.order.controller;

import com.geekq.miaosha.common.biz.entity.MiaoshaUser;
import com.geekq.miaosha.common.utils.SpringContextUtil;
import com.geekq.miaosha.common.vo.GoodsExtVo;
import com.geekq.miaosha.order.interceptor.RequireLogin;
import com.geekq.miaosha.order.redis.limiter.RedisLimiter;
import com.geekq.miaosha.order.service.*;
import com.geekq.miaosha.common.enums.resultbean.ResultGeekQ;
import com.geekq.miaosha.order.service.impl.GoodsComposeService;
import com.geekq.miaosha.order.service.impl.MiaoShaUserComposeService;
import com.geekq.miaosha.order.service.impl.SecondKillComposeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.Random;

import static com.geekq.miaosha.common.enums.enums.ResultStatus.*;


@Controller
@RequestMapping("/miaosha")
@Api(tags = "秒杀服务")
public class MiaoshaController {

    private static Logger logger = LoggerFactory.getLogger(MiaoshaController.class);

    private boolean synchSecondKill =true;
    @Autowired
    MiaoShaUserComposeService userService;

    @Autowired
    SecondKillComposeService miaoShaComposeService;

    @Autowired
    private IVerficateService iVerficateService;

    /**
     * QPS:1306
     * 5000 * 10
     * get　post get 幂等　从服务端获取数据　不会产生影响　　post 对服务端产生变化
     * 此方法并发量太低
     */
    @RequireLogin(seconds = 5, maxCount = 5, needLogin = true)
    @RequestMapping(value="/{path}/do_miaosha", method= RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value="秒杀商品")
    @RedisLimiter(scriptLocation = "lua/redislimit.lua")
    public ResultGeekQ<String> miaosha(Model model, MiaoshaUser user, @PathVariable("path") String path,
                                        @RequestParam("goodsId") long goodsId) throws InterruptedException {
        ResultGeekQ<String> result = ResultGeekQ.build();
        user=new MiaoshaUser();
        long id= new Random().nextLong();
        user.setId(id);
        user.setNickname("13640250671");
        String checkResult=miaoShaComposeService.checkBeforeSecondKill(user, path, goodsId);
        if("success".equals(checkResult)){
            if(synchSecondKill){
               checkResult=miaoShaComposeService.synProcessSecondKill(user, path, goodsId,false);

            }else{
            checkResult= miaoShaComposeService.asyProcessSecondKill(user, path, goodsId,false);

            }
        }
        result.setData(checkResult);
        return result;
    }


    /**
     * 秒杀之后，前段会回调获取秒杀结果
     * orderId：成功
     * -1：秒杀失败
     * 0： 排队中
     */
    @RequireLogin(seconds = 5, maxCount = 5, needLogin = true)
    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value="获取秒杀结果")
    public ResultGeekQ<Long> miaoshaResult(Model model, MiaoshaUser user,
                                           @RequestParam("goodsId") long goodsId) {
        ResultGeekQ<Long> result = ResultGeekQ.build();
        if (user == null) {
            result.withError(SESSION_ERROR.getCode(), SESSION_ERROR.getMessage());
            return result;
        }
        model.addAttribute("user", user);
        Long miaoshaResult = miaoShaComposeService.getSecondKillResult(Long.valueOf(user.getNickname()), goodsId);
        result.setData(miaoshaResult);
        return result;
    }

    @RequireLogin(seconds = 5, maxCount = 5, needLogin = true)
    @RequestMapping(value = "/path", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value="获取秒杀商品路径")
    public ResultGeekQ<String> getMiaoshaPath(HttpServletRequest request, MiaoshaUser user,
                                              @RequestParam("goodsId") long goodsId,
                                              @RequestParam(value = "verifyCode", defaultValue = "0") int verifyCode
    ) {
        ResultGeekQ<String> result = ResultGeekQ.build();
        if (user == null) {
            result.withError(SESSION_ERROR.getCode(), SESSION_ERROR.getMessage());
            return result;
        }
        boolean check = iVerficateService.checkVerifyCode(user, goodsId, verifyCode);
        if (!check) {
            result.withError(REQUEST_ILLEGAL.getCode(), REQUEST_ILLEGAL.getMessage());
            return result;
        }
        String path = miaoShaComposeService.createSecondKillPath(user, goodsId);
        result.setData(path);
        return result;
    }

    @RequestMapping(value = "/verifyCodeRegister", method = RequestMethod.GET)
    @ResponseBody
    public ResultGeekQ<String> getMiaoshaVerifyCod(HttpServletResponse response
                                                  ) {
        ResultGeekQ<String> result = ResultGeekQ.build();
        try {
            BufferedImage image = iVerficateService.createVerifyCodeRegister();
            OutputStream out = response.getOutputStream();
            ImageIO.write(image, "JPEG", out);
            out.flush();
            out.close();
            return result;
        } catch (Exception e) {
            logger.error("生成验证码错误-----注册:{}", e);
            result.withError(MIAOSHA_FAIL.getCode(), MIAOSHA_FAIL.getMessage());
            return result;
        }
    }

/*
*获取秒杀页面校验码
* */
    @RequestMapping(value = "/verifyCode", method = RequestMethod.GET)
    @ResponseBody
    public ResultGeekQ<String> getMiaoshaVerifyCod(HttpServletResponse response, MiaoshaUser user,
                                                   @RequestParam("goodsId") long goodsId) {
        ResultGeekQ<String> result = ResultGeekQ.build();
        if (user == null) {
            result.withError(SESSION_ERROR.getCode(), SESSION_ERROR.getMessage());
            return result;
        }
        try {
            BufferedImage image = iVerficateService.createVerifyCode(user, goodsId);
            OutputStream out = response.getOutputStream();
            ImageIO.write(image, "JPEG", out);
            out.flush();
            out.close();
            return result;
        } catch (Exception e) {
            logger.error("生成验证码错误-----goodsId:{}", goodsId, e);
            result.withError(MIAOSHA_FAIL.getCode(), MIAOSHA_FAIL.getMessage());
            return result;
        }
    }

    @RequestMapping(value = "/initStock",method=RequestMethod.GET)
    @ResponseBody
    public String initSecondKillStockInCache(){
        miaoShaComposeService.prepareSecondKillStockToRedis();
        return "success";
    }

    @RequestMapping(value="/test",method=RequestMethod.GET)
    @ResponseBody
    public void test(){
        GoodsComposeService goodsComposeService= SpringContextUtil.getBean(GoodsComposeService.class);
        GoodsExtVo goodsExtVo=new GoodsExtVo();

        goodsExtVo.setId((long)2);
        goodsComposeService.reduceStock(goodsExtVo);
        return ;
    }


}
