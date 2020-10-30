package com.rakuten.tech.mobile.testapp.ui.component

import android.annotation.SuppressLint
import android.content.Context
import android.os.Message
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebView.WebViewTransport
import android.webkit.WebViewClient
import com.rakuten.tech.mobile.miniapp.navigator.MiniAppExternalUrlLoader

@SuppressLint("SetJavaScriptEnabled")
class SampleExternalWebView(context: Context, url: String, sampleWebViewClient: WebViewClient): WebView(context) {

    init {
        settings.javaScriptEnabled = true
        settings.setSupportMultipleWindows(true)
        settings.javaScriptCanOpenWindowsAutomatically = true
        webViewClient = sampleWebViewClient
        webChromeClient = SampleWebChromeClient(context)
        loadUrl(url)
    }
}

class SampleWebViewClient(private val miniAppExternalUrlLoader: MiniAppExternalUrlLoader): WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean =
        miniAppExternalUrlLoader.shouldOverrideUrlLoading(url ?: "")
}

class SampleWebChromeClient(val context: Context) : WebChromeClient() {

    private var windowAutoincrementId = 0

    @SuppressLint("SetJavaScriptEnabled", "LongLogTag")
    override fun onCreateWindow(
        view: WebView?,
        isDialog: Boolean,
        isUserGesture: Boolean,
        resultMsg: Message?
    ): Boolean {

        val newWebView = WebView(context)
        val webSettings = newWebView.settings
        webSettings.javaScriptEnabled = true
        (resultMsg?.obj as WebViewTransport).webView = newWebView
        resultMsg.sendToTarget()

        //################################################

        // detecting window.open / _blank in this external webview

        val result = view?.hitTestResult
        val url = result?.extra

        Log.d("Tracing url...", url)
        Log.d("Tracing originalUrl...", view?.originalUrl)

        if (url?.contains(view.originalUrl)!!)
            Log.d("Tracing incoming...","window.open")
        else
            Log.d("Tracing incoming...","_blank")

        return true
    }
}
