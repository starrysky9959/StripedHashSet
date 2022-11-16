/*
 * @Author: starrysky9959 starrysky9651@outlook.com
 * @Date: 2022-11-16 11:06:22
 * @LastEditors: starrysky9959 starrysky9651@outlook.com
 * @LastEditTime: 2022-11-16 16:00:21
 * @Description:  
 */
package com.example;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.example.hashset.*;

/**
 * Unit test for simple App.
 */
public class AppTest {
    private final static int THREAD_NUM = 8;
    private final static int TEST_SIZE = 512;
    private final static int PER_THREAD_TASK = TEST_SIZE / THREAD_NUM;
    Set<Integer> set;
    Thread[] threads;

    public void commonTest() throws InterruptedException {
        threads = new Thread[THREAD_NUM];

        // add operation
        for (int i = 0; i < THREAD_NUM; ++i) {
            threads[i] = new AddThread(i);
        }
        for (int i = 0; i < THREAD_NUM; ++i) {
            threads[i].start();
        }
        for (int i = 0; i < THREAD_NUM; ++i) {
            threads[i].join();
        }
        System.out.println("[INFO] set size = " + set.size());
        assertTrue(set.size() == TEST_SIZE);

        // contains operation
        for (int i = 0; i < THREAD_NUM; ++i) {
            threads[i] = new ContainsThread(i);
        }
        for (int i = 0; i < THREAD_NUM; ++i) {
            threads[i].start();
        }
        for (int i = 0; i < THREAD_NUM; ++i) {
            threads[i].join();
        }

        // remove operation
        for (int i = 0; i < THREAD_NUM; ++i) {
            threads[i] = new RemoveThread(i);
        }
        for (int i = 0; i < THREAD_NUM; ++i) {
            threads[i].start();
        }
        for (int i = 0; i < THREAD_NUM; ++i) {
            threads[i].join();
        }
        System.out.println("[INFO] set size = " + set.size());
        assertTrue(set.size() == 0);
    }

    @Test
    public void testStripedHashSet() throws InterruptedException {

        set = new StripedHashSet<Integer>(TEST_SIZE);
        commonTest();
    }

    @Test
    public void testRWStripedHashSet() throws InterruptedException {

        set = new RWLockStripedHashSet<Integer>(TEST_SIZE);
        commonTest();
    }

    public int getValue(int id, int i) {
        return id * PER_THREAD_TASK + i;
    }

    class AddThread extends Thread {
        int threadID;

        AddThread(int id) {
            threadID = id;
        }

        @Override
        public void run() {
            System.out.println("[INFO] add op in thread " + threadID);
            for (int i = 0; i < PER_THREAD_TASK; ++i) {
                int value = getValue(threadID, i);

                if (!set.add(value)) {
                    System.err.println("[ERROR] add " + value + " fail!");
                }
            }
        }
    }

    class ContainsThread extends Thread {
        int threadID;

        ContainsThread(int id) {
            threadID = id;
        }

        @Override
        public void run() {
            System.out.println("[INFO] contains op in thread " + threadID);
            for (int i = 0; i < PER_THREAD_TASK; ++i) {
                int value = getValue(threadID, i);

                if (!set.contains(value)) {
                    System.err.println("[ERROR] contains " + value + " fail!");
                }
            }
        }
    }

    class RemoveThread extends Thread {
        int threadID;

        RemoveThread(int id) {
            threadID = id;
        }

        @Override
        public void run() {
            System.out.println("[INFO] remove op in thread " + threadID);
            for (int i = 0; i < PER_THREAD_TASK; ++i) {
                int value = getValue(threadID, i);
                if (!set.remove(value)) {
                    System.err.println("[ERROR] remove " + value + " fail!");
                }
            }
        }
    }
}
