package com.mf.base.http;

import java.io.File;

public interface OnDownloadListener {
    /**
     * 下载成功之后的文件
     */
    void onDownloadSuccess(File file, Object extra);

    /**
     * 下载进度
     */
    void onDownloading(int progress, Object extra);

    /**
     * 下载异常信息
     */

    void onDownloadFailed(Exception e, Object extra);
}
