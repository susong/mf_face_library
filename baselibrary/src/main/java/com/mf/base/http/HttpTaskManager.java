package com.mf.base.http;


import android.content.Context;

import com.mf.log.LogUtils;
import com.mf.base.log.Logger;
import com.mf.base.log.LoggerFactory;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * @ClassName: HttpManager
 * @Description: 提供http任务管理
 * @Author: duanbangchao
 * @CreateDate: 11/27/20
 * @UpdateUser: updater
 * @UpdateDate: 11/27/20
 * @UpdateRemark: 更新内容
 * @Version: 1.0
 */
public class HttpTaskManager extends AbstractManager {
    private Logger mLogger = LoggerFactory.getLogger(HttpTaskManager.class);
    private static final String TAG = HttpTaskManager.class.getSimpleName();
    private HttpClient mClient = new HttpClient();

    @Override
    protected void onInit(Context context) {
    }

    @Override
    protected void onRelease() {
        mClient.release();
    }

    @Override
    public void dump(StringBuilder builder) {
        mClient.dump(builder);
    }

    private static class HttpTaskManagerHolder {
        private static HttpTaskManager INSTANCE = new HttpTaskManager();
    }

    private HttpTaskManager() {
    }

    public static HttpTaskManager getInstance() {
        return HttpTaskManagerHolder.INSTANCE;
    }

    public void execute(final Http task, final OnHttpResponseListener l, final Object extra) {
        task.prepare();
        mLogger.info(TAG, "execute:%s", task.toString());
        mClient.asyncQuery(task.url() + task.query(), task.param(), task.header(), task.method(), task.body(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mLogger.warn(TAG, "onFailure:%s", e.toString());
                if (null != l) {
                    l.onResponse(false, task.query(), OnHttpResponseListener.CODE_REQUEST_FAILED, null, extra);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                LogUtils.iTag(TAG, "onResponse:" + response.toString());
                if (null != l) {
                    l.onResponse(response.code() == 200, task.query(), response.code(), response, extra);
                }
            }
        });
    }

    public void execute(final Http task, final OnHttpResponseStringListener l, final Object extra) {
        task.prepare();
        mLogger.info(TAG, "execute:%s", task.toString());
        mClient.asyncQuery(task.url() + task.query(), task.param(), task.header(), task.method(), task.body(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mLogger.warn(TAG, "onFailure:%s", e.toString());
                if (null != l) {
                    l.onResponse(false, task.query(), OnHttpResponseListener.CODE_REQUEST_FAILED, null, extra);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                LogUtils.iTag(TAG, "onResponse:" + response.toString());
                if (null != l) {
                    l.onResponse(response.code() == 200, task.query(), response.code(), response.body().string(), extra);
                }
            }
        });
    }

    public Response executeSync(final Http task) throws IOException {
        task.prepare();
        mLogger.info(TAG, "execute:%s", task.toString());
        return mClient.syncQuery(task.url() + task.query(), task.param(), task.header(), task.method(), task.body());
    }

    public Response executeSync(String url) throws IOException {
        return mClient.syncQuery(url, null, null, "", null);
    }
}
