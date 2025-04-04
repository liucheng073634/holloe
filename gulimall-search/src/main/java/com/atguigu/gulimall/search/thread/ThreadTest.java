package com.atguigu.gulimall.search.thread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadTest {
    public static ExecutorService executor=Executors.newFixedThreadPool(10);
    public static void main(String[] args) throws ExecutionException, InterruptedException {
      /*  System.out.println("main");
        CompletableFuture<Integer> voidCompletableFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程"+Thread.currentThread().getId());
            return 10/0;
        }, executor).whenComplete((t,u)->{
            System.out.println("t"+t);
            System.out.println("u"+u);
        }).exceptionally(e->{
            e.printStackTrace();
            return 10;
        });
        try {
            System.out.println( voidCompletableFuture.get());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        */



        CompletableFuture<Object> future1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程"+Thread.currentThread().getId());
            return "hello";
        }, executor);
        CompletableFuture<Object> future2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程"+Thread.currentThread().getId());
            return "属性";
        }, executor);

        CompletableFuture<Object> future3 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("当前线程"+Thread.currentThread().getId());
            return "颜色";
        }, executor);

        CompletableFuture<Void> voidCompletableFuture = CompletableFuture.allOf(future1, future2, future3);
        voidCompletableFuture.get();
        System.out.println(future1.get()+"-"+future2.get()+"-"+future3.get());

    }

}
