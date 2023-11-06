package com.offcn.product.service;

public interface TestService {

    //测试本地锁
    void testLock();


    //测试读锁
    String readLock();

    //测试写锁
    String writeLock();
}
