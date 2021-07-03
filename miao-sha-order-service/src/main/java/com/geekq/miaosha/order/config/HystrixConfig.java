package com.geekq.miaosha.order.config;

import com.netflix.hystrix.contrib.javanica.aop.aspectj.HystrixCommandAspect;
import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HystrixConfig {

    @Bean
   public ServletRegistrationBean hystrixMetricsStreamServlet() {
            return new ServletRegistrationBean(new HystrixMetricsStreamServlet(), "/hystrix.stream");
        }

             /**
 25      * AspectJ aspect to process methods which annotated with {@link HystrixCommand} annotation.
 26      *
 27      * {@link HystrixCommand} annotation used to specify some methods which should be processes as hystrix commands.
 28      */
            @Bean
           public HystrixCommandAspect hystrixCommandAspect() {
               return new HystrixCommandAspect();
           }
}
