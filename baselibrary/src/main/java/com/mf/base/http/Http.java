package com.mf.base.http;

import java.util.HashMap;
import java.util.Map;

public class Http {
    public static final String METHOD_POST = "POST";
    public static final String METHOD_GET = "GET";
    private String mURL = "";
    private String mQuery = "";
    /**
     * 请求参数
     */
    private Map<String, String> mParams = new HashMap<>();

    /**
     * 请求头
     */
    private Map<String, String> mHeader = new HashMap<>();

    /**
     * 请求body
     */
    private StringBuilder mBody = new StringBuilder();

    /**
     * 默认使用post方法
     */
    private String mMethod = METHOD_POST;

    public Http(String url, String query, String method) {
        mURL = url;
        mQuery = query;
        mMethod = method;
    }

    public String key() {
        return onGenerateKey(mURL, mQuery, mHeader, mParams, mBody.toString());
    }

    protected String onGenerateKey(final String url, final String query, final Map<String, String> headers, final Map<String, String> params, final String body) {
        return query;
    }

    /**
     * 请求方法
     *
     * @return
     */
    public String method() {
        return mMethod;
    }

    public void setMethod(String method) {
        mMethod = method;
    }

    /**
     * 返回请求url
     *
     * @return
     */
    public String url() {
        return mURL;
    }

    public void setURL(String url) {
        mURL = url;
    }

    public String query() {
        return mQuery;
    }

    /**
     * 返回请求参数
     *
     * @return
     */
    public Map<String, String> param() {
        return mParams;
    }

    public void addParam(String key, String value) {
        mParams.put(key, value);
    }

    /**
     * 返回请求header
     *
     * @return
     */
    public Map<String, String> header() {
        return mHeader;
    }

    public void addHeader(String key, String value) {
        mHeader.put(key, value);
    }


    public String body() {
        return mBody.toString();
    }

    public void setBody(String body) {
        mBody.append(body);
    }

    public void prepare() {
        onPrepare();
    }

    protected void onPrepare() {

    }

    @Override
    public String toString() {
        return "HttpTask{" +
                "mURL='" + mURL + '\'' +
                ", mQuery='" + mQuery + '\'' +
                ", mParams=" + mParams +
                ", mHeader=" + mHeader +
                ", mBody=" + mBody +
                ", mMethod='" + mMethod + '\'' +
                '}';
    }
}
