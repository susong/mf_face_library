package com.mf.base.http;

/**
 * http回调数据接口, 对OkHttp返回数据进行封装
 */
public interface OnHttpResponseStringListener {
    static final int CODE_REQUEST_FAILED = -1;
    static final int CODE_RETRY_TIMEOUT = -2;

    /**
     * http请求回调接口
     *
     * @param success  -- 是否成功
     * @param query    -- 发送的query请求
     * @param code     -- 响应code
     * @param response -- 响应返回数据
     * @param extra    -- 附带数据
     */
    void onResponse(boolean success, String query, int code, String response, Object extra);
}
