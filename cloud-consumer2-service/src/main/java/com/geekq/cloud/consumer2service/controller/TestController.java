package com.geekq.cloud.consumer2service.controller;

import java.sql.PreparedStatement;

/*
* 找出范围内质数，统计质数个位数与十位数之和大的那个数
* */
public class TestController {


    private static boolean isPrive(long num){
        for(int i=2;i<num;++i){
            if((num % i) ==0){
                return false;
            }
        }
        return true;
    }

    private static int getMin(long low,long hight){
        int result=0,shiweiall=0,geweiall=0;
        for(long temp=low;temp<hight;++temp){
            if(isPrive(temp)){
                int all=(int )temp % 100;
                int shiwei=0,gewei=0;
                if(all /10 >0){
                    shiwei=all/10;
                }
                gewei=all%10;
                shiweiall+=shiwei;
                geweiall+=gewei;


            }
        }
        result=shiweiall<geweiall ? shiweiall:geweiall;
        return result;
    }

    public static void main(String[] args) {
        long low=151,hight=160;
        int rerult=getMin(151,160);
        System.out.println(rerult);
    }

}
