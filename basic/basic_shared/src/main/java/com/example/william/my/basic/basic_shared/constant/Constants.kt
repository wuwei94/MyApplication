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
    // PAG 演示素材（module_anim assets/pag）
    // 素材取自 libpag 官方 assets 目录（Apache-2.0），统一走 PAGFile.Load(assets, path)，
    // 因此不带 assets:// scheme。注释中的「槽位」是该 .pag 内可运行时替换的可编辑层数量，
    // 由 PAGExporter 勾选「可编辑」决定；画布尺寸与槽位数均已用 libpag 解析器逐个核实。

    /** 播放：1600×1600 · 6000ms · 0 槽位。正方形画布，纯动效 */
    const val Pag_Playback = "pag/gift_moonlight.pag"

    /** 替换图像：800×800 · 1000ms · 图片槽 ×1。自带内容为一张中性的图片占位图，替换前后对比清楚 */
    const val Pag_Image = "pag/replacement.pag"

    /** 替换文字：1334×750 · 4000ms · 文本槽 ×2。槽 0 主标题、槽 1 副标题，两槽互不干扰 */
    const val Pag_Text = "pag/template_text.pag"

    // PAG 示例图池（assets/img，作为 PAGImage.FromAssets 的槽位内容）。
    // 一组通用示例图，页面对齐素材实际槽位数取前 N 张：当前替换图像素材只有 1 个图片槽，
    // 实际只用第 1 张；换成多槽素材时同一段代码会自动铺满。
    const val Pag_Image_Product_Bottle = "img/product_bottle.png"
    const val Pag_Image_Product_Bag = "img/product_bag.png"
    const val Pag_Image_Product_Cat = "img/product_cat.png"
    const val Pag_Image_Product_Logo = "img/product_logo.png"
    const val Pag_Image_Poster_Sale = "img/poster_sale.jpg"

    /** 官方 SVGA-Samples 心跳动效，imageKey="heart" 可换图；"heartbeat" 为音轨图层 */
    const val Url_SVGA_Heartbeat = "svga/heartbeat.svga"
    const val SVGA_Avatar_A = "svga/avatar_a.png"
    const val SVGA_Avatar_B = "svga/avatar_b.png"
    const val Url_Audio = "https://video.fanqievv.com/user_sound/2021/01/10/1610291672209.mp3"

    const val Key_Username = "username"
    const val Key_Password = "password"
    const val Value_Username = "17778060027"
    const val Value_Password = "123456"
}
