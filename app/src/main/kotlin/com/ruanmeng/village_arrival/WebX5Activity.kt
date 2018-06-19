package com.ruanmeng.village_arrival

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import com.lzg.extend.StringDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.ruanmeng.base.BaseActivity
import com.ruanmeng.base.cancelLoadingDialog
import com.ruanmeng.base.showLoadingDialog
import com.ruanmeng.share.BaseHttp
import com.tencent.smtt.sdk.WebSettings
import com.tencent.smtt.sdk.WebView
import com.tencent.smtt.sdk.WebViewClient
import kotlinx.android.synthetic.main.activity_web_x5.*
import org.json.JSONObject

class WebX5Activity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_x5)
        init_title(intent.getStringExtra("title"))

        OkGo.post<String>(BaseHttp.new_user_course)
                .tag(this@WebX5Activity)
                .execute(object : StringDialogCallback(baseContext) {
                    override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) =
                            wv_web.loadUrl(JSONObject(response.body()).optString("object"))
                })
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Suppress("DEPRECATION")
    override fun init_title() {
        super.init_title()
        wv_web.apply {

            settings.apply {
                //支持javascript
                javaScriptEnabled = true
                //设置可以支持缩放
                setSupportZoom(true)
                //自适应屏幕
                loadWithOverviewMode = true
                layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN

                //设置出现缩放工具
                builtInZoomControls = true
                displayZoomControls = false

                //设置是否使用缓存
                setAppCacheEnabled(true)
                domStorageEnabled = true

                allowFileAccess = true
                useWideViewPort = true
                //开启 Application Caches 功能
                setAppCacheEnabled(true)
                //设置 Application Caches 缓存目录
                setAppCachePath(cacheDir.absolutePath)
                setGeolocationEnabled(true)
                setGeolocationDatabasePath(cacheDir.absolutePath)
                databaseEnabled = true
                databasePath = cacheDir.absolutePath
            }
            isHorizontalScrollBarEnabled = false

            webViewClient = object : WebViewClient() {

                /*
                 * 这个事件，将在用户点击链接时触发。
                 * 通过判断url，可确定如何操作，
                 * 如果返回true，表示我们已经处理了这个request，
                 * 如果返回false，表示没有处理，那么浏览器将会根据url获取网页
                 */
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    //view.loadUrl(url)
                    return false
                }

                /*
                 * 在开始加载网页时会回调
                 */
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    showLoadingDialog()
                }

                /*
                 * 在结束加载网页时会回调
                 */
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    cancelLoadingDialog()
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
