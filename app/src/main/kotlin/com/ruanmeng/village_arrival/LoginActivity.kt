package com.ruanmeng.village_arrival

import android.content.Intent
import android.os.Bundle
import android.view.View
import cn.jpush.android.api.JPushInterface
import com.lzg.extend.StringDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.ruanmeng.base.*
import com.ruanmeng.share.BaseHttp
import com.ruanmeng.utils.ActivityStack
import com.ruanmeng.utils.CommonUtil
import com.umeng.socialize.UMAuthListener
import com.umeng.socialize.UMShareAPI
import com.umeng.socialize.bean.SHARE_MEDIA
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject
import com.umeng.socialize.UMShareConfig



class LoginActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        init_title("登录账号")
    }

    override fun init_title() {
        super.init_title()
        bt_login.setBackgroundResource(R.drawable.rec_bg_d0d0d0)
        bt_login.isClickable = false

        et_name.addTextChangedListener(this@LoginActivity)
        et_pwd.addTextChangedListener(this@LoginActivity)

        if (getString("mobile").isNotEmpty()) {
            et_name.setText(getString("mobile"))
            et_name.setSelection(et_name.text.length)
        }

        if (intent.getBooleanExtra("offLine", false)) {
            clearData()
            ActivityStack.screenManager.popAllActivityExcept(this@LoginActivity::class.java)
        }
    }

    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.tv_forget -> startActivity<ForgetActivity>()
            R.id.tv_register -> startActivity<RegisterActivity>()
            R.id.bt_login -> {
                if (!CommonUtil.isMobile(et_name.text.toString())) {
                    et_name.requestFocus()
                    et_name.setText("")
                    showToast("手机号码格式错误，请重新输入")
                    return
                }

                if (et_pwd.text.length < 6) {
                    et_pwd.requestFocus()
                    showToast("密码长度不少于6位")
                    return
                }

                OkGo.post<String>(BaseHttp.login_sub)
                        .tag(this@LoginActivity)
                        .params("accountName", et_name.text.trim().toString())
                        .params("password", et_pwd.text.trim().toString())
                        .params("loginType", "mobile")
                        .execute(object : StringDialogCallback(this@LoginActivity) {

                            override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {
                                val obj = JSONObject(response.body()).getJSONObject("object")

                                putBoolean("isLogin", true)
                                putString("token", obj.optString("token"))
                                putString("mobile", obj.optString("mobile"))
                                putString("loginType", "mobile")

                                startActivity<MainActivity>()
                                ActivityStack.screenManager.popActivities(this@LoginActivity::class.java)
                            }

                        })
            }
            R.id.login_wx -> {
                UMShareAPI.get(this@LoginActivity).setShareConfig(UMShareConfig().apply { isNeedAuthOnGetUserInfo = true })

                UMShareAPI.get(baseContext).getPlatformInfo(
                        this@LoginActivity,
                        SHARE_MEDIA.WEIXIN,
                        object : UMAuthListener {

                            /**
                             * @desc 授权成功的回调
                             * @param platform 平台名称
                             * @param action 行为序号，开发者用不上
                             * @param data 用户资料返回
                             */
                            override fun onComplete(platform: SHARE_MEDIA, action: Int, data: MutableMap<String, String>) {
                                getThirdLogin("WX",
                                        data["uid"] ?: "",
                                        data["name"] ?: "",
                                        data["iconurl"] ?: "")
                            }

                            /**
                             * @desc 授权取消的回调
                             * @param platform 平台名称
                             * @param action 行为序号，开发者用不上
                             */
                            override fun onCancel(platform: SHARE_MEDIA, action: Int) {}

                            /**
                             * @desc 授权失败的回调
                             * @param platform 平台名称
                             * @param action 行为序号，开发者用不上
                             * @param t 错误原因
                             */
                            override fun onError(platform: SHARE_MEDIA, action: Int, t: Throwable) = showToast("授权失败")

                            /**
                             * @desc 授权开始的回调
                             * @param platform 平台名称
                             */
                            override fun onStart(platform: SHARE_MEDIA) {}

                        })
            }
            R.id.login_qq -> {
                UMShareAPI.get(this@LoginActivity).setShareConfig(UMShareConfig().apply { isNeedAuthOnGetUserInfo = true })

                UMShareAPI.get(baseContext).getPlatformInfo(
                        this@LoginActivity,
                        SHARE_MEDIA.QQ,
                        object : UMAuthListener {

                            /**
                             * @desc 授权成功的回调
                             * @param platform 平台名称
                             * @param action 行为序号，开发者用不上
                             * @param data 用户资料返回
                             */
                            override fun onComplete(platform: SHARE_MEDIA, action: Int, data: MutableMap<String, String>) {
                                getThirdLogin("QQ",
                                        data["uid"] ?: "",
                                        data["name"] ?: "",
                                        data["iconurl"] ?: "")
                            }

                            /**
                             * @desc 授权取消的回调
                             * @param platform 平台名称
                             * @param action 行为序号，开发者用不上
                             */
                            override fun onCancel(platform: SHARE_MEDIA, action: Int) {}

                            /**
                             * @desc 授权失败的回调
                             * @param platform 平台名称
                             * @param action 行为序号，开发者用不上
                             * @param t 错误原因
                             */
                            override fun onError(platform: SHARE_MEDIA, action: Int, t: Throwable) = showToast("授权失败")

                            /**
                             * @desc 授权开始的回调
                             * @param platform 平台名称
                             */
                            override fun onStart(platform: SHARE_MEDIA) {}

                        })
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        UMShareAPI.get(this@LoginActivity).onActivityResult(requestCode, resultCode, data)
    }

    private fun getThirdLogin(loginType: String,
                              openId: String,
                              nickName: String,
                              headImgUrl: String) {
        OkGo.post<String>(BaseHttp.login_sub)
                .tag(this@LoginActivity)
                .params("loginType", loginType)
                .params("openId", openId)
                .execute(object : StringDialogCallback(this@LoginActivity) {

                    override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {
                        val obj = JSONObject(response.body()).getJSONObject("object")

                        putBoolean("isLogin", true)
                        putString("token", obj.optString("token"))
                        putString("mobile", obj.optString("mobile"))
                        putString("loginType", loginType)

                        startActivity<MainActivity>()
                        ActivityStack.screenManager.popActivities(this@LoginActivity::class.java)
                    }

                    override fun onSuccessResponseErrorCode(response: Response<String>, msg: String, msgCode: String) {
                        if (msgCode == "105") {
                            val intent = Intent(baseContext, BindActivity::class.java)
                            intent.putExtra("loginType", loginType)
                            intent.putExtra("openId", openId)
                            intent.putExtra("nickName", nickName)
                            intent.putExtra("headImgUrl", headImgUrl)
                            startActivity(intent)
                        } else showToast(msg)
                    }

                })
    }

    private fun clearData() {
        putBoolean("isLogin", false)
        putString("token", "")

        putString("nickName", "")
        putString("userhead", "")
        putString("sex", "")
        putString("pass", "")
        putString("status", "")

        when (getString("loginType")) {
            "WX", "QQ" -> UMShareAPI.get(baseContext).deleteOauth(
                    this@LoginActivity,
                    if (getString("loginType") == "WX") SHARE_MEDIA.WEIXIN else SHARE_MEDIA.QQ,
                    null)
        }
        putString("loginType", "")

        JPushInterface.stopPush(applicationContext)
        JPushInterface.clearAllNotifications(applicationContext)
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (et_name.text.isNotBlank()
                && et_pwd.text.isNotBlank()) {
            bt_login.setBackgroundResource(R.drawable.rec_bg_blue_shade)
            bt_login.isClickable = true
        } else {
            bt_login.setBackgroundResource(R.drawable.rec_bg_d0d0d0)
            bt_login.isClickable = false
        }
    }
}
