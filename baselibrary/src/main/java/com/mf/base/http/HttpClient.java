package com.mf.base.http;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Dns;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

public class HttpClient {
    private static final String TAG = HttpClient.class.getSimpleName();
    private OkHttpClient mClient = null;

    public HttpClient() {
        mClient = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .connectTimeout(3, TimeUnit.SECONDS) // 连接超时
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .dns(new XDns(3, TimeUnit.SECONDS)) // DNS超时
                .build();
    }

    public void release() {
        mClient.dispatcher().cancelAll();
    }

    public void dump(StringBuilder builder) {
        List<Call> calls = mClient.dispatcher().queuedCalls();
        builder.append("Queue Calls:");
        builder.append(calls.size());
        for (Call call : calls) {
            builder.append("\n\t");
            builder.append(call.request().toString());
        }
        List<Call> runnings = mClient.dispatcher().runningCalls();
        builder.append("Running Calls:");
        builder.append(runnings.size());
        for (Call call : runnings) {
            builder.append("\n\t");
            builder.append(call.request().toString());
        }
    }

    public void asyncQuery(String url, Map<String, String> params, Map<String, String> headers, String method, String body, Callback cb) {
        final Request request = createRequest(url, params, headers, method, body);
        mClient.newCall(request).enqueue(cb);
    }

    public Response syncQuery(String url, Map<String, String> params, Map<String, String> headers, String method, String body) throws IOException {
        final Request request = createRequest(url, params, headers, method, body);
        return mClient.newCall(request).execute();
    }

    public void download(String url, long downloadLength, Callback callback) {
        Request request = new Request.Builder()
                .addHeader("RANGE", "bytes=" + downloadLength + "-" + getContentLength(url))
                .url(url)
                .build();
        mClient.newCall(request).enqueue(callback);
    }

    public void requestFileInfo(String url, Callback callback) {
        final Request request = new Request.Builder()
                .url(url)
                .build();
        mClient.newCall(request).enqueue(callback);
    }

    /**
     * 获取下载长度
     *
     * @param downloadUrl
     * @return
     */
    private long getContentLength(String downloadUrl) {
        Request request = new Request.Builder()
                .url(downloadUrl)
                .build();
        try {
            Response response = mClient.newCall(request).execute();
            if (response != null && response.isSuccessful()) {
                long contentLength = response.body().contentLength();
                response.close();
                return contentLength == 0 ? -1 : contentLength;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private Request createRequest(String url, Map<String, String> params, Map<String, String> headers, String method, String body) {
        AtomicReference<Request.Builder> builder = new AtomicReference<>(new Request.Builder());
        HttpUrl httpUrl = createHttpUrl(url, params);
        builder.get().url(httpUrl);
        if (null != headers) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                builder.get().addHeader(header.getKey(), header.getValue());
            }
        }
        if (method.equals(Http.METHOD_POST)) {
            if (null != params) {
                FormBody.Builder builder0 = new FormBody.Builder();
                for (Map.Entry<String, String> param : params.entrySet()) {
                    builder0.addEncoded(param.getKey(), param.getValue());
                }
                builder.get().post(builder0.build());
            }

            if (!TextUtils.isEmpty(body)) {
                //MediaType  设置Content-Type 标头中包含的媒体类型值
                RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8"), body);
                builder.get().post(requestBody);
            }
        }
        return builder.get().build();
    }

    private HttpUrl createHttpUrl(String url, Map<String, String> params) {
        HttpUrl.Builder builder = HttpUrl.parse(url).newBuilder();
        if (null != params) {
            for (Map.Entry<String, String> param : params.entrySet()) {
                builder.addQueryParameter(param.getKey(), param.getValue());
            }
        }
        return builder.build();
    }

    /**
     * OKHttp RequestBody的MediaType会默认添加charset=utf-8,但是易停车场项目如果添加charset=utf-8后服务端POST请求不能正确获取Body
     */
    private class PostRequestBody extends RequestBody {
        private final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");
        private String mBody = null;

        PostRequestBody(String body) {
            mBody = body;
        }

        @Override
        public MediaType contentType() {
            return MEDIA_TYPE_JSON;
        }

        @Override
        public long contentLength() throws IOException {
            // 如果mBody有中文字符，mBody.length()和mBody.getBytes("utf-8").length不一致，导致无法想服务器写数据会报错
            return mBody.getBytes("utf-8").length;
        }

        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            sink.write(mBody.getBytes("utf-8"));
        }
    }

    public static class XDns implements Dns {
        private final long timeout;
        private final TimeUnit unit;

        public XDns(long timeout, TimeUnit unit) {
            this.timeout = timeout;
            this.unit = unit;
        }

        @NonNull
        @Override
        public List<InetAddress> lookup(@NonNull final String hostname) throws UnknownHostException {
            try {
                FutureTask<List<InetAddress>> task = new FutureTask<>(() ->
                        Arrays.asList(InetAddress.getAllByName(hostname)));
                new Thread(task).start();
                return task.get(timeout, unit);
            } catch (Exception e) {
                UnknownHostException unknownHostException =
                        new UnknownHostException("Broken system behaviour for dns lookup of " + hostname);
                unknownHostException.initCause(e);
                throw unknownHostException;
            }
        }
    }
}
