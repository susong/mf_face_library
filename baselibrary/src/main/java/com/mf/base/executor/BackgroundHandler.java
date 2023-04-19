package com.mf.base.executor;

import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Printer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

/**
 * 后台Handler, 对 {@link Handler} 和 {@link HandlerThread}进行封装
 */
public class BackgroundHandler {
    private HandlerThread mThread = null;
    private Handler mHandler = null;

    public BackgroundHandler(String name) {
        mThread = new HandlerThread(name);
        mThread.start();
        mHandler = new Handler(mThread.getLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                BackgroundHandler.this.handleMessage(msg);
            }
        };
    }

    public final Message obtainMessage() {
        return mHandler.obtainMessage();
    }

    public final Message obtainMessage(int what) {
        return mHandler.obtainMessage(what);
    }

    public final Message obtainMessage(int what, @Nullable Object obj) {
        return mHandler.obtainMessage(what, obj);
    }

    public final Message obtainMessage(int what, int arg1, int arg2) {
        return mHandler.obtainMessage(what, arg1, arg2);
    }

    public final Message obtainMessage(int what, int arg1, int arg2, @Nullable Object obj) {
        return mHandler.obtainMessage(what, arg1, arg2, obj);
    }

    public final boolean post(@NonNull Runnable r) {
        return mHandler.post(r);
    }

    public final boolean postAtTime(@NonNull Runnable r, long uptimeMillis) {
        return mHandler.postAtTime(r, uptimeMillis);
    }

    public final boolean postAtTime(@NonNull Runnable r, @Nullable Object token, long uptimeMillis) {
        return mHandler.postAtTime(r, token, uptimeMillis);
    }

    public final boolean postDelayed(@NonNull Runnable r, long delayMillis) {
        return mHandler.postDelayed(r, delayMillis);
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public final boolean postDelayed(Runnable r, int what, long delayMillis) {
        return mHandler.postDelayed(r, what, delayMillis);
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public final boolean postDelayed(@NonNull Runnable r, @Nullable Object token, long delayMillis) {
        return mHandler.postDelayed(r, token, delayMillis);
    }

    public final boolean postAtFrontOfQueue(@NonNull Runnable r) {
        return mHandler.postAtFrontOfQueue(r);
    }

    public final void removeCallbacks(@NonNull Runnable r) {
        mHandler.removeCallbacks(r);
    }

    public final void removeCallbacks(@NonNull Runnable r, @Nullable Object token) {
        mHandler.removeCallbacks(r, token);
    }

    public final boolean sendMessage(@NonNull Message msg) {
        return mHandler.sendMessage(msg);
    }

    public final boolean sendEmptyMessage(int what) {
        return mHandler.sendEmptyMessage(what);
    }

    public final boolean sendEmptyMessageDelayed(int what, long delayMillis) {
        return mHandler.sendEmptyMessageDelayed(what, delayMillis);
    }

    public final boolean sendEmptyMessageAtTime(int what, long uptimeMillis) {
        return mHandler.sendEmptyMessageAtTime(what, uptimeMillis);
    }

    public final boolean sendMessageDelayed(@NonNull Message msg, long delayMillis) {
        return mHandler.sendMessageDelayed(msg, delayMillis);
    }

    public boolean sendMessageAtTime(@NonNull Message msg, long uptimeMillis) {
        return mHandler.sendMessageAtTime(msg, uptimeMillis);
    }

    public final boolean sendMessageAtFrontOfQueue(@NonNull Message msg) {
        return mHandler.sendMessageAtFrontOfQueue(msg);
    }

    public final void removeMessages(int what) {
        mHandler.removeMessages(what);
    }

    public final void removeMessages(int what, @Nullable Object object) {
        mHandler.removeMessages(what, object);
    }

    public final void removeCallbacksAndMessages(@Nullable Object token) {
        mHandler.removeCallbacksAndMessages(token);
    }

    public final boolean hasMessages(int what) {
        return mHandler.hasMessages(what);
    }

    public final boolean hasMessages(int what, @Nullable Object object) {
        return mHandler.hasMessages(what, object);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public final boolean hasCallbacks(@NonNull Runnable r) {
        return mHandler.hasCallbacks(r);
    }

    @NonNull
    public final Looper getLooper() {
        return mHandler.getLooper();
    }

    public final void dump(@NonNull Printer pw, @NonNull String prefix) {
        mHandler.dump(pw, prefix);
    }

    public void dump(StringBuilder builder) {
        builder.append("null == mThread:");
        builder.append(null == mThread);
        builder.append(", null == mHandler:");
        builder.append(null == mHandler);
        if (null != mThread) {
            builder.append(", Name:");
            builder.append(mThread.getName());
            builder.append(", State:");
            builder.append(mThread.getState());
        }
    }

    public void handleMessage(@NonNull Message msg) {
    }
}
