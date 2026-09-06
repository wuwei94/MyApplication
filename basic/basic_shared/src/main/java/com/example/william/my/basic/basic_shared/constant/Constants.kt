package com.example.william.my.basic.basic_shared.constant

import com.example.william.my.basic.basic_shared.BuildConfig

/**
 * 全局常量（接口地址、资源 URL、密钥等）
 */
object Constants {
    const val Url_Base = "https://www.wanandroid.com/"
    const val Url_Login = Url_Base + "user/login"

    const val Url_Article_List = Url_Base + "article/list/{page}/json"

    const val Url_Download =
        "https://d1.mosi.126.net/dmusic/NeteaseCloudMusic_Moyi_netease1_1.2.0.1597393004.apk"

    const val Url_WebSocket = "wss://echo.websocket.org"
    const val Url_DeepSeek = "https://api.deepseek.com/chat/completions"

    /**
     * DeepSeek API Key（从 local.properties 动态注入，不入库）
     */
    val DeepSeek_ApiKey: String get() = BuildConfig.DEEPSEEK_API_KEY

    // MQTT（EMQX 公共 Broker，无需账号）
    const val Mqtt_Broker = "tcp://broker.emqx.io:1883"
    const val Mqtt_Host = "broker.emqx.io"
    const val Mqtt_Port = 1883
    const val Mqtt_Topic = "mqtt/example"

    const val Url_Ludo = "https://gamfunfile.gamfun.com/ludo/zip/ludo.zip"
    const val Url_BombCat = "https://gamfunfile.gamfun.com/bombcat/zip/cat.zip"

    const val Url_Upload = "http://192.168.0.103:5566/upload"
    const val Url_Image1 =
        "https://web.hycdn.cn/arknights/official/pic/20210329/7dcfb48a8b98d7fb6966728e19b782d9.png"
    const val Url_Image2 =
        "https://web.hycdn.cn/arknights/official/pic/20210401/8b683b7c01ebf0eb570370a48b655504.png"
    const val Url_NinePatchAsset = "file:///android_asset/ninepatch_toggle.9.png"
    const val Url_NinePatchNetwork =
        "https://raw.githubusercontent.com/Anatolii/NinePatchChunk/master/NinePatchChunk/Library/src/androidTest/assets/lib_bg.9.png"
    const val Url_PAG = "assets://pag/diamond.pag"
    const val Url_SVGA = "svga/diamond.svga"
    const val Url_Audio = "https://video.fanqievv.com/user_sound/2021/01/10/1610291672209.mp3"

    const val Key_Username = "username"
    const val Key_Password = "password"
    const val Value_Username = "17778060027"
    const val Value_Password = "123456"
}
