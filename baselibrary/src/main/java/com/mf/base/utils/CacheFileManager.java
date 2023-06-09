package com.mf.base.utils;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 缓存文件管理器
 */
public class CacheFileManager {

    private final AtomicLong cacheSize;
    private final AtomicInteger cacheCount;
    private final long sizeLimit;
    private final int countLimit;
    private final Map<File, Long> lastUsageDates = Collections
            .synchronizedMap(new HashMap<File, Long>());
    protected File cacheDir;

    public CacheFileManager(File cacheDir, long sizeLimit, int countLimit) {
        this.cacheDir = cacheDir;
        this.sizeLimit = sizeLimit;
        this.countLimit = countLimit;
        cacheSize = new AtomicLong();
        cacheCount = new AtomicInteger();
        calculateCacheSizeAndCacheCount();
    }

    /**
     * 计算 cacheSize和cacheCount
     */
    private void calculateCacheSizeAndCacheCount() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int size = 0;
                int count = 0;
                File[] cachedFiles = cacheDir.listFiles();
                if (cachedFiles != null) {
                    for (File cachedFile : cachedFiles) {
                        size += calculateSize(cachedFile);
                        count += 1;
                        lastUsageDates.put(cachedFile,
                                cachedFile.lastModified());
                    }
                    cacheSize.set(size);
                    cacheCount.set(count);
                }
            }
        }).start();
    }

    public void put(String filePath) {
        put(new File(filePath));
    }

    public void put(File file) {
        int curCacheCount = cacheCount.get();
        while (curCacheCount + 1 > countLimit) {
            long freedSize = removeNext();
            cacheSize.addAndGet(-freedSize);

            curCacheCount = cacheCount.addAndGet(-1);
        }
        cacheCount.addAndGet(1);

        long valueSize = calculateSize(file);
        long curCacheSize = cacheSize.get();
        while (curCacheSize + valueSize > sizeLimit) {
            long freedSize = removeNext();
            curCacheSize = cacheSize.addAndGet(-freedSize);
        }
        cacheSize.addAndGet(valueSize);

        Long currentTime = System.currentTimeMillis();
        file.setLastModified(currentTime);
        lastUsageDates.put(file, currentTime);
    }

    public void useFile(String filePath) {
        File file = new File(filePath);
        useFile(file);
    }

    public void useFile(File file) {
        Long currentTime = System.currentTimeMillis();
        file.setLastModified(currentTime);
        lastUsageDates.put(file, currentTime);
    }

    public File get(String key) {
        File file = newFile(key);
        Long currentTime = System.currentTimeMillis();
        file.setLastModified(currentTime);
        lastUsageDates.put(file, currentTime);

        return file;
    }

    private File newFile(String key) {
        return new File(cacheDir, key.hashCode() + "");
    }

    private boolean remove(String key) {
        File image = get(key);
        return image.delete();
    }

    private void clear() {
        lastUsageDates.clear();
        cacheSize.set(0);
        File[] files = cacheDir.listFiles();
        if (files != null) {
            for (File f : files) {
                f.delete();
            }
        }
    }

    /**
     * 移除旧的文件
     *
     * @return
     */
    private long removeNext() {
        if (lastUsageDates.isEmpty()) {
            return 0;
        }

        Long oldestUsage = null;
        File mostLongUsedFile = null;
        Set<Map.Entry<File, Long>> entries = lastUsageDates.entrySet();
        synchronized (lastUsageDates) {
            for (Map.Entry<File, Long> entry : entries) {
                if (mostLongUsedFile == null) {
                    mostLongUsedFile = entry.getKey();
                    oldestUsage = entry.getValue();
                } else {
                    Long lastValueUsage = entry.getValue();
                    if (lastValueUsage < oldestUsage) {
                        oldestUsage = lastValueUsage;
                        mostLongUsedFile = entry.getKey();
                    }
                }
            }
        }

        long fileSize = calculateSize(mostLongUsedFile);
        if (mostLongUsedFile.delete()) {
            lastUsageDates.remove(mostLongUsedFile);
        }
        return fileSize;
    }

    private long calculateSize(File file) {
        return file.length();
    }
}
