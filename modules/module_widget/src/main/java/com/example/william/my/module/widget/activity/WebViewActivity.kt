package com.example.william.my.module.widget.activity

import android.annotation.SuppressLint
import android.net.http.SslError
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.activity.BaseVBActivity
import com.example.william.my.module.widget.databinding.UiActivityWebviewBinding

/**
 * WebView — 网页加载控件
 *
 * WebView 是 Android 内置的网页加载控件，用于展示网页内容。
 *
 * 核心特性：
 * 1. 网页加载：支持加载 URL、本地 HTML、JavaScript
 * 2. 交互支持：支持 JavaScript 与原生代码交互
 * 3. 缓存机制：支持网页缓存，提升加载速度
 * 4. 安全控制：支持 SSL 证书处理、文件访问控制
 *
 * 基本用法：
 * ```kotlin
 * // 加载网页
 * webView.loadUrl("https://www.example.com")
 *
 * // 启用 JavaScript
 * webView.settings.javaScriptEnabled = true
 *
 * // 添加 JavaScript 接口
 * webView.addJavascriptInterface(MyInterface(), "Android")
 *
 * // 设置 WebViewClient
 * webView.webViewClient = object : WebViewClient() {
 *     override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
 *         view.loadUrl(url)
 *         return true
 *     }
 * }
 * ```
 *
 * 适用场景：
 * - 加载网页内容
 * - 混合开发（Hybrid App）
 * - 展示富文本内容
 */
@Route(path = RouterPath.Widget.WebView)
class WebViewActivity : BaseVBActivity<UiActivityWebviewBinding>() {

    override fun getViewBinding(): UiActivityWebviewBinding {
        return UiActivityWebviewBinding.inflate(layoutInflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        initWebView()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (mBinding.webView.canGoBack()) {
                    mBinding.webView.goBack()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    override fun onDestroy() {
        mBinding.webView.apply {
            stopLoading()
            destroy()
        }
        super.onDestroy()
    }

    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
    private fun initWebView() {
        //隐藏滚动条
        mBinding.webView.isVerticalScrollBarEnabled = true
        mBinding.webView.isHorizontalScrollBarEnabled = true

        //启用JavaScript
        mBinding.webView.settings.javaScriptEnabled = true
        //启用视图支持
        mBinding.webView.settings.useWideViewPort = true
        //适应屏幕宽度
        mBinding.webView.settings.loadWithOverviewMode = true
        //手势缩放
        mBinding.webView.settings.builtInZoomControls = true
        //隐藏缩放按钮
        mBinding.webView.settings.displayZoomControls = false
        //DOM Storage
        mBinding.webView.settings.domStorageEnabled = true
        // 关闭file域访问，禁止file域对http域进行访问
        // setAllowFileAccessFromFileURLs&setAllowUniversalAccessFromFileURLs
        // Android 4.1版本之前这两个API默认是true，需要显式设置为false
        val headers = mapOf<String, String>()
        //添加HTTP头信息
        mBinding.webView.loadUrl("https://www.baidu.com/", headers)
        mBinding.webView.webViewClient = object : WebViewClient() {
            /**
             * 拦截资源请求
             */
            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }

            /**
             * 拦截资源请求
             */
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                view.loadUrl(request.url.toString())
                return true
            }

            /**
             * SSL证书错误处理：直接取消加载，不忽略证书错误
             */
            override fun onReceivedSslError(
                view: WebView,
                handler: SslErrorHandler,
                error: SslError
            ) {
                handler.cancel()
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
            }
        }
        mBinding.webView.webChromeClient = object : WebChromeClient() {

        }
        mBinding.webView.addJavascriptInterface(object :
            WebViewInterface(object : WebViewJsCallback() {
                override fun closeWebViewPage() {

                }
            }) {

        }, "interfaceName")
    }

    open class WebViewInterface(private val jsCallback: WebViewJsCallback?) {

        @JavascriptInterface
        fun closeWebViewPage() {
            jsCallback?.closeWebViewPage()
        }
    }

    abstract class WebViewJsCallback {
        abstract fun closeWebViewPage()
    }
}