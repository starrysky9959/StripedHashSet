/*
 * @Author: starrysky9959 starrysky9651@outlook.com
 * @Date: 2022-11-15 23:44:20
 * @LastEditors: starrysky9959 starrysky9651@outlook.com
 * @LastEditTime: 2022-11-16 15:57:17
 * @Description:  
 */
package com.example.hashset;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class BaseHashSet<T> implements Set<T> {
    protected List<T>[] table;
    protected AtomicInteger size;

    public BaseHashSet(int capacity) {
        size = new AtomicInteger(0);
        table = new List[capacity];
        for (int i = 0; i < capacity; ++i) {
            table[i] = new ArrayList<T>();
        }
    }

    @Override
    public boolean contains(T key) {
        acquire(key);
        try {
            int myBucket = Math.abs(key.hashCode() % table.length);
            return table[myBucket].contains(key);
        } finally {
            release(key);
        }
    }

    @Override
    public boolean add(T key) {
        acquire(key);
        boolean result;
        try {
            int myBucket = Math.abs(key.hashCode() % table.length);
            result = table[myBucket].add(key);
            if (result) {
                size.incrementAndGet();
            }

        } finally {
            release(key);
        }

        if (policy()) {
            resize();
        }
        return result;
    }

    @Override
    public boolean remove(T key) {
        acquire(key);
        try {
            int myBucket = Math.abs(key.hashCode() % table.length);
            boolean result = table[myBucket].remove(key);
            if (result) {
                size.decrementAndGet();
            }
            return result;
        } finally {
            release(key);
        }
    }

    /*
     * acquire the lock
     */
    protected abstract void acquire(T key);

    /*
     * release the lock
     */
    protected abstract void release(T key);

    /*
     * decide whether to resize
     */
    protected abstract boolean policy();

    /*
     * resize the set
     */
    protected abstract void resize();
}
