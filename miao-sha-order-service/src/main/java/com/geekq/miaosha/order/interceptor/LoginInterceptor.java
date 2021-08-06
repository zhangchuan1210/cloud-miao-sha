package com.geekq.miaosha.order.interceptor;

import com.geekq.miaosha.common.biz.entity.MiaoshaUser;
import com.geekq.miaosha.common.utils.UserContext;
import com.geekq.miaosha.order.redis.RedisService;
import com.geekq.miaosha.order.service.impl.MiaoShaUserComposeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


@Service
public class LoginInterceptor extends HandlerInterceptorAdapter {

	private static Logger logger = LoggerFactory.getLogger(LoginInterceptor.class);

	@Autowired
	MiaoShaUserComposeService userService;

	@Autowired
	RedisService redisService;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		/**
		 * 获取调用 获取主要方法
		 */
		if(handler instanceof HandlerMethod) {
			logger.info("打印拦截方法handler ：{} ",handler);
			HandlerMethod hm = (HandlerMethod)handler;
			MiaoshaUser user = getUser(request, response);
			if(user == null){
				response.sendRedirect("/do_login");
				return super.preHandle(request, response, handler);
			}

			UserContext.setUser(user);
		}
		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		super.afterCompletion(request, response, handler, ex);
		UserContext.removeUser();
	}


	private MiaoshaUser getUser(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session= request.getSession();
        if(null!=session){
        	String sessionId=session.getId();
			return (MiaoshaUser)session.getAttribute(sessionId);
		} else{
        	return null;
		}

	}


}
