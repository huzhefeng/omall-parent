package com.offcn;


import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CompletableFutureDemo3 {

    public static void main(String[] args) {
        //创建一个线程池
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(50, 500, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10000));

        //编写线程，执行返回一个结果 hello
        CompletableFuture<String> futureA=CompletableFuture.supplyAsync(()->"hello");

        //编写线程1，获取线程1执行结果
        CompletableFuture<Void> futureB=futureA.thenAcceptAsync((s)->{
            //让线程2睡4秒
            try {
                Thread.sleep(2000);
                System.out.println("第一个线程执行结果:"+s);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        },threadPoolExecutor);

        //编写第三个线程，获取线程1结果
        CompletableFuture<Void> futureC=futureA.thenAcceptAsync((s)->{
            //让线程3睡3秒
            try {
                Thread.sleep(5000);
                System.out.println("第二个线程执行结果:"+s);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        },threadPoolExecutor);
    }
}
