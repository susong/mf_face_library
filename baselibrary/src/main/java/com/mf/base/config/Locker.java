package com.mf.base.config;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @ClassName: Locker
 * @Description: java类作用描述
 * @Author: duanbangchao
 * @CreateDate: 11/12/20
 * @UpdateUser: updater
 * @UpdateDate: 11/12/20
 * @UpdateRemark: 更新内容
 * @Version: 1.0
 */
public class Locker {
    private ReentrantReadWriteLock mLock = new ReentrantReadWriteLock();

    public void lock() {
        mLock.writeLock().lock();
    }

    public void unlock() {
        mLock.writeLock().unlock();
    }

    public void lockRead() {
        mLock.readLock().lock();
    }

    public void unlockRead() {
        mLock.readLock().unlock();
    }

    public void lockWrite() {
        mLock.writeLock().lock();
    }

    public void unlockWrite() {
        mLock.writeLock().unlock();
    }
}
