package com.mf.base.log;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class LoggerFactory {

    private static Map<String, Logger> mLoggerMap = new ConcurrentHashMap<>();

    public static Logger getLogger(Class<?> cls) {
        return getLogger(cls, "");
    }

    public static Logger getLogger(Class<?> cls, String tag) {
        if (!mLoggerMap.containsKey(cls.getName())) {
            mLoggerMap.put(cls.getName(), new Logger(cls, tag));
        }
        return mLoggerMap.get(cls.getName());
    }
    
}
