package com.ruanmeng.village_arrival

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import com.lzg.extend.StringDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.ruanmeng.base.BaseActivity
import com.ruanmeng.base.showToast
import com.ruanmeng.share.BaseHttp
import com.ruanmeng.share.Const
import com.ruanmeng.utils.ActivityStack
import com.ruanmeng.utils.CommonUtil
import kotlinx.android.synthetic.main.activity_register.*
import org.json.JSONObject

class RegisterActivity : BaseActivity() {

    private var time_count: Int = 180
    private lateinit var thread: Runnable
    private var YZM: String = ""
    private var mTel: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        init_title("注册账号")
    }

    override fun init_title() {
        super.init_title()
        bt_register.setBackgroundResource(R.drawable.rec_bg_d0d0d0)
        bt_register.isClickable = false

        et_name.addTextChangedListener(this@RegisterActivity)
        et_yzm.addTextChangedListener(this@RegisterActivity)
        et_pwd.addTextChangedListener(this@RegisterActivity)
    }

    @SuppressLint("SetTextI18n")
    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.tv_login -> onBackPressed()
            R.id.bt_yzm -> {
                if (et_name.text.isBlank()) {
                    et_name.requestFocus()
                    showToast("请输入手机号")
                    return
                }

                if (!CommonUtil.isMobile(et_name.text.toString())) {
                    et_name.requestFocus()
                    et_name.setText("")
                    showToast("手机号码格式错误，请重新输入")
                    return
                }

                thread = Runnable {
                    bt_yzm.text = "${time_count}秒后重发"
                    if (time_count > 0) {
                        bt_yzm.postDelayed(thread, 1000)
                        time_count--
                    } else {
                        bt_yzm.text = "获取验证码"
                        bt_yzm.isClickable = true
                        time_count = 180
                    }
                }

                OkGo.post<String>(BaseHttp.identify_get)
                        .tag(this@RegisterActivity)
                        .params("mobile", et_name.text.trim().toString())
                        .params("time", Const.MAKER)
                        .execute(object : StringDialogCallback(baseContext) {

                            override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                                YZM = JSONObject(response.body()).optString("object")
                                mTel = et_name.text.trim().toString()
                                if (BuildConfig.LOG_DEBUG) et_yzm.setText(YZM)

                                bt_yzm.isClickable = false
                                time_count = 180
                                bt_yzm.post(thread)
                            }

                        })
            }
            R.id.bt_register -> {
                if (!CommonUtil.isMobile(et_name.text.toString())) {
                    et_name.requestFocus()
                    et_name.setText("")
                    showToast("手机号码格式错误，请重新输入")
                    return
                }

                if (et_name.text.toString() != mTel) {
                    showToast("手机号码不匹配，请重新获取验证码")
                    return
                }

                if (et_yzm.text.trim().toString() != YZM) {
                    et_yzm.requestFocus()
                    et_yzm.setText("")
                    showToast("验证码错误，请重新输入")
                    return
                }

                if (et_pwd.text.length < 6) {
                    showToast("新密码长度不少于6位")
                    return
                }

                OkGo.post<String>(BaseHttp.register_sub)
                        .tag(this@RegisterActivity)
                        .params("mobile", mTel)
                        .params("smscode", et_yzm.text.trim().toString())
                        .params("password", et_pwd.text.trim().toString())
                        .execute(object : StringDialogCallback(baseContext) {

                            override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                                showToast(msg)
                                ActivityStack.screenManager.popActivities(this@RegisterActivity::class.java)
                            }

                        })
            }
        }
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (et_name.text.isNotBlank()
                && et_yzm.text.isNotBlank()
                && et_pwd.text.isNotBlank()) {
            bt_register.setBackgroundResource(R.drawable.rec_bg_blue_shade)
            bt_register.isClickable = true
        } else {
            bt_register.setBackgroundResource(R.drawable.rec_bg_d0d0d0)
            bt_register.isClickable = false
        }
    }
}
