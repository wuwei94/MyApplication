package com.example.william.my.basic.basic_shared.category

/**
 * 首页一级分类定义
 *
 * 分类的「稳定 id + 展示标题」集中在此：目录页（[DirectoryActivity]）按枚举顺序生成分类入口，
 * 分类页（[CategoryActivity]）用 id 反查标题。此前标题在两端各写一份，改一处容易漏一处。
 *
 * [id] 同时是 ARouter 跳转参数值，变更会导致按旧 id 传参的入口失效，故保持稳定；
 * 调整展示文案只改 [title]。
 *
 * 一级分类的划分与模块归属判据见 `docs/01-rules/conventions.md#分类判据与模块边界`。
 */
enum class Category(val id: String, val title: String) {
    UI("ui", "UI 交互"),
    MEDIA("media", "多媒体"),
    NETWORK("network", "网络通信"),
    STORAGE("storage", "数据存储"),
    SYSTEM("system", "系统能力"),
    AI("ai", "AI 与机器学习"),
    ENGINEERING("engineering", "架构与工程"),
    KOTLIN_JETPACK("kotlin_jetpack", "Kotlin & Jetpack"),
    COMPOSE_FLUTTER("compose_flutter", "Compose & Flutter"),
    SAMPLE_FEATURE("sample_feature", "Sample & Feature"),
    ;

    companion object {

        /** 按 id 反查分类；未知 id（含空值）返回 null */
        fun fromId(id: String?): Category? = entries.firstOrNull { it.id == id }
    }
}
