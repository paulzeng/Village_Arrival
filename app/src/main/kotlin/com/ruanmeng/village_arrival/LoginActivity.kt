package com.ruanmeng.village_arrival

import android.os.Bundle
import android.view.View
import com.lzg.extend.StringDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.ruanmeng.base.*
import com.ruanmeng.share.BaseHttp
import com.ruanmeng.utils.ActivityStack
import com.ruanmeng.utils.CommonUtil
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject

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

                                startActivity<MainActivity>()
                                ActivityStack.screenManager.popActivities(this@LoginActivity::class.java)
                            }

                        })
            }
        }
    }

    private fun clearData() {
        putBoolean("isLogin", false)
        putString("token", "")

        putString("nickName", "")
        putString("userhead", "")
        putString("sex", "")
        putString("pass", "")
        putString("status", "")
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
