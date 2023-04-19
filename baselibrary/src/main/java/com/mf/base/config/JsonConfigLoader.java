package com.mf.base.config;

import android.text.TextUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.mf.log.LogUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @ClassName: JsonConfigLoader
 * @Description: 单个配置文件加载器
 * @Author: duanbangchao
 * @CreateDate: 4/20/21
 * @UpdateUser: updater
 * @UpdateDate: 4/20/21
 * @UpdateRemark: 更新内容
 * @Version: 1.0
 */
final class JsonConfigLoader {
    private static final String JSON_CONFIG_DIR = FileIOUtils.CONFIG + "/json/";
    private static final String TAG = JsonConfigLoader.class.getSimpleName();
    private static final String KEY_SPLITTER = "=";
    private static final String KEY_COMMENT = "#";

    private static class SingletonHolder {
        private static JsonConfigLoader INSTANCE = new JsonConfigLoader();
    }

    public static JsonConfigLoader getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 初始化
     */
    public void init() {
        FileIOUtils.createDir(JSON_CONFIG_DIR);
    }

    /**
     * 加载配置信息,优先使用json来解析，如果解析失败使用=分割符来解析
     *
     * @param file
     * @param elements
     */
    public void loadFile(String file, Map<String, JsonElement> elements) {
        if (!FileIOUtils.hasFile(file)) {
            return;
        }
        String data = FileIOUtils.readFile(file);
        try {
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(data);
            if (element.isJsonObject()) {
                merge(elements, element.getAsJsonObject().entrySet());
            }
        } catch (JsonSyntaxException e) {
            LogUtils.w(TAG, String.format("使用json解析文件%s失败，尝试使用=分割符号来解析", file));
            loadFile(file, elements, KEY_SPLITTER);
        }
    }

    private JsonConfigLoader() {
    }


    private void merge(Map<String, JsonElement> dst, final Set<Map.Entry<String, JsonElement>> src) {
        for (Map.Entry<String, JsonElement> entry : src) {
            dst.put(entry.getKey(), entry.getValue().deepCopy());
        }
    }

    /**
     * 加载使用=分割符加载配置文件信息
     */
    private void loadFile(String file, Map<String, JsonElement> elements, final String splitter) {
        if (!FileIOUtils.hasFile(file)) {
            return;
        }
        List<String> lines = FileIOUtils.read(file, new FileIOUtils.OnLineFilterListener() {
            @Override
            public boolean onLineFilter(String line) {
                return line.startsWith(KEY_COMMENT);
            }
        });
        LogUtils.i(TAG, String.format("lines:%d", lines.size()));
        for (String line : lines) {
            String[] args = line.split(splitter);
            if (null == args || args.length != 2 || TextUtils.isEmpty(args[0])) {
                LogUtils.i(TAG, String.format("loadOldConfigFile line:%s", line));
                continue;
            }
            elements.put(args[0], new JsonPrimitive(args[1]));
        }
    }
}
