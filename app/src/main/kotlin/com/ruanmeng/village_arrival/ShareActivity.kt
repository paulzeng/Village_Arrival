package com.ruanmeng.village_arrival

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.ruanmeng.base.BaseActivity
import com.ruanmeng.share.BaseHttp
import com.ruanmeng.utils.CommonUtil
import kotlinx.android.synthetic.main.activity_share.*
import android.webkit.WebChromeClient
import android.webkit.JavascriptInterface
import android.widget.LinearLayout
import android.widget.TextView
import com.ruanmeng.base.getString
import com.umeng.socialize.ShareAction
import com.umeng.socialize.UMShareAPI
import com.umeng.socialize.bean.SHARE_MEDIA
import com.umeng.socialize.media.UMImage
import com.umeng.socialize.media.UMWeb

class ShareActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)
        init_title("邀请有奖")

        wv_web.loadUrl(BaseHttp.invite_index)
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

            //设置是否使用缓存
            settings.setAppCacheEnabled(true)
            settings.domStorageEnabled = true

            addJavascriptInterface(JsInteration(), "share")
            webChromeClient = WebChromeClient()
            webViewClient = object : WebViewClient() {

                /* 这个事件，将在用户点击链接时触发。
                 * 通过判断url，可确定如何操作，
                 * 如果返回true，表示我们已经处理了这个request，
                 * 如果返回false，表示没有处理，那么浏览器将会根据url获取网页
                 */
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    val isWeb = CommonUtil.isWeb(url)
                    if (isWeb) view.loadUrl(url)
                    return isWeb
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        UMShareAPI.get(this@ShareActivity).onActivityResult(requestCode, resultCode, data)
    }

    inner class JsInteration {
        @SuppressLint("InflateParams")
        @JavascriptInterface
        fun openDialog() {
            runOnUiThread {
                val view = LayoutInflater.from(baseContext).inflate(R.layout.dialog_share_bottom, null) as View
                val wechat = view.findViewById<LinearLayout>(R.id.dialog_share_wechat)
                val circle = view.findViewById<LinearLayout>(R.id.dialog_share_circle)
                val qq = view.findViewById<LinearLayout>(R.id.dialog_share_qq)
                val space = view.findViewById<LinearLayout>(R.id.dialog_share_space)
                val sms = view.findViewById<LinearLayout>(R.id.dialog_share_sms)
                val cancel = view.findViewById<TextView>(R.id.dialog_share_cancel)
                val dialog = BottomSheetDialog(baseContext)

                val urlShare = "${BaseHttp.baseImg}forend/register_index.hm?userInfoId=${getString("token")}"

                wechat.setOnClickListener {
                    dialog.dismiss()

                    ShareAction(baseContext)
                            .setPlatform(SHARE_MEDIA.WEIXIN)
                            .withText(getString(R.string.app_name))
                            .withMedia(UMWeb(urlShare).apply {
                                title = "邀请好友"
                                description = "在线注册"
                                setThumb(UMImage(baseContext, R.mipmap.ic_launcher_logo))
                            })
                            .share()
                }
                circle.setOnClickListener {
                    dialog.dismiss()

                    ShareAction(baseContext)
                            .setPlatform(SHARE_MEDIA.WEIXIN_CIRCLE)
                            .withText(getString(R.string.app_name))
                            .withMedia(UMWeb(urlShare).apply {
                                title = "邀请好友"
                                description = "在线注册"
                                setThumb(UMImage(baseContext, R.mipmap.ic_launcher_logo))
                            })
                            .share()
                }
                qq.setOnClickListener {
                    dialog.dismiss()

                    ShareAction(baseContext)
                            .setPlatform(SHARE_MEDIA.QQ)
                            .withText(getString(R.string.app_name))
                            .withMedia(UMWeb(urlShare).apply {
                                title = "邀请好友"
                                description = "在线注册"
                                setThumb(UMImage(baseContext, R.mipmap.ic_launcher_logo))
                            })
                            .share()
                }
                space.setOnClickListener {
                    dialog.dismiss()

                    ShareAction(baseContext)
                            .setPlatform(SHARE_MEDIA.QZONE)
                            .withText(getString(R.string.app_name))
                            .withMedia(UMWeb(urlShare).apply {
                                title = "邀请好友"
                                description = "在线注册"
                                setThumb(UMImage(baseContext, R.mipmap.ic_launcher_logo))
                            })
                            .share()
                }
                sms.setOnClickListener {
                    dialog.dismiss()

                    ShareAction(baseContext)
                            .setPlatform(SHARE_MEDIA.SMS)
                            .withText(getString(R.string.app_name))
                            .withMedia(UMWeb(urlShare).apply {
                                title = "邀请好友"
                                description = "在线注册"
                                setThumb(UMImage(baseContext, R.mipmap.ic_launcher_logo))
                            })
                            .share()
                }
                cancel.setOnClickListener { dialog.dismiss() }

                dialog.setContentView(view)
                dialog.show()
            }
        }
    }
}
