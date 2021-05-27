package com.geekq.miaosha.order.redis;

public class Userkey extends BasePrefix {

    private Userkey(String prefix) {
        super( prefix);
    }

    public static Userkey getById = new Userkey("id") ;

    public static Userkey getByName = new Userkey("name") ;

}
