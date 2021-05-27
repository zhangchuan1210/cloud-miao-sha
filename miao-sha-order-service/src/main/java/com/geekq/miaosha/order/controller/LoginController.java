package com.geekq.miaosha.order.controller;


import com.geekq.miaosha.order.redis.redismanager.RedisLua;
import com.geekq.miaosha.order.service.MiaoShaUserComposeService;
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

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import static com.geekq.miaosha.common.enums.Constanst.COUNTLOGIN;

@Controller
@RequestMapping("/login")
public class LoginController {

    private static Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private MiaoShaUserComposeService userService;



    @RequestMapping("/sayHello")
    public String sayHello() throws Exception {
        return "login222";
    }



    @RequestMapping("/to_login")
    public String tologin(LoginVo loginVo, Model model)  {
        logger.info(loginVo.toString());

        //未完成
          RedisLua.vistorCount(COUNTLOGIN);
        String count = RedisLua.getVistorCount(COUNTLOGIN).toString();
        logger.info("访问网站的次数为:{}",count);
        model.addAttribute("count",count);
        return "login";
    }

    @RequestMapping(value = "/loginin",method = RequestMethod.POST)
    @ResponseBody
    public ResultGeekQ<String> dologin(HttpServletResponse response, @Valid LoginVo loginVo) {
        ResultGeekQ<String> result = ResultGeekQ.build();
        logger.info(loginVo.toString());
        userService.login(response, loginVo);
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