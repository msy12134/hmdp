package com.hmdp.dto;

import lombok.Data;

@Data
public class CacheData<T> {
    private T data;
    private long expireTime;

    public CacheData(T data, long expireTime) {
        this.data = data;
        this.expireTime = expireTime;
    }
}
