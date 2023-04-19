package com.mf.common.bean

data class ConfigBean(
    val httpServerHost: String = "",
    val httpServerPort: Int = 0,
    val webSocketServerHost: String = "",
    val webSocketServerPort: Int = 0,
    val baiduFaceSerialNumber: String = "",
)
