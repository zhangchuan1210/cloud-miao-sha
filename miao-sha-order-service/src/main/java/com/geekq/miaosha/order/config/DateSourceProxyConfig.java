package com.geekq.miaosha.order.config;

import com.alibaba.druid.pool.DruidDataSource;
import io.seata.rm.datasource.DataSourceProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DateSourceProxyConfig {


    @Primary
    @Bean
    public DataSourceProxy dataSouce(DataSource druidDataSource){
        return new DataSourceProxy(druidDataSource);
    }
}
