package com.geekq.cloud.consumer2service.service.impl;

import com.geekq.cloud.consumer2service.service.IDeadLockTest;
import lombok.Synchronized;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class DeadlockTest implements IDeadLockTest {
    private static Object lockA=new Object();
    private static Object lockB=new Object();
    @Override
    public void test(){
        Thread threada=new Thread(()->{
            synchronized (lockA){
                System.out.println(Thread.currentThread().getName()+"获取 locka成功。");

                try {
                    TimeUnit.SECONDS.sleep(5);
                    synchronized (lockB){
                        System.out.println(Thread.currentThread().getName()+"尝试获取lockb。");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });
        Thread threadb=new Thread(()->{
            synchronized (lockB){
                System.out.println(Thread.currentThread().getName()+"获取 lockb成功。");

                try {
                    TimeUnit.SECONDS.sleep(6);
                    synchronized (lockA){
                        System.out.println(Thread.currentThread().getName()+"尝试获取locka。");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });

        threada.start();
        threadb.start();

    }


    public static void main(String[] args) {
        DeadlockTest deadlockTest=new DeadlockTest();
        deadlockTest.test();
    }
}
