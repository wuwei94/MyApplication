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
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.module.widget.databinding.UiActivityWebviewBinding

/**
 * WebView — 网页加载控件
 *
 * WebView 是 Android 内置的网页加载控件，用于展示网页内容。
 *
 * 核心机制与避坑点：
 * 1. 网页加载：支持加载 URL、本地 HTML、JavaScript
 * 2. 交互支持：支持 JavaScript 与原生代码交互
 * 3. 缓存机制：支持网页缓存，提升加载速度
 * 4. 安全控制：支持 SSL 证书处理、文件访问控制
 */
@Route(path = RouterPath.Widget.WebView)
class WebViewActivity : BaseVBActivity<UiActivityWebviewBinding>() {

    override fun getViewBinding(): UiActivityWebviewBinding = UiActivityWebviewBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        initWebView()

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.webView.canGoBack()) {
                        binding.webView.goBack()
                    } else {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            },
        )
    }

    override fun onDestroy() {
        binding.webView.apply {
            stopLoading()
            destroy()
        }
        super.onDestroy()
    }

    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
    private fun initWebView() {
        // 隐藏滚动条
        binding.webView.isVerticalScrollBarEnabled = true
        binding.webView.isHorizontalScrollBarEnabled = true

        // 启用JavaScript
        binding.webView.settings.javaScriptEnabled = true
        // 启用视图支持
        binding.webView.settings.useWideViewPort = true
        // 适应屏幕宽度
        binding.webView.settings.loadWithOverviewMode = true
        // 手势缩放
        binding.webView.settings.builtInZoomControls = true
        // 隐藏缩放按钮
        binding.webView.settings.displayZoomControls = false
        // DOM Storage（DOM 存储）
        binding.webView.settings.domStorageEnabled = true
        // 关闭 file 域访问：禁止 WebView 通过 file:// 协议加载本地文件（minSdk 24 下默认开启，需显式关闭）
        binding.webView.settings.allowFileAccess = false
        val headers = mapOf<String, String>()
        // 添加HTTP头信息
        binding.webView.loadUrl("https://www.baidu.com/", headers)
        binding.webView.webViewClient = object : WebViewClient() {
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
                request: WebResourceRequest,
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
                error: SslError,
            ) {
                handler.cancel()
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
            }
        }
        binding.webView.webChromeClient = object : WebChromeClient() {
        }
        binding.webView.addJavascriptInterface(
            object :
                WebViewInterface(object : WebViewJsCallback() {
                    override fun closeWebViewPage() {
                    }
                }) {
            },
            "interfaceName",
        )
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
