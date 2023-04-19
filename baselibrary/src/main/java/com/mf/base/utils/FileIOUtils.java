package com.mf.base.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.google.gson.GsonBuilder;
import com.mf.base.log.Logger;
import com.mf.base.log.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;


/**
 * 用于对本地文件的读写操作工具类
 *
 * @author Simon
 * @data: 2016/5/20 10:48
 * @version: V1.0
 */

public class FileIOUtils {
    private static final String TAG = FileIOUtils.class.getSimpleName();
    private static Logger mLogger = LoggerFactory.getLogger(FileIOUtils.class);
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
    public static final String PHOTO_DIR = ROOT + "photo";

    public static final String AUSPACE_RESOURCE_DIR = SDCARD + "/auspace/";
    public static final String AUSPACE_ROBOT_BACKGROUND_DIR = SDCARD + "/auspace/robot/Background/";
    public static final String AUSPACE_ROBOT_ROOT_DIR = SDCARD + "/auspace/robot/";
    public static final String SHARE_MEDIA_CACHE_DIR = SDCARD + "/mediaCache/";
    public static final String APP_DIR = SDCARD + "/updateApkFile/";
    public static final String DOWNLOAD = ROOT + "/download";
    public static final String ROBOT_RESOURCE = ROOT + "robot/";

    static {
        String[] dirs = {ROOT, CONFIG, LOG, CACHE, WEBVIEW,
                WEBVIEW_CACHE, DEBUG_DATA,
                CACHE_TTS, CACHE_MEDIA,
                PHOTO_DIR, AUSPACE_ROBOT_ROOT_DIR, APP_DIR, DOWNLOAD, ROBOT_RESOURCE};
        for (String dir : dirs) {
            createDir(dir);
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

    public static void cleanDir(String dir) {
        File[] files = list(dir);
        if (null == files) {
            return;
        }
        for (File file : files) {
            if (file.isFile()) {
                deleteFile(file.getAbsolutePath());
            } else if (file.isDirectory()) {
                deleteDirectory(file.getAbsolutePath());
            }
        }
    }

    /**
     * 从URL中获取文件名
     *
     * @param url
     * @return
     */
    public static String getFileNameFromURL(String url) {
        return getFileNameFromURL(url, "");
    }

    /**
     * 从url中获取文件名。
     * 1.如果md5 存在，从url中获取文件后缀，如果后缀存在，那么返回md5+文件后缀，否则md5 +".unknown";如果md5不存在，进行2;
     * 2.获取url中最后一个"/"的字符串，如果该字符串存在，那么使用该字符串作为文件名;如果该字符串不存在，使用url的md5作为文件名
     *
     * @param url 在线文件URL
     * @param md5 md5
     * @return
     */
    public static String getFileNameFromURL(String url, String md5) {
        if (!TextUtils.isEmpty(md5)) {
            int index = url.lastIndexOf(".");
            if (index > 0) {
                return md5 + url.substring(index);
            }
            return md5 + ".unknown";
        }
        int index = url.lastIndexOf("/");
        if (index > 0) {
            return url.substring(index + 1);
        }
        return MD5Util.getMD5(url);
    }

    /**
     * 格式化对象成JSON并保存到指定文件
     * 文件路径为/sdcard/gowild/debug-data/
     *
     * @param file
     * @param src
     */
    public static void saveDebugData(String file, Object src) {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        saveLine(DEBUG_DATA + "/" + file, builder.create().toJson(src));
    }

    /**
     * 格式化对象成JSON并保存到指定文件
     * 文件路径为/sdcard/gowild/debug-data/
     *
     * @param file
     * @param txt
     */
    public static void saveDebugData(String file, String txt) {
        saveLine(DEBUG_DATA + "/" + file, txt);
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

    /**
     * 从指定目录读取一行数据
     *
     * @param path
     * @param encode 指定读取编码格式
     * @return 文件中的首行数据，如果为空则返回""
     */
    public static String readSingleLine(String path, String encode) {
        // 将本地缓存的数据读出保存在内存中
        File file = new File(path);
        BufferedReader reader = null;
        String lineChars = "";
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encode));
            lineChars = reader.readLine();
        } catch (FileNotFoundException ioEx) {
            mLogger.error(TAG, "%s is not found !!!", path);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return lineChars == null ? "" : lineChars;
    }

    /**
     * 获取异常信息保存文件
     *
     * @param context
     * @return
     */
    @SuppressLint("NewApi")
    public static File getCatchFile(Context context) throws NullPointerException {
        return new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsoluteFile()
                + "/" + DateTimeUtil.getLogFormatOutput() + "catch.log");
    }

    /**
     * 获取异常信息保存文件目录
     *
     * @param context
     * @return
     */
    @SuppressLint("NewApi")
    public static File getCatchFileDir(Context context) {
        return new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsoluteFile() + "/");
    }


    public static long time = 0;

    /**
     * 保存字符串到本地
     *
     * @param content 追加的内容
     */
    public static void saveLogcat(Context context, String content) throws NullPointerException {
        String fileName = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsoluteFile() + "/" + "log.txt";
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(fileName, true);
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(
                    fileOutputStream, "UTF-8"));
            bufferedWriter.write(content + "\n");
            bufferedWriter.flush();
            bufferedWriter.close();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveLine(String path, String content) {
        saveLine(path, content, false);
    }

    public static void saveLine(String path, String content, boolean apped) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(path, apped);
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(
                    fileOutputStream, "UTF-8"));
            bufferedWriter.write(content + "\n");
            bufferedWriter.flush();
            bufferedWriter.close();
            fileOutputStream.close();
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

    public static final String BASICFILES[] = {"Alarms", "Android", "DCIM", "Download", "Movies", "Music", "Notifications",
            "Pictures", "Podcasts", "Ringtones", "auspace", "errorMessage.txt", "line.txt", "msc", "updateApkFile",
            "updateFir", "zhc", "code_cache"};

    /**
     * 监测是否是基本文件，不是的话就删除掉
     */
    public static void checkBasicFile() {
        File sdcard = new File(SDCARD);
        File[] files = sdcard.listFiles();
        for (File file : files) {
            System.out.println("名字" + file.getName() + " 时间 == " + System.currentTimeMillis() + "  地址" + file.getAbsolutePath());
            if (!isBasicFile(file.getName())) {
                System.out.println("delete = " + file.getAbsolutePath());
            }
        }
    }

    public static boolean isBasicFile(String fileName) {
        boolean isBasic = false;
        for (int i = 0; i < BASICFILES.length; i++) {
            isBasic = fileName.startsWith(BASICFILES[i]);
            if (isBasic) {
                break;
            }
        }
        return isBasic;
    }

    /**
     * 删除单个文件
     *
     * @param filePath 被删除文件的文件名
     * @return 文件删除成功返回true，否则返回false
     */
    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            return file.delete();
        }
        return false;
    }

    /**
     * 删除文件夹以及目录下的文件
     *
     * @param filePath 被删除目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    public static boolean deleteDirectory(String filePath) {
        boolean flag = false;
        //如果filePath不以文件分隔符结尾，自动添加文件分隔符
        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator;
        }
        File dirFile = new File(filePath);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        flag = true;
        File[] files = dirFile.listFiles();
        if (files != null) {
            //遍历删除文件夹下的所有文件(包括子目录)
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) {
                    //删除子文件
                    flag = deleteFile(files[i].getAbsolutePath());
                    if (!flag) {
                        break;
                    }
                } else {
                    //删除子目录
                    flag = deleteDirectory(files[i].getAbsolutePath());
                    if (!flag) {
                        break;
                    }
                }
            }
        }
        if (!flag) {
            return false;
        }
        //删除当前空目录
        return dirFile.delete();
    }

    /**
     * 解压一个压缩的目录
     *
     * @param inputFile -- 源文件
     * @param targetDir -- 解压到指定路径
     * @throws Exception
     */
    public static boolean unzipFolder(String inputFile, String targetDir) throws Exception {
        File srcFile = new File(inputFile);
        // 判断源文件是否存在
        if (!srcFile.exists()) {
            return false;
        }
        createDir(targetDir);
        //创建压缩文件对象
        ZipFile source = new ZipFile(srcFile);
        Map<String, ZipEntry> fileTree = new TreeMap<>();
        // 获取压缩文件信息
        Enumeration<?> entries = source.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            fileTree.put(entry.getName(), entry);
        }
        /**
         * 因为是压缩目录，而TreeMap是升序的，如果地一个ZipEntry不是目录直接退出
         */
        String sourceDir = "";
        boolean firstEntry = false;
        String dest = "";
        for (String key : fileTree.keySet()) {
            if (!firstEntry) {
                firstEntry = true;
                if (!fileTree.get(key).isDirectory()) {
                    mLogger.warn(TAG, "unzipFolder不是一个目录压缩包");
                    return false;
                }
                sourceDir = key;
                continue;
            }
            dest = targetDir + "/" + key.replaceFirst(sourceDir, "");
            if (fileTree.get(key).isDirectory()) {
                createDir(dest);
            } else {
                boolean success = write(source.getInputStream(fileTree.get(key)), dest);
                source.getInputStream(fileTree.get(key)).close();
                if (!success) {
                    mLogger.warn(TAG, "unzipFolder failed:%s->%s", key, dest);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 根据路径删除指定的目录或文件，无论存在与否
     *
     * @param filePath 要删除的目录或文件
     * @return 删除成功返回 true，否则返回 false。
     */
    public static boolean DeleteFolder(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return false;
        } else {
            if (file.isFile()) {
                // 为文件时调用删除文件方法
                return deleteFile(filePath);
            } else {
                // 为目录时调用删除目录方法
                return deleteDirectory(filePath);
            }
        }
    }

    public static boolean copyFile(String origin, String dest) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File originFile = new File(origin);
            //读入原文件
            InputStream inStream = new FileInputStream(origin);
            FileOutputStream fs = new FileOutputStream(dest);
            byte[] buffer = new byte[1444];
            while ((byteread = inStream.read(buffer)) != -1) {
                //字节数文件大小
                bytesum += byteread;
                fs.write(buffer, 0, byteread);
            }
            inStream.close();
            return true;
        } catch (Exception e) {
            mLogger.debug(TAG, "occur error %s", e.toString());
        }
        return false;
    }

    public static void checkDir(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (!dir.isDirectory()) {
            dir.delete();
            dir.mkdirs();
        }
    }

    public static void copyExtendFile(Context context) {

        File targetFile = new File("/sdcard/extend.sh");
        // 复制到指定位置
        FileIOUtils.copyAssetsToStorage(context, "extend.sh", targetFile);

    }

    public static void copyFileToStorage(Context context, String filePath, File targetFile) {
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            if (!targetFile.getParentFile().exists()) {
                targetFile.getParentFile().mkdirs();
            }
            targetFile.createNewFile();
            inputStream = new FileInputStream(filePath);
            outputStream = new FileOutputStream(targetFile);
            byte[] buffer = new byte[1024];
            int readLen = 0;
            while ((readLen = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, readLen);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void copyAssetsToStorage(Context context, String fileName, File targetFile) {
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            targetFile.getParentFile().mkdirs();
            targetFile.createNewFile();
            inputStream = context.getAssets().open(fileName);
            outputStream = new FileOutputStream(targetFile);
            byte[] buffer = new byte[1024];
            int readLen = 0;
            while ((readLen = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, readLen);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

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

    public static List<String> read(String filename, OnLineFilterListener l) {
        if (hasFile(filename)) {
            try {
                InputStream stream = new FileInputStream(filename);
                return read(stream, l);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<String>();
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

    public static boolean exists(String path) {
        File file = new File(path);
        return file.exists();
    }

    public static boolean hasFile(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        File file = new File(path);
        return file.exists() && file.isFile() && file.length() > 0;
    }

    public static boolean hasFile(File file) {
        if (file == null) {
            return false;
        }
        return file.exists() && file.length() > 0;
    }

    public static boolean hasFile(File file,long minSize){
        if (file == null){
            return false;
        }
        return file.exists() && file.length() > minSize;
    }

    public static void writeDebugData(String path, String content) {
        write(DEBUG_DATA + "/" + path, content);
    }

    public static void write(String path, byte[] data, int offset, int length) {
        try {
            FileOutputStream os = new FileOutputStream(path);
            os.write(data, offset, length);
            os.flush();
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public static boolean write(InputStream is, String file) {
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[8 * 1024];
            int read = -1;
            while ((read = is.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
                outputStream.flush();
            }
        } catch (IOException e) {
            return false;
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return true;
    }

    public interface onFileWriteListener {
        void onWriting(int read);
    }

    public static boolean write(InputStream is, String file, onFileWriteListener listener) {
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[8 * 1024];
            int read = -1;
            while ((read = is.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
                listener.onWriting(read);
                outputStream.flush();
            }
        } catch (IOException e) {
            return false;
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return true;
    }

    public static void write(String path, List<String> lines) {
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            sb.append(line);
            sb.append("\n");
        }
        write(path, sb.toString());
    }

    public static boolean isLocalFile(String path) {
        return !path.startsWith("http") && !path.startsWith("https");
    }

    /**
     * oldPath 和 newPath必须是新旧文件的绝对路径
     */
    public static File renameFile(String oldPath, String newPath) {
        if (TextUtils.isEmpty(oldPath)) {
            return null;
        }

        if (TextUtils.isEmpty(newPath)) {
            return null;
        }
        File oldFile = new File(oldPath);
        File newFile = new File(newPath);
        boolean b = oldFile.renameTo(newFile);
        File file2 = new File(newPath);
        return file2;
    }

    /**
     * 解压zip到指定的路径
     *
     * @param zipFileString ZIP的名称
     * @param outPathString 要解压缩路径
     * @throws Exception
     */
    public static void UnZipFolder(String zipFileString, String outPathString) throws Exception {
        ZipInputStream inZip = new ZipInputStream(new FileInputStream(zipFileString));
        ZipEntry zipEntry;
        String szName = "";
        while ((zipEntry = inZip.getNextEntry()) != null) {
            szName = zipEntry.getName();
            if (zipEntry.isDirectory()) {
                //获取部件的文件夹名
                szName = szName.substring(0, szName.length() - 1);
                File folder = new File(outPathString + File.separator + szName);
                folder.mkdirs();
            } else {
                mLogger.error(TAG, outPathString + File.separator + szName);
                File file = new File(outPathString + File.separator + szName);
                if (!file.exists()) {
                    mLogger.error(TAG, "Create the file:" + outPathString + File.separator + szName);
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                }
                // 获取文件的输出流
                FileOutputStream out = new FileOutputStream(file);
                int len;
                byte[] buffer = new byte[1024];
                // 读取（字节）字节到缓冲区
                while ((len = inZip.read(buffer)) != -1) {
                    // 从缓冲区（0）位置写入（字节）字节
                    out.write(buffer, 0, len);
                    out.flush();
                }
                out.close();
            }
        }
        inZip.close();
    }
}
