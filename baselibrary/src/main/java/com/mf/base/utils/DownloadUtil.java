package com.mf.base.utils;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.mf.log.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Dns;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public enum DownloadUtil {
    INSTANCE;

    public static DownloadUtil getInstance() {
        return INSTANCE;
    }

    private static final String TAG = DownloadUtil.class.getSimpleName();
    private final String uCloudUrl = "https://media-parking.codfly.cn";

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .connectTimeout(3, TimeUnit.SECONDS) // 连接超时
            .dns(new XDns(3, TimeUnit.SECONDS)) // DNS超时
            .build();

    public void download(String url, String saveDir, String fileName, String fileMd5, Object extra, OnDownloadListener listener) {
        checkQiniuCloud(url, saveDir, fileName, fileMd5, extra, listener);
    }

    private void realDownload(String url, String saveDir, String fileName, String fileMd5, Object extra, OnDownloadListener listener) {
        try {
            File dir = new File(saveDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, fileName);
            // 判断文件是否存在，并且md5一致
            if (file.exists() && MD5Util.getFileMD5(file).equals(fileMd5)) {
                LogUtils.i("file exist:" + file.getAbsolutePath());
                if (listener != null) {
                    listener.onDownloadSuccess(true, file, extra);
                }
                return;
            }
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    LogUtils.e("下载文件失败 url:" + url + " saveDir:" + saveDir + " fileName:" + fileName + " extra:" + extra, e);
                    if (listener != null) {
                        listener.onDownloadFailed(e, extra);
                    }
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    ResponseBody body = response.body();
                    if (body == null) {
                        LogUtils.w("download failed response body is null");
                        if (listener != null) {
                            listener.onDownloadFailed(new RuntimeException("download failed response body is null"), extra);
                        }
                        return;
                    }
                    long total = body.contentLength();
                    LogUtils.i("download size:" + total);
                    InputStream is = body.byteStream();
                    BufferedInputStream input = new BufferedInputStream(is);
                    OutputStream output = new FileOutputStream(file);
                    byte[] buf = new byte[1024 * 10];
                    int len = 0;
                    long sum = 0;
                    int progress = 0;
                    while ((len = input.read(buf)) != -1) {
                        output.write(buf, 0, len);
                        sum += len;
                        int newProgress = (int) (sum * 1.0f / total * 100);
                        if (newProgress != progress) {
                            progress = newProgress;
                            LogUtils.i("download progress:" + progress);
                            if (listener != null) {
                                listener.onDownloading(progress, extra);
                            }
                        }
                    }
                    output.flush();
                    output.close();
                    input.close();

                    LogUtils.i("download success path:" + file.getAbsolutePath());
                    if (listener != null) {
                        listener.onDownloadSuccess(false, file, extra);
                    }
                }
            });
        } catch (Exception e) {
            LogUtils.e("下载文件失败 url:" + url + " saveDir:" + saveDir + " fileName:" + fileName + " extra:" + extra, e);
            if (listener != null) {
                listener.onDownloadFailed(e, extra);
            }
        }
    }

    private void checkQiniuCloud(String url, String saveDir, String fileName, String fileMd5, Object extra, OnDownloadListener listener) {
        if (!url.startsWith(uCloudUrl)) {
            realDownload(url, saveDir, fileName, fileMd5, extra, listener);
            return;
        }
        httpClient.newCall(new Request.Builder().url(url + "?hash/md5").build())
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        LogUtils.e("获取MD5失败 url:" + url + " saveDir:" + saveDir + " fileName:" + fileName + " extra:" + extra, e);
                        realDownload(url, saveDir, fileName, fileMd5, extra, listener);
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        String md5;
                        try {
                            JSONObject jsonObject = new JSONObject(response.body().string());
                            String str = jsonObject.optString("md5");
                            if (!TextUtils.isEmpty(str)) {
                                md5 = str;
                            } else {
                                md5 = fileMd5;
                            }
                        } catch (JSONException e) {
                            LogUtils.e("json解析异常", e);
                            md5 = fileMd5;
                        }
                        realDownload(url, saveDir, fileName, md5, extra, listener);
                    }
                });
    }

    public interface OnDownloadListener {
        /**
         * 下载成功之后的文件
         */
        void onDownloadSuccess(boolean isExist, File file, Object extra);

        /**
         * 下载进度
         */
        void onDownloading(int progress, Object extra);

        /**
         * 下载异常信息
         */

        void onDownloadFailed(Exception e, Object extra);
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
