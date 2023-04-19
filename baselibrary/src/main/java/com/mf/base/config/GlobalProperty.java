package com.mf.base.config;

import android.text.TextUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import com.mf.log.LogUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: GlobalProperty
 * @Description: 加载配置文件信息，默认加载/sdcard/local.prop,
 * @Author: duanbangchao
 * @CreateDate: 10/10/20
 * @UpdateUser: updater
 * @UpdateDate: 10/10/20
 * @UpdateRemark: 更新内容
 * @Version: 1.0
 */
public class GlobalProperty extends Locker {
    private static final String TAG = GlobalProperty.class.getSimpleName();
    /**
     * 新文件配置信息
     */
    public static final String JSON_CONFIG_FILE = FileIOUtils.CONFIG + "/mf-global-config.json";
    /**
     * 云端配置保存在本地信息
     */
    private static final String CLOUD_CONFIG_FILE = FileIOUtils.CONFIG + "/local-config.json";
    /**
     * H5本地配置信息
     */
    private static final String H5_CONFIG_FILE = FileIOUtils.WEBVIEW + "/assets/config.prop";
    /**
     * Robot本地配置信息
     */
    private static final String ROBOT_CONFIG_FILE = FileIOUtils.SDCARD + "/auspace/config.prop";
    /**
     * 保存所有的配置信息
     */
    private Map<String, JsonElement> mConfigQuickMap = new HashMap<>(32);

    /**
     * 保存文件，以及该文件中的配置信息
     */
    private Map<String, Map<String, JsonElement>> mFileConfigs = new HashMap<>(4);

    private static class GlobalPropertyHolder {
        private static GlobalProperty INSTANCE = new GlobalProperty();
    }

    public static GlobalProperty getInstance() {
        return GlobalPropertyHolder.INSTANCE;
    }

    public void init() {
        lockWrite();
        // 初始化JsonConfigLoader
        JsonConfigLoader.getInstance().init();
        List<String> files = new ArrayList<>(4);

        // 加载并合并合并所有配置信息
        // step 1.合并旧版本配置信息
        // step 2.合并当前配置信息
        // step 3.合并应用配置信息
        // step 4.合并云端配置信息
        // List是有序的，按照存放数据顺序加载文件并合并配置信息
        files.add(JSON_CONFIG_FILE);
        files.add(H5_CONFIG_FILE);
        files.add(ROBOT_CONFIG_FILE);
        files.add(CLOUD_CONFIG_FILE);
        TimeCoster counter = new TimeCoster();
        for (String file : files) {
            Map<String, JsonElement> map = new HashMap<>(32);
            JsonConfigLoader.getInstance().loadFile(file, map);
            mFileConfigs.put(file, map);
            mConfigQuickMap.putAll(map);
        }

        LogUtils.v(TAG, String.format("init configs:size=%d cost：%fms", mConfigQuickMap.size(), (counter.end() / 1000.0f)));
        unlockWrite();
    }

    public JsonObject loadAll() {
        try {
            lockRead();
            JsonObject root = new JsonObject();
            for (Map.Entry<String, JsonElement> entry : mConfigQuickMap.entrySet()) {
                root.add(entry.getKey(), entry.getValue());
            }
            return root;
        } finally {
            unlockRead();
        }
    }

    public String getString(String key, String defaultValue) {
        try {
            lockRead();
            JsonElement element = mConfigQuickMap.get(key);
            if (null == element) {
                return defaultValue;
            }
            if (!element.isJsonPrimitive()) {
                LogUtils.w(TAG, String.format("获取%s失败，不是基本类型数据:%s", key, element.toString()));
                return defaultValue;
            }
            String result = element.getAsString();
            return TextUtils.isEmpty(result) ? defaultValue : result;
        } finally {
            unlockRead();
        }
    }

    public int getInt(String key, int defaultValue) {
        String result = getString(key, "");
        return parseInt(result, defaultValue);
    }

    public long getLong(String key, long defaultValue) {
        String result = getString(key, "");
        return parseLong(result, defaultValue);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String result = getString(key, "");
        return parseBoolean(result, defaultValue);
    }

    public int parseInt(String str, int defaultValue) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public long parseLong(String str, long defaultValue) {
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public boolean parseBoolean(String str, boolean defaultValue) {
        try {
            return Boolean.parseBoolean(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public void putString(String key, String value) {
        Map<String, JsonElement> map = mFileConfigs.get(CLOUD_CONFIG_FILE);
        if (map == null) {
            map = new HashMap<>();
        }
        JsonPrimitive jsonPrimitive = new JsonPrimitive(value);
        map.put(key, jsonPrimitive);
    }

    public void save() {
        try {
            lockWrite();
            save(CLOUD_CONFIG_FILE, mFileConfigs.get(CLOUD_CONFIG_FILE));
        } finally {
            unlockWrite();
        }
    }

    private void save(final String file, final Map<String, JsonElement> map) {
        if (file == null) {
            return;
        }
        if (map == null) {
            return;
        }
        JsonObject json = new JsonObject();
        for (Map.Entry<String, JsonElement> entry : map.entrySet()) {
            json.add(entry.getKey(), entry.getValue());
        }
        try {
            StringWriter stringWriter = new StringWriter();
            JsonWriter jsonWriter = new JsonWriter(stringWriter);
            jsonWriter.setIndent("    ");
            Streams.write(json, jsonWriter);
            FileIOUtils.write(file, stringWriter.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
