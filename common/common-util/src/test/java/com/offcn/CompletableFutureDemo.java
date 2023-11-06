package com.offcn;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class CompletableFutureDemo {
    public static void main(String[] args) throws ExecutionException,InterruptedException {
        CompletableFuture<Object> completableFuture = CompletableFuture.supplyAsync(new Supplier<Object>() {
            /**
             * Gets a result.
             *
             * @return a result
             */
            @Override
            public Object get() {
                System.out.println(Thread.currentThread().getName() + " CompletableFuture");
                int i=1/0;
                return 1024;
            }
        }).whenComplete(new BiConsumer<Object, Throwable>() {
            /**
             * Performs this operation on the given arguments.
             *
             * @param o         the first input argument
             * @param throwable the second input argument
             */
            @Override
            public void accept(Object o, Throwable throwable) {
                System.out.println("-------o=" + o.toString());
                System.out.println("-------thorwable=" + throwable);
            }
        }).exceptionally(new Function<Throwable, Object>() {
            /**
             * Applies this function to the given argument.
             *
             * @param throwable the function argument
             * @return the function result
             */
            @Override
            public Object apply(Throwable throwable) {
                System.out.println("throwable=" + throwable);
                return 6666;
            }
        });

        System.out.println(completableFuture.get());
    }
}
