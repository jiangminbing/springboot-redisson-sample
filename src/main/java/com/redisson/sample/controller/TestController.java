package com.redisson.sample.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author jiangmb
 * @version 1.0.0
 * @date 2023-03-15 11:51
 */
@RestController
@RequestMapping("/v1")
@Slf4j
@Api(value = "测试类", description = "测试类")
public class TestController {
    @Autowired
    RedissonClient redissonClient;


    @ApiOperation(value = "发送广播消息", notes = "发送广播消息")
    @GetMapping(value = "private/test1")
    public String test1(
            @RequestParam("name")String name
    ) {
        // Map 数据集操作
        RMap<String,Object> testMap = redissonClient.getMap("reddisson:test");
        testMap.put("key1","Jeff");

        // String 数据操作
        RBucket<String> bucket = redissonClient.getBucket("myBucket");
        bucket.set("Hello World!");
        //  设置String 和存活时间
        bucket.set("Hello World",10, TimeUnit.SECONDS);


        // set 数据操作
        RSet<String> set = redissonClient.getSet("mySet");
        set.add("value1");
        set.add("value2");
        set.add("value3");

        // list 数据集合操作
        RList<String> list = redissonClient.getList("myList");
        list.add("value1");
        list.add("value2");
        list.add("value3");

        //zset 数据操作
        RScoredSortedSet<Integer> zset = redissonClient.getScoredSortedSet("myZset");
        zset.add(1,1);
        zset.add(3,3);
        zset.add(2,3);

        return "success";
    }


    @ApiOperation(value = "测试方法", notes = "测试方法")
    @GetMapping(value = "private/test2")
    public String test2(
            @RequestParam("name")String name
    ) {

        RBucket buckets = redissonClient.getBucket("setnx");
        String vin = "12312";
        // 利用Redis的事务特性，同时保存所有的通用对象桶，如果任意一个通用对象桶已经存在则放弃保存其他所有数据。
        buckets.trySet(vin);
        return "success";
    }

    @ApiOperation(value = "测试方法", notes = "测试方法")
    @GetMapping(value = "private/test3")
    public String test3(
            @RequestParam("name")String name
    ) throws InterruptedException {
//        RLock lock = redissonClient.getLock("anyLock");
//        // 最常见的使用方法
//        lock.lock();
//        // 加锁以后10秒钟自动解锁
//        // 无需调用unlock方法手动解锁
//        lock.lock(10, TimeUnit.SECONDS);

        /**
         * 模拟并发场景下获取锁
         */
        CountDownLatch countDownLatch = new CountDownLatch(5);

        for(int i = 0;i<5;i++){
            Thread thread = new Thread(()->{
                // 先休眠5s等待所有线程创建完成
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                RLock lock = redissonClient.getLock("anyLock");
                boolean res = false;
                try {
                    // 尝试加锁，最多等待100秒，上锁以后10秒自动解锁
                    res = lock.tryLock(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (res) {
                    try {
                        System.out.println(Thread.currentThread().getName()+"线程获取了对象锁，开始休眠10s");
                        try {
                            TimeUnit.SECONDS.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } finally {
                        if(lock.isLocked()){
                            lock.unlock();
                        }
                    }
                }
                countDownLatch.countDown();
            });
            thread.start();
        }
        // 释放状态，等待计数的线程执行结束后才会执行后面的数据
        countDownLatch.await();

        System.out.println("线程竞争结束了");
        return "success";
    }



}
