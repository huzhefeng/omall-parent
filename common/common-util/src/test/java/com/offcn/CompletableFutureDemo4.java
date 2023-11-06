package com.offcn;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class CompletableFutureDemo4 {

    public static void main(String[] args) {
        //编写线程1
        CompletableFuture<String> futureA=CompletableFuture.supplyAsync(()->{
            //让当前线程睡3秒
            try {
                Thread.sleep(3000);
                System.out.println("线程1执行");

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "one";
        });

        //编写线程2
        CompletableFuture<String> futureB=CompletableFuture.supplyAsync(()->{
            try {
                Thread.sleep(4000);
                System.out.println("线程2执行");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "two";
        });

        //把以上2个线程组合到一起，等待全部执行完毕
        CompletableFuture.anyOf(futureA,futureB).whenComplete(new BiConsumer<Object, Throwable>() {
            /**
             * Performs this operation on the given arguments.
             *
             * @param unused    the first input argument
             * @param throwable the second input argument
             */
            @Override
            public void accept(Object unused, Throwable throwable) {
                System.out.println("两个线程执行完毕");
            }
        });

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
