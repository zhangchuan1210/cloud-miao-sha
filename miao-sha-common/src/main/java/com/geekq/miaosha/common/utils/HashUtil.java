package com.geekq.miaosha.common.utils;

public class HashUtil {


    public static long getHash(String key,int mod){
        if(mod>0 && mod<Integer.MAX_VALUE){
           int h=key.hashCode() ;
           return (h ^ (h>>>16)) & mod;

        }else{
            return 0;
        }
    }

}
