package com.mf.base.http;

import android.content.Context;

import com.mf.base.log.Logger;
import com.mf.base.log.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;


/**
 * @ClassName: AbstractManager
 * @Description: java类作用描述
 * @Author: duanbangchao
 * @CreateDate: 9/15/21
 * @UpdateUser: updater
 * @UpdateDate: 9/15/21
 * @UpdateRemark: 更新内容
 * @Version: 1.0
 */
public abstract class AbstractManager {
    protected Logger mLogger = LoggerFactory.getLogger(AbstractManager.class);
    private AtomicBoolean mReady = new AtomicBoolean(false);
    private Context mContext = null;

    public void init0(Context context) {
    }

    public void init(Context context) {
        mContext = context;
        onInit(mContext);
        setReady(true);
    }

    public Context getContext() {
        return mContext;
    }

    public void setReady(boolean ready) {
        mReady.set(ready);
    }

    public boolean isReady() {
        return mReady.get();
    }

    public void release() {
        if (!mReady.get()) {
            return;
        }

        onRelease();
    }

    public void dump() {
        dump(getClass().getSimpleName());
    }

    public void dump(String tag) {
        StringBuilder builder = new StringBuilder(128);
        builder.append(getClass().getSimpleName());
        builder.append("{Ready:");
        builder.append(mReady.get());
        builder.append(",");
        dump(builder);
        builder.append("}");
        mLogger.debug(tag, builder.toString());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(128);
        builder.append(getClass().getSimpleName());
        builder.append("{Ready=");
        builder.append(mReady.get());
        builder.append(",");
        dump(builder);
        builder.append("}");
        return builder.toString();
    }


    protected abstract void onInit(Context context);

    protected abstract void onRelease();

    public abstract void dump(StringBuilder builder);
}
