package com.mf.base.utils

import com.blankj.utilcode.util.PathUtils
import com.mf.base.GlobalConstants
import java.io.File

object PathUtil {

    @JvmStatic
    fun createAppBasePath() {
        val appBasePath = PathUtils.getExternalStoragePath() + GlobalConstants.APP_BASE_PATH
        val file = File(appBasePath)
        if (!file.exists()) {
            file.mkdirs()
        } else if (file.exists() && file.isFile) {
            file.delete()
            file.mkdirs()
        }
    }
}