package com.mf.face.demo

import android.app.Application
import me.jessyan.autosize.AutoSize

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        //当 App 中出现多进程, 并且您需要适配所有的进程, 就需要在 App 初始化时调用 initCompatMultiProcess()
        AutoSize.initCompatMultiProcess(this)
    }
}