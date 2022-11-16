/*
 * @Author: starrysky9959 starrysky9651@outlook.com
 * @Date: 2022-11-16 01:00:16
 * @LastEditors: starrysky9959 starrysky9651@outlook.com
 * @LastEditTime: 2022-11-16 15:58:39
 * @Description:  
 */
package com.example.hashset;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/*
 * StripedHashSet use Read/Write lock
 */
public class RWLockStripedHashSet<T> implements Set<T> {
    private List<T>[] table;
    private AtomicInteger size;
    final ReadWriteLock[] locks;

    public RWLockStripedHashSet(int capacity) {
        size = new AtomicInteger(0);
        table = new List[capacity];
        locks = new ReentrantReadWriteLock[capacity];
        for (int i = 0; i < capacity; ++i) {
            table[i] = new ArrayList<T>();
            locks[i] = new ReentrantReadWriteLock();
        }
    }

    @Override
    public int size() {
        return size.get();
    }

    @Override
    public boolean contains(T key) {
        acquireReadLock(key);
        try {
            int myBucket = Math.abs(key.hashCode() % table.length);
            return table[myBucket].contains(key);
        } finally {
            releaseReadLock(key);
        }
    }

    @Override
    public boolean add(T key) {
        acquireWriteLock(key);
        boolean result;
        try {
            int myBucket = Math.abs(key.hashCode() % table.length);
            result = table[myBucket].add(key);
            if (result) {
                size.incrementAndGet();
            }

        } finally {
            releaseWriteLock(key);
        }

        if (policy()) {
            resize();
        }
        return result;
    }

    @Override
    public boolean remove(T key) {
        acquireWriteLock(key);
        try {
            int myBucket = Math.abs(key.hashCode() % table.length);
            boolean result = table[myBucket].remove(key);
            if (result) {
                size.decrementAndGet();
            }
            return result;
        } finally {
            releaseWriteLock(key);
        }
    }

    private void acquireReadLock(T key) {
        int myBucket = Math.abs(key.hashCode() % locks.length);
        locks[myBucket].readLock().lock();
    }

    private void releaseReadLock(T key) {
        int myBucket = Math.abs(key.hashCode() % locks.length);
        locks[myBucket].readLock().unlock();
    }

    private void acquireWriteLock(T key) {
        int myBucket = Math.abs(key.hashCode() % locks.length);
        locks[myBucket].writeLock().lock();
    }

    private void releaseWriteLock(T key) {
        int myBucket = Math.abs(key.hashCode() % locks.length);
        locks[myBucket].writeLock().unlock();
    }

    private boolean policy() {
        return size.get() / table.length > 4;
    }

    private void resize() {
        int oldCapacity = table.length;
        for (ReadWriteLock lock : locks) {
            lock.writeLock().lock();
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
            for (ReadWriteLock lock : locks) {
                lock.writeLock().unlock();
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
