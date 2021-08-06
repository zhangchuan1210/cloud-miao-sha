package com.geekq.cloud.consumerservice;

import java.util.ArrayList;
import java.util.List;

/*
* gc: -Xms15M -Xmx15M -XX:+PrintGCDetails -XX:SurvivorRatio=8 -XX:+UseSerialGC -XX:+HeapDumpOutOfMemoryError
*
* */
public class TestClass {
    private final static int  _1M=1024*1024;

    public static void main(String[] args) {

        List<byte[]> byteList=new ArrayList<>();
        for(int i=0;i<1000;++i){
            byte[] temp=new byte[1*_1M];
            byteList.add(temp);
            try {
                Thread.sleep(12000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }



    }
}
