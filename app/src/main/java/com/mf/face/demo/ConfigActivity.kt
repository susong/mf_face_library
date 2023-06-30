package com.mf.face.demo

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import android.widget.Button
import androidx.fragment.app.FragmentActivity
import com.baidu.idl.face.main.attribute.activity.attribute.FaceAttributeRgbActivity
import com.baidu.idl.main.facesdk.utils.PreferencesUtil
import com.blankj.utilcode.util.SPUtils
import com.mf.base.config.GlobalProperty
import com.mf.face.manager.FaceAuthManager
import com.mf.face.utils.ConfigUtils
import com.mf.log.LogUtils
import com.permissionx.guolindev.PermissionX

class ConfigActivity : FragmentActivity(), View.OnClickListener {
    private val TAG: String = ConfigActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        hideStatusBar()
        hideSystemUI()
        super.onCreate(savedInstanceState)
//        val isFirstLaunch = SPUtils.getInstance().getBoolean("isFirstLaunch", true)
//        val faceConfigExit = ConfigUtils.isConfigExist()
//        if (isFirstLaunch || !faceConfigExit) {
            setContentView(R.layout.activity_config)
            initView()
            requestPermissions()
//        } else {
//            initConfig()
//            goMainActivity()
//        }
    }

    private fun initView() {
        findViewById<Button>(R.id.btn_go_home).setOnClickListener(this)
        findViewById<Button>(R.id.btn_go_setting).setOnClickListener(this)
        findViewById<Button>(R.id.btn_go_face_setting).setOnClickListener(this)
    }

    private fun requestPermissions() {
        val permissionList = mutableListOf(
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.SYSTEM_ALERT_WINDOW,
            Manifest.permission.CAMERA,
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            permissionList.add(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
        }
        PermissionX.init(this)
            .permissions(permissionList)
            .explainReasonBeforeRequest()
            .onExplainRequestReason { scope, deniedList, beforeRequest ->
                if (beforeRequest) {
                    scope.showRequestReasonDialog(
                        deniedList,
                        "即将申请的权限是程序必须依赖的权限", "我已明白"
                    )
                } else {
                    scope.showRequestReasonDialog(
                        deniedList,
                        "即将重新申请的权限是程序必须依赖的权限", "我已明白"
                    )
                }
            }
            .onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(
                    deniedList,
                    "您需要去应用程序设置当中手动开启权限", "去设置"
                )
            }
            .request { allGranted, grantedList, deniedList ->
                LogUtils.d(
                    "allGranted:$allGranted grantedList:$grantedList " +
                            "deniedList:$deniedList"
                )
                if (allGranted) {
//                    ToastUtils.showShort("所有申请的权限都已通过")
                    onAllPermissionsGranted()
                } else {
//                    ToastUtils.showLong("您拒绝了如下权限：$deniedList")
                }
            }
    }

    private fun onAllPermissionsGranted() {
        SPUtils.getInstance().put("isFirstLaunch", false)
        initConfig()
        initFaceConfig()
    }

    private fun initConfig() {
        GlobalProperty.getInstance().init()
    }

    private fun goSystemSettingPage(context: Context) {
        val intent = Intent(Settings.ACTION_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    private fun goFaceSettingPage(context: Context) {
        val intent = Intent(context, FaceAttributeRgbActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    private fun goMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        // 跳转无动画
        overridePendingTransition(0, 0)
        finish()
    }

    private fun initFaceConfig() {
        PreferencesUtil.initPrefs(this)
        PreferencesUtil.putString(
            "activate_online_key",
            GlobalProperty.getInstance().getString("baidu.face.serialnumber", "")
        )
        FaceAuthManager.getInstance().init(this, null)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    /**
     * 启动全屏模式，隐藏状态栏和导航栏
     * 参考：https://developer.android.com/training/system-ui/immersive
     */
    private fun hideSystemUI() {
        // Enables sticky immersive mode.
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    /**
     * 隐藏状态栏
     * 参考：https://juejin.cn/post/6941012715838111775
     */
    private fun hideStatusBar() {
        val lp = window.attributes
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            lp.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        window.attributes = lp
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_go_home -> {
                goMainActivity()
            }

            R.id.btn_go_setting -> {
                goSystemSettingPage(this)
            }

            R.id.btn_go_face_setting -> {
                goFaceSettingPage(this)
            }
        }
    }
}

