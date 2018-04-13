package com.ruanmeng.village_arrival

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.ruanmeng.base.BaseActivity
import kotlinx.android.synthetic.main.activity_web.*

class WebActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)
        init_title(intent.getStringExtra("title"))

        when (intent.getStringExtra("title")) {
            "账户说明" -> {}
            "保证金说明" -> {}
            "提现说明" -> {}
            "常见问题" -> {}
            "消息详情" -> {}
        }
     }

    @SuppressLint("SetJavaScriptEnabled")
    override fun init_title() {
        super.init_title()
        wv_web.apply {
            //支持javascript
            settings.javaScriptEnabled = true
            //设置可以支持缩放
            settings.setSupportZoom(true)
            //自适应屏幕
            settings.loadWithOverviewMode = true
            settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
            isHorizontalScrollBarEnabled = false

            //设置出现缩放工具
            settings.builtInZoomControls = true
            settings.displayZoomControls = false

            webViewClient = object : WebViewClient() {

                /* 这个事件，将在用户点击链接时触发。
                 * 通过判断url，可确定如何操作，
                 * 如果返回true，表示我们已经处理了这个request，
                 * 如果返回false，表示没有处理，那么浏览器将会根据url获取网页
                 */
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    view.loadUrl(url)
                    return true
                }
            }
        }
    }

    override fun onBackPressed() {
        if (wv_web.canGoBack()) {
            wv_web.goBack()
            return
        }
        super.onBackPressed()
    }

    override fun onResume() {
        super.onResume()
        wv_web.onResume()
    }

    override fun onPause() {
        super.onPause()
        wv_web.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        wv_web.destroy()
    }
}
