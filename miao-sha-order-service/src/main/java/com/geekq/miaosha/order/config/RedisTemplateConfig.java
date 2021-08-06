package com.geekq.miaosha.order.config;

import com.geekq.miaosha.order.redis.LocalStockOverSubcribe;
import com.geekq.miaosha.order.redis.MiaoshaKey;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.net.UnknownHostException;

@Configuration
public class RedisTemplateConfig {
    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) throws UnknownHostException {
        RedisTemplate<Object, Object> template = new RedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        RedisSerializer stringSerializer = new StringRedisSerializer();//序列化为String
        template.setDefaultSerializer(stringSerializer);
        return template;
    }
    @Bean
    RedisMessageListenerContainer listenerContainer(RedisConnectionFactory redisConnectionFactory, MessageListenerAdapter listenerAdapter){
        RedisMessageListenerContainer redisMessageListenerContainer=new RedisMessageListenerContainer();
        redisMessageListenerContainer.setConnectionFactory(redisConnectionFactory);
        redisMessageListenerContainer.addMessageListener(listenerAdapter,new ChannelTopic(MiaoshaKey.isGoodsOver.getPrefix()));
        return redisMessageListenerContainer;
    }

    @Bean
    MessageListenerAdapter messageListener() {
        return new MessageListenerAdapter(new LocalStockOverSubcribe());
    }


}
