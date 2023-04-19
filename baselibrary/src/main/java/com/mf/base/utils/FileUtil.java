package com.mf.base.utils;

import android.content.Context;

import com.blankj.utilcode.util.FileIOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class FileUtil {

    public static void copyFromAssetsIfNotExist(Context context, String assetsFileName, String destFilePath) {
        File file = new File(destFilePath);
        if (file.exists()) {
            if (file.isFile()) {
                return;
            } else {
                file.delete();
            }
        }
        file.getParentFile().mkdirs();
        try {
            InputStream is = context.getAssets().open(assetsFileName);
            FileIOUtils.writeFileFromIS(file, is);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
