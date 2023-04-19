package com.mf.base.config;

/**
 * 用于统计函数调用时间
 */
public final class TimeCoster {
    private long mStart = 0;

    public TimeCoster() {
        mStart = System.currentTimeMillis();
    }

    public void begin() {
        mStart = System.currentTimeMillis();
    }

    public long end() {
        return System.currentTimeMillis() - mStart;
    }
}
