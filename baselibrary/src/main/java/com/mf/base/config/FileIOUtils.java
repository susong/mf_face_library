package com.mf.base.config;

import android.os.Environment;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 用于对本地文件的读写操作工具类
 *
 * @author Simon
 * @data: 2016/5/20 10:48
 * @version: V1.0
 */

class FileIOUtils {
    public static final String SDCARD = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String ROOT = SDCARD + "/mf/";
    public static final String CONFIG = ROOT + "/config/";
    public static final String LOG = ROOT + "/log/";
    public static final String CACHE = ROOT + "/cache/";
    public static final String WEBVIEW = ROOT + "/webview/";
    public static final String WEBVIEW_CACHE = WEBVIEW + "/cache/";
    public static final String DEBUG_DATA = ROOT + "/debug-data/";

    public static final String CACHE_MEDIA = CACHE + "media";
    public static final String CACHE_TTS = CACHE + "tts/";
    public static final String CACHE_TTS_TMP = CACHE_TTS + "tmp/";
    public static final String PHOTO_DIR = ROOT + "photo";

    public static final String AUSPACE_RESOURCE_DIR = SDCARD + "/auspace/";
    public static final String AUSPACE_ROBOT_BACKGROUND_DIR = SDCARD + "/auspace/robot/Background/";
    public static final String AUSPACE_ROBOT_ROOT_DIR = SDCARD + "/auspace/robot/";
    public static final String SHARE_MEDIA_CACHE_DIR = SDCARD + "/mediaCache/";
    public static final String APP_DIR = SDCARD + "/updateApkFile/";
    public static final String DOWNLOAD = ROOT + "/download";
    public static final String ROBOT_RESOURCE = ROOT + "robot/";

    /**
     * @ClassName: OnLineFilterListener
     * @Description: 读取文件行过滤器
     * @Author: duanbangchao
     * @CreateDate: 10/10/20
     * @UpdateUser: updater
     * @UpdateDate: 10/10/20
     * @UpdateRemark: 更新内容
     * @Version: 1.0
     */
    public interface OnLineFilterListener {
        /**
         * @param line -- 文件行内容
         * @return 返回false，表示忽略该行内容
         * @Author duanbangchao
         * @Time
         * @description 读取文件时行过滤器
         */
        boolean onLineFilter(String line);
    }

    public static boolean hasFile(String path) {
        File file = new File(path);
        return file.exists() && file.isFile() && file.length() > 0;
    }

    public static void write(String path, String content) {
        try {
            FileWriter fw = new FileWriter(path);
            fw.write(content);
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取指定文件
     *
     * @param path
     * @return
     */
    public static String readFile(String path) {
        StringBuilder builder = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
            String line = "";
            while (!TextUtils.isEmpty((line = reader.readLine()))) {
                builder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    public static List<String> read(String filename, OnLineFilterListener l) {
        if (hasFile(filename)) {
            try {
                InputStream stream = new FileInputStream(filename);
                return read(stream, l);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }

    public static List<String> read(InputStream stream, OnLineFilterListener l) {
        List<String> list = new ArrayList<>();
        BufferedReader reader = null;
        String line = null;
        try {
            reader = new BufferedReader(new InputStreamReader(stream));
            while (null != (line = reader.readLine())) {
                if (null != l && l.onLineFilter(line)) {
                    continue;
                }
                list.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return list;
    }

    /**
     * 创建路径
     *
     * @param path
     */
    public static void createDir(String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public static File[] list(String dir) {
        return list(new File(dir));
    }

    public static File[] list(File dir) {
        if (dir.exists() && dir.canRead() && dir.isDirectory()) {
            return dir.listFiles();
        }
        return null;
    }
}
