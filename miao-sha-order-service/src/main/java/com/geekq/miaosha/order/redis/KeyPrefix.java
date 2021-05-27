package com.geekq.miaosha.order.redis;

public interface KeyPrefix {

    public int expireSeconds() ;

    public String getPrefix() ;

}
