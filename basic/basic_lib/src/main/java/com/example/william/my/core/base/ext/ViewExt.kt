package com.example.william.my.core.base.ext

import android.graphics.Rect
import android.view.View

/**
 * 判断 View 是否在当前屏幕可视区域内（基于 getLocalVisibleRect 计算）
 */
fun View.isLocalVisibleOnScreen(): Boolean {
    if (!this.isShown) return false
    val dm = context.resources.displayMetrics
    val screenRect = Rect(0, 0, dm.widthPixels, dm.heightPixels)
    return this.getLocalVisibleRect(screenRect)
}
