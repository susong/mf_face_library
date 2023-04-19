package com.mf.base.log;

import com.mf.log.LogUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Logger {
    private String mTAGPrefix;
    private String mClassName;
    private final int DEFAULT_STACK_INDEX = 3;

    public Logger(Class<?> cls, String prefix) {
        mClassName = cls.getSimpleName();
        mTAGPrefix = prefix;
    }

    public Logger(String name, String prefix) {
        mClassName = name;
        mTAGPrefix = prefix;
    }

    public void verbose(String tag, String msg) {
        LogUtils.vTag(tag, msg);
    }

    public void verbose(String tag, String format, Object... params) {
        LogUtils.vTag(tag, getParams(format, params));
    }

    public void verbose(String tag, boolean printStack, String format, Object... params) {
        LogUtils.vTag(tag, getParams(format, params));
    }

    public void info(String tag, String msg) {
        LogUtils.iTag(tag, msg);
    }

    public void info(String tag, String format, Object... params) {
        LogUtils.iTag(tag, getParams(format, params));
    }

    public void info(String tag, boolean printStack, String format, Object... params) {
        LogUtils.iTag(tag, getParams(format, params));
    }

    public void debug(String tag, String msg) {
        LogUtils.dTag(tag, msg);
    }

    public void debug(String tag, String format, Object... params) {
        LogUtils.dTag(tag, getParams(format, params));
    }

    public void debug(String tag, boolean printStack, String format, Object... params) {
        LogUtils.dTag(tag, getParams(format, params));
    }

    public void warn(String tag, String msg) {
        LogUtils.wTag(tag, msg);
    }

    public void warn(String tag, String format, Object... params) {
        LogUtils.wTag(tag, getParams(format, params));
    }

    public void warn(String tag, boolean printStack, String format, Object... params) {
        LogUtils.wTag(tag, getParams(format, params));
    }

    public void error(String tag, String msg) {
        LogUtils.eTag(tag, msg);
    }

    public void error(String tag, String format, Object... params) {
        LogUtils.eTag(tag, getParams(format, params));
    }

    public void error(String tag, boolean printStack, String format, Object... params) {
        LogUtils.eTag(tag, getParams(format, params));
    }

    public void error(String tag, Throwable tr, String message, Object... params) {
        StackTraceElement stack = new Throwable().getStackTrace()[1];
        String log = params == null ? message : String.format(message, params);
        if (log == null) {
            log = "";
        }
        log += "  " + android.util.Log.getStackTraceString(tr);
        LogUtils.vTag(tag, getParams(message, params));
    }

    private List<Object> getParams(String format, Object... params) {
        List<Object> list = new ArrayList<>();
        list.add(format);
        if (params != null) {
            list.addAll(Arrays.asList(params));
        }
        return list;
    }
}
