/*
 * @Author: starrysky9959 starrysky9651@outlook.com
 * @Date: 2022-11-16 00:21:03
 * @LastEditors: starrysky9959 starrysky9651@outlook.com
 * @LastEditTime: 2022-11-16 16:04:58
 * @Description:  
 */
package com.example.hashset;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class StripedHashSet<T> extends BaseHashSet<T> {
    final Lock[] locks;

    public StripedHashSet(int capacity) {
        super(capacity);
        locks = new Lock[capacity];
        for (int i = 0; i < capacity; ++i) {
            locks[i] = new ReentrantLock();
        }
    }

    @Override
    public int size() {
        return size.get();
    }

    @Override
    protected void acquire(T key) {
        int myBucket = Math.abs(key.hashCode() % locks.length);
        locks[myBucket].lock();
    }

    @Override
    protected void release(T key) {
        int myBucket = Math.abs(key.hashCode() % locks.length);
        locks[myBucket].unlock();
    }

    @Override
    protected boolean policy() {
        return size.get() / table.length > 4;
    }

    @Override
    protected void resize() {
        int oldCapacity = table.length;
        for (Lock lock : locks) {
            lock.lock();
        }
        try {
            if (oldCapacity != table.length) { // have been resized by other thread
                return;
            }

            int newCapacity = 2 * oldCapacity;
            List<T>[] oldTable = table;
            for (int i = 0; i < newCapacity; ++i) {
                table[i] = new ArrayList<T>();
            }
            initializeFrom(oldTable);
        } finally {
            for (Lock lock : locks) {
                lock.unlock();
            }
        }
    }

    /*
     * move old data to new table
     */
    private void initializeFrom(List<T>[] oldTable) {
        for (List<T> bucket : oldTable) {
            for (T key : bucket) {
                int myBucket = Math.abs(key.hashCode() % table.length);
                table[myBucket].add(key);
            }
        }
    }
}
