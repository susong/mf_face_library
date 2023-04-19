package com.mf.base.utils;

import java.util.concurrent.atomic.AtomicBoolean;
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
    private final String mName;
    private AtomicBoolean mFlagLockRead = new AtomicBoolean(false);
    private AtomicBoolean mFlagLockWrite = new AtomicBoolean(false);

    public Locker() {
        mName = getClass().getSimpleName();
    }

    Locker(String name) {
        mName = name;
    }

    public void lock() {
        mLock.writeLock().lock();
        mFlagLockWrite.set(true);
    }

    public void unlock() {
        mLock.writeLock().unlock();
        mFlagLockWrite.set(false);
    }

    public void lockRead() {
        mLock.writeLock().lock();
        mFlagLockWrite.set(true);
    }

    public void unlockRead() {
        mLock.writeLock().unlock();
        mFlagLockWrite.set(false);
    }

    public void lockWrite() {
        mLock.writeLock().lock();
        mFlagLockWrite.set(true);
    }

    public void unlockWrite() {
        mLock.writeLock().unlock();
        mFlagLockWrite.set(false);
    }

    public String name() {
        return mName;
    }

    protected boolean isLockRead() {
        return mFlagLockRead.get();
    }

    protected boolean isLockWrite() {
        return mFlagLockWrite.get();
    }

    public void dump0(StringBuilder builder) {
        builder.append("name:");
        builder.append(mName);
        builder.append(",LockWrite:");
        builder.append(mFlagLockWrite.get());
        builder.append(",LockRead:");
        builder.append(mFlagLockRead.get());
    }
}
