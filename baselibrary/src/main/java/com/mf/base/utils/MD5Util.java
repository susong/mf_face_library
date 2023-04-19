package com.mf.base.utils;

import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

/**
 * 获取指定字符串或是文件的MD5值
 *
 * @author Simon
 * @data: 2016/6/3 10:55
 * @version: V1.0
 */

public class MD5Util {

    /**
     * 十六进制下数字到字符的映射数组
     */
    private final static char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * 获取指定字符串的md5值
     *
     * @param chars
     * @return
     */
    public static String getMD5(String chars) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] results = md.digest(chars.getBytes());
            String resultString = byteArrayToHexString(results);
            return resultString.toUpperCase();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 返回小写的
     * @param chars
     * @return
     */
    public static String getMd5(String chars) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] results = md.digest(chars.getBytes());
            String resultString = byteArrayToHexString(results);
            return resultString;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 获取指定文件的md5值
     *
     * @param file
     * @return
     */
    public String getMD5(File file) {

        return getFileMD5(file);
    }

    /**
     * 获取单个文件的MD5值！

     * @param file
     * @return
     */

    public static String getFileMD5(File file) {
        if (!file.isFile()) {
            return "";
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        byte[] bytes = digest.digest();
        int n=bytes.length;
        StringBuffer stringbuffer = new StringBuffer(2 * n);
        for (int l = 0; l < n; l++) {
            byte bt=bytes[l];
            char c0 = hexDigits[(bt & 0xf0) >> 4];
            char c1 = hexDigits[bt & 0xf];
            stringbuffer.append(c0);
            stringbuffer.append(c1);
        }
        //BigInteger bigInt = new BigInteger(1, digest.digest());
        //String md5 = bigInt.toString(16);
        return stringbuffer.toString();
    }

    public static boolean equalsMD5(String raw, String target){
        if (TextUtils.isEmpty(target) || TextUtils.isEmpty(raw)){
            return false;
        }

        if (target.length() < 32 && raw.length() >= 32){
            return raw.equals(formatMD5(target));
        }else if (raw.length() < 32 && target.length() >= 32){
            return target.equals(formatMD5(raw));
        }else {
            return raw.equals(target);
        }

    }

    private static String formatMD5(String md5){
        StringBuffer buffer = new StringBuffer(md5);
        int dif = 32 - md5.length();
        for (int i = 0 ;i < dif ;i++){
            buffer.insert(0,"0");
        }
        return buffer.toString();
    }

    /**
     * 转换字节数组为16进制字串
     *
     * @param b
     *            字节数组
     * @return 十六进制字串
     */
    private static String byteArrayToHexString(byte[] b) {
        StringBuffer resultSb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            resultSb.append(byteToHexString(b[i]));
        }
        return resultSb.toString();
    }

    /**
     * 将一个字节转化成16进制形式的字符串
     *
     * @param b
     * @return
     */
    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0) {
            n = 256 + n;
        }
        int d1 = n / 16;
        int d2 = n % 16;
        return String.valueOf(hexDigits[d1]) + String.valueOf(hexDigits[d2]);
    }
}
