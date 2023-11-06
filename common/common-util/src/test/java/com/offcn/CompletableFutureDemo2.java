package com.offcn;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class CompletableFutureDemo2 {

    public static void main(String[] args) throws ExecutionException,InterruptedException{
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(new Supplier<Integer>() {
            @Override
            public Integer get() {
                System.out.println(Thread.currentThread().getName() + " CompletableFuture");

                return 1024;
            }
        }).thenApply(new Function<Integer, Integer>() {
            @Override
            public Integer apply(Integer o) {
                System.out.println("执行thenApply方法，上次方法返回结果:" + o);
                int i=1/0;
                //把上个方法返回的结果乘以2
                return o * 2;
            }
        }).whenComplete(new BiConsumer<Integer, Throwable>() {
            @Override
            public void accept(Integer o, Throwable throwable) {
                System.out.println("------o:" + o);
                System.out.println("-------throwable:" + throwable);
            }
        }).exceptionally(new Function<Throwable, Integer>() {
            @Override
            public Integer apply(Throwable throwable) {
                System.out.println("throwable:" + throwable);
                return 6666;
            }
        });
        System.out.println(future1.get());
    }
}
