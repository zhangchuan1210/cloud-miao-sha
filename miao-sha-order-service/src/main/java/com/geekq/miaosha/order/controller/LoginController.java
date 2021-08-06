package com.geekq.miaosha.order.controller;


import cn.hutool.core.date.DateUtil;
import com.geekq.miaosha.common.utils.HashUtil;
import com.geekq.miaosha.order.redis.RedisService;
import com.geekq.miaosha.order.service.impl.MiaoShaUserComposeService;
import com.geekq.miaosha.common.enums.resultbean.ResultGeekQ;
import com.geekq.miaosha.common.vo.LoginVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.geekq.miaosha.common.enums.Constanst.*;

@Controller
@RequestMapping("/login")
public class LoginController {

    private static Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private MiaoShaUserComposeService userService;
    @Autowired
    private RedisService redisService;

    private final String visitScriptPath="lua/visit.lua";
    private final int mod=1000000;

    @RequestMapping("/to_login")
    public String tologin(LoginVo loginVo, Model model)  {
        logger.info(loginVo.toString());
        String visitorId=loginVo.getNickname();
        List<String> keyList=new ArrayList<>();
        String dayDate=DateUtil.format(new Date(),"YYYY-MM-dd");
        //当前用户访问次数
        keyList.add(COUNTLOGIN);
        //总访问次数
        keyList.add(COUNTALLLOGIN);
        //每天总访问量
        keyList.add(dayDate+"_"+COUNTDAYLOGIN);
        Long temp= HashUtil.getHash(visitorId,mod);
        Long count=(Long) redisService.execScript(visitScriptPath,Long.class,keyList,visitorId,String.valueOf(temp));
        logger.info("访问网站的次数为:{}",count);
        model.addAttribute("count",count);
        return "login";
    }

    @RequestMapping(value = "/loginin",method = RequestMethod.POST)
    @ResponseBody
    public ResultGeekQ<String> dologin(HttpServletRequest request,HttpServletResponse response, @Valid LoginVo loginVo) {
        ResultGeekQ<String> result = ResultGeekQ.build();
        logger.info(loginVo.toString());
        userService.login(request,response, loginVo);
        return result;
    }


    @RequestMapping("/create_token")
    @ResponseBody
    public String createToken(HttpServletResponse response, @Valid LoginVo loginVo) {
        logger.info(loginVo.toString());
        String token = userService.createToken(response, loginVo);
        return token;
    }
}
