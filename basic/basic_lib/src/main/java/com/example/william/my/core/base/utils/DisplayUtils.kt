package com.example.william.my.core.base.utils

import android.content.res.Resources

/**
 * 屏幕尺寸转换工具
 */
object DisplayUtils {

    /**
     * dp 转 px
     * @param dpValue dp 值
     * @return 对应的 px 值
     */
    fun dp2px(dpValue: Float): Int {
        val scale = Resources.getSystem().displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    /**
     * px 转 dp
     * @param pxValue px 值
     * @return 对应的 dp 值
     */
    fun px2dp(pxValue: Float): Int {
        val scale = Resources.getSystem().displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }
}
