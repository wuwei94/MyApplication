package com.example.william.my.core.base.scheme

import android.app.Activity
import android.content.Intent
import android.net.Uri

/**
 * 私有协议Schema跳转帮助类
 */
object ProtocolHelper {

    /**
     * 处理协议跳转
     *
     * @param activity 上下文
     * @param url      跳转地址
     * @param extraMap 额外需要带的参数
     */
    fun handleProtocolEvent(
        activity: Activity?,
        url: String?,
        extraMap: Map<String?, Any?>? = null
    ) {
        if (activity == null || url.isNullOrBlank()) {
            return
        }
        if (url.startsWith("http://", ignoreCase = true) || url.startsWith("https://", ignoreCase = true)) {
            // Http网页通过系统浏览器打开
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                activity.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else if (url.startsWith(ProtocolConstants.APP_SCHEME_HEADER)) {
            // App内部跳转
            val path = getProtocolAction(url)
            val paramsMap = getProtocolParams(url)
            val intent = getPageIntent(activity, path)
            if (intent != null) {
                for ((key, value) in paramsMap) {
                    if (!key.isNullOrBlank() && value != null) {
                        intent.putExtra(key, value.toString())
                    }
                }
                if (!extraMap.isNullOrEmpty()) {
                    for ((key, value) in extraMap) {
                        if (!key.isNullOrBlank() && value != null) {
                            intent.putExtra(key, value.toString())
                        }
                    }
                }
                activity.startActivity(intent)
            }
        }
    }

    /**
     * 私有协议分析,获取协议事件.
     */
    fun getProtocolAction(linkUrl: String?): String? {
        if (linkUrl.isNullOrBlank()) return null
        return try {
            val uri = Uri.parse(linkUrl)
            if (!uri.host.isNullOrEmpty()) {
                uri.host
            } else {
                var cleanUrl = linkUrl
                val questIndex = cleanUrl.indexOf('?')
                if (questIndex != -1) {
                    cleanUrl = cleanUrl.substring(0, questIndex)
                }
                if (cleanUrl.startsWith(ProtocolConstants.APP_SCHEME_HEADER)) {
                    cleanUrl = cleanUrl.removePrefix(ProtocolConstants.APP_SCHEME_HEADER)
                }
                cleanUrl
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 解析私有协议的参数,其中page字段是需要打开的页面.
     */
    fun getProtocolParams(urlString: String?): Map<String, Any> {
        val paramsMap = mutableMapOf<String, Any>()
        if (urlString.isNullOrBlank()) return paramsMap
        return try {
            val uri = Uri.parse(urlString)
            for (queryName in uri.queryParameterNames) {
                val queryValue = uri.getQueryParameter(queryName)
                if (!queryName.isNullOrEmpty() && queryValue != null) {
                    paramsMap[queryName] = queryValue
                }
            }
            paramsMap
        } catch (e: Exception) {
            e.printStackTrace()
            paramsMap
        }
    }

    fun getPageIntent(activity: Activity?, page: String?): Intent? {
        if (activity == null || page.isNullOrBlank()) {
            return null
        }
        var intent: Intent? = null
        if (ProtocolConstants.SCHEME_PAGE_MAIN_PAGE == page) {
            intent = activity.packageManager.getLaunchIntentForPackage(activity.packageName)
        }
        if (intent != null) {
            intent.putExtra(ProtocolConstants.SCHEME_FROM, activity.javaClass.simpleName)
        }
        return intent
    }
}