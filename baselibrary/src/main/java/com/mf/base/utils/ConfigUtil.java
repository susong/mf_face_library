package com.mf.base.utils;

import android.content.Context;

import com.blankj.utilcode.util.FileIOUtils;
import com.mf.log.LogUtils;
import com.blankj.utilcode.util.PathUtils;
import com.mf.base.GlobalConstants;
import com.mf.common.bean.ConfigBean;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ConfigUtil {

    private ConfigUtil() {

    }

    private static class HolderClass {
        private static final ConfigUtil instance = new ConfigUtil();
    }

    public static ConfigUtil getInstance() {
        return HolderClass.instance;
    }

    private ConfigBean configBean;

    public ConfigBean getConfigBean() {
        return configBean;
    }

    public void setConfigBean(ConfigBean configBean) {
        this.configBean = configBean;
        if (configBean != null) {
            try {
                saveConfig(configBean);
            } catch (Exception e) {
                LogUtils.e(e);
            }
        }
    }

    public ConfigBean loadConfig(Context context) {
        String configFilePath = PathUtils.getExternalStoragePath() + GlobalConstants.APP_BASE_PATH +
                File.separator + GlobalConstants.APP_CONFIG_FILENAME;
        ConfigBean configBean = null;
        try {
            File configFile = new File(configFilePath);
            if (!configFile.exists()) {
                copyConfig(context, configFilePath);
            }
            configBean = parseConfig(configFilePath);
            if (configBean == null) {
                copyConfig(context, configFilePath);
                configBean = parseConfig(configFilePath);
            }
        } catch (Exception e) {
            LogUtils.e(e);
        }
        if (configBean == null) {
            configBean = new ConfigBean();
        }
        this.configBean = configBean;
        return configBean;
    }

    private void saveConfig(ConfigBean configBean) {
        String configFilePath = com.blankj.utilcode.util.PathUtils.getExternalStoragePath() + GlobalConstants.APP_BASE_PATH +
                File.separator + GlobalConstants.APP_CONFIG_FILENAME;
        Yaml yaml = new Yaml();
        String str = yaml.dumpAs(configBean, Tag.MAP, null);
        FileIOUtils.writeFileFromString(configFilePath, str);
    }

    private ConfigBean parseConfig(String configFilePath) throws Exception {
        Yaml yaml = new Yaml(new Constructor(ConfigBean.class));
        return (ConfigBean) yaml.load(new FileInputStream(configFilePath));
    }

    private void copyConfig(Context context, String configFilePath) throws IOException {
        InputStream assetsYml = context.getAssets().open(GlobalConstants.APP_CONFIG_FILENAME);
        FileIOUtils.writeFileFromIS(configFilePath, assetsYml);
    }
}
