package com.geekq.miaosha.order.init;


import com.geekq.miaosha.common.utils.SpringContextUtil;
import com.geekq.miaosha.order.service.MiaoShaComposeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class RunnerAfterStartUp implements ApplicationRunner {
    @Autowired
    private MiaoShaComposeService miaoShaComposeService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
      /*  Reflections reflections = new Reflections("com.geekq.miaosha.order");
        Set<Class<?>> classList = reflections.getTypesAnnotatedWith(RunningAfterStartUp.class);
        */

       /* String[] beanNames= SpringContextUtil.getApplicationContext().getBeanDefinitionNames();
        for(String name :beanNames){
              Method[] methods=  SpringContextUtil.getBean("miaoShaComposeService").getClass().getDeclaredMethods();
              for(Method method : methods){
                  if(method.isAnnotationPresent(RunningAfterStartUp.class)){
                      System.out.println(method.getName());
                  }
              }



        }
*/
        miaoShaComposeService.prepareSecondKillStockToRedis();
        return ;
    }
}
