package com.ruanmeng.village_arrival

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.lzg.extend.StringDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.ruanmeng.base.BaseActivity
import com.ruanmeng.share.BaseHttp
import kotlinx.android.synthetic.main.activity_web.*
import org.json.JSONObject

class WebActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)
        init_title(intent.getStringExtra("title"))

        when (intent.getStringExtra("title")) {
            "账户说明", "保证金说明", "提现说明",
            "关于我们", "注册协议", "在线咨询" -> {
                OkGo.post<String>(BaseHttp.help_center)
                        .tag(this@WebActivity)
                        .params("htmlKey", when(intent.getStringExtra("title")) {
                            "账户说明" -> "ZHSM"
                            "保证金说明" -> "BZJSM"
                            "提现说明" -> "TXSM"
                            "关于我们" -> "GYWM"
                            "注册协议" -> "ZCXY"
                            "在线咨询" -> "zxzx"
                            else -> ""
                        })
                        .execute(object : StringDialogCallback(baseContext) {

                            override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                                val str = "<!doctype html><html>\n" +
                                        "<meta charset=\"utf-8\">" +
                                        "<style type=\"text/css\">" +
                                        "body{ padding:0; margin:0; }\n" +
                                        ".con{ width:95%; margin:0 auto; color:#fff; color:#666; padding:0.5em 0; overflow:hidden; display:block; font-size:0.92em; line-height:1.8em; }\n" +
                                        ".con h1,h2,h3,h4,h5,h6{ font-size:1em; }\n " +
                                        "img{ width:auto; max-width: 100% !important; height:auto !important; margin:0 auto; display:block; }\n" +
                                        "*{ max-width:100% !important; }\n" +
                                        "</style>\n" +
                                        "<body style=\"padding:0; margin:0; \">" +
                                        "<div class=\"con\">" +
                                        JSONObject(response.body()).optString("help") +
                                        "</div>" +
                                        "</body>" +
                                        "</html>"

                                wv_web.loadDataWithBaseURL(BaseHttp.baseImg, str, "text/html", "utf-8", "")
                            }

                        })
            }
            "常见问题" -> {
                val str = "<!doctype html><html>\n" +
                        "<meta charset=\"utf-8\">" +
                        "<style type=\"text/css\">" +
                        "body{ padding:0; margin:0; }\n" +
                        ".con{ width:95%; margin:0 auto; color:#fff; color:#666; padding:0.5em 0; overflow:hidden; display:block; font-size:0.92em; line-height:1.8em; }\n" +
                        ".con h1,h2,h3,h4,h5,h6{ font-size:1em; }\n " +
                        "img{ width:auto; max-width: 100% !important; height:auto !important; margin:0 auto; display:block; }\n" +
                        "*{ max-width:100% !important; }\n" +
                        "</style>\n" +
                        "<body style=\"padding:0; margin:0; \">" +
                        "<div class=\"con\">" +
                        intent.getStringExtra("content") +
                        "</div>" +
                        "</body>" +
                        "</html>"

                wv_web.loadDataWithBaseURL(BaseHttp.baseImg, str, "text/html", "utf-8", "")
            }
            "消息详情" -> {
                val str = "<!doctype html><html>\n" +
                        "<meta charset=\"utf-8\">" +
                        "<style type=\"text/css\">" +
                        "body{ padding:0; margin:0; }\n" +
                        ".view_h1{ width:95%; margin:0 auto; display:block; overflow:hidden;  font-size:1.1em; color:#333; padding:0.5em 0; line-height:1.0em; }\n" +
                        ".view_time{ width:95%; margin:0 auto; display:block; overflow:hidden; font-size:0.8em; color:#999; }\n" +
                        ".con h1,h2,h3,h4,h5,h6{ font-size:1em;}\n " +
                        "img{ width:auto; max-width: 100% !important; height:auto !important; margin:0 auto; display:block; }\n" +
                        "*{ max-width:100% !important; }\n" +
                        "</style>\n" +
                        "<body style=\"padding:0; margin:0; \">" +
                        "<div class=\"view_h1\">" +
                        intent.getStringExtra("content") +
                        "</div>" +
                        "<div class=\"view_time\">" +
                        intent.getStringExtra("sendDate") +
                        "</div>" +
                        "</body>" +
                        "</html>"

                wv_web.loadDataWithBaseURL(BaseHttp.baseImg, str, "text/html", "utf-8", "")
            }
            "详情" -> {
                OkGo.post<String>(BaseHttp.newsnotice_Info)
                        .tag(this@WebActivity)
                        .params("newsnoticeId", intent.getStringExtra("newsnoticeId"))
                        .execute(object : StringDialogCallback(baseContext) {

                            override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                                val str = "<!doctype html><html>\n" +
                                        "<meta charset=\"utf-8\">" +
                                        "<style type=\"text/css\">" +
                                        "body{ padding:0; margin:0; }\n" +
                                        ".con{ width:95%; margin:0 auto; color:#fff; color:#666; padding:0.5em 0; overflow:hidden; display:block; font-size:0.92em; line-height:1.8em; }\n" +
                                        ".con h1,h2,h3,h4,h5,h6{ font-size:1em; }\n " +
                                        "img{ width:auto; max-width: 100% !important; height:auto !important; margin:0 auto; display:block; }\n" +
                                        "*{ max-width:100% !important; }\n" +
                                        "</style>\n" +
                                        "<body style=\"padding:0; margin:0; \">" +
                                        "<div class=\"con\">" +
                                        JSONObject(response.body()).optString("content") +
                                        "</div>" +
                                        "</body>" +
                                        "</html>"

                                wv_web.loadDataWithBaseURL(BaseHttp.baseImg, str, "text/html", "utf-8", "")
                            }

                        })
            }
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
