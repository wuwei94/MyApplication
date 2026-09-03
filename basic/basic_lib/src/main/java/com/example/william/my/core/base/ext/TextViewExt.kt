package com.example.william.my.core.base.ext

import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.Typeface
import android.text.TextUtils
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

/**
 * 设置自定义 Asset 字体
 */
fun TextView.setTypefaceFromAsset(path: String): TextView {
    this.typeface = Typeface.createFromAsset(this.context.assets, path)
    return this
}

/**
 * 开启单行跑马灯滚动效果
 */
fun TextView.setMarquee() {
    this.setSingleLine()
    this.setHorizontallyScrolling(true)
    this.ellipsize = TextUtils.TruncateAt.MARQUEE
    this.marqueeRepeatLimit = -1
    this.isSelected = true
}

/**
 * 设置水平双色渐变色
 */
fun TextView.setGradientColor(@ColorRes startColor: Int, @ColorRes endColor: Int) {
    val textLength = if (this.text.isNullOrEmpty()) 1 else this.text.length
    this.paint.shader = LinearGradient(
        0f,
        0f,
        this.paint.textSize * textLength,
        0f,
        ContextCompat.getColor(this.context, startColor),
        ContextCompat.getColor(this.context, endColor),
        Shader.TileMode.CLAMP,
    )
    this.invalidate()
}

/**
 * 设置水平三色渐变色
 */
fun TextView.setGradientColor(
    @ColorRes startColor: Int,
    @ColorRes midColor: Int,
    @ColorRes endColor: Int,
) {
    val textLength = if (this.text.isNullOrEmpty()) 1 else this.text.length
    this.paint.shader = LinearGradient(
        0f,
        0f,
        this.paint.textSize * textLength,
        0f,
        intArrayOf(
            ContextCompat.getColor(this.context, startColor),
            ContextCompat.getColor(this.context, midColor),
            ContextCompat.getColor(this.context, endColor),
        ),
        null,
        Shader.TileMode.CLAMP,
    )
    this.invalidate()
}
