/*
 * @Author: starrysky9959 starrysky9651@outlook.com
 * @Date: 2022-11-15 23:38:38
 * @LastEditors: starrysky9959 starrysky9651@outlook.com
 * @LastEditTime: 2022-11-16 16:04:07
 * @Description: 
 */
package com.example.hashset;

public interface Set<T> {
    int size();

    boolean contains(T key);

    boolean add(T key);

    boolean remove(T key);
}
