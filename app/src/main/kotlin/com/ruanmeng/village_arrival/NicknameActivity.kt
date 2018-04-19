package com.ruanmeng.village_arrival

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.view.View
import com.lzg.extend.StringDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.ruanmeng.base.BaseActivity
import com.ruanmeng.base.getString
import com.ruanmeng.base.putString
import com.ruanmeng.base.showToast
import com.ruanmeng.share.BaseHttp
import com.ruanmeng.utils.ActivityStack
import com.ruanmeng.utils.NameLengthFilter
import kotlinx.android.synthetic.main.activity_nickname.*
import java.util.regex.Pattern

class NicknameActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nickname)
        init_title("设置昵称")
    }

    override fun init_title() {
        super.init_title()
        bt_save.setBackgroundResource(R.drawable.rec_bg_d0d0d0)
        bt_save.isClickable = false

        et_name.filters = arrayOf<InputFilter>(NameLengthFilter(16))
        et_name.addTextChangedListener(this@NicknameActivity)

        if (getString("nickName").isNotEmpty()) {
            et_name.setText(getString("nickName"))
            et_name.setSelection(et_name.text.length)
        }
    }

    override fun doClick(v: View) {
        super.doClick(v)
        when(v.id) {
            R.id.bt_save -> {
                if (et_name.text.trim().toString() == getString("nickName")) {
                    showToast("未做任何修改")
                    return
                }

                if (pageNum < 4) {
                    showToast("昵称长度不少于4个字符（一个汉字两个字符）")
                    return
                }

                OkGo.post<String>(BaseHttp.nickName_change_sub)
                        .tag(this@NicknameActivity)
                        .isMultipart(true)
                        .headers("token", getString("token"))
                        .params("nickName", et_name.text.trim().toString())
                        .execute(object : StringDialogCallback(baseContext) {

                            override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                                showToast(msg)
                                putString("nickName", et_name.text.toString())
                                ActivityStack.screenManager.popActivities(this@NicknameActivity::class.java)
                            }

                        })
            }
        }
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (et_name.text.isNotBlank()) {
            bt_save.setBackgroundResource(R.drawable.rec_bg_blue_shade)
            bt_save.isClickable = true
        } else {
            bt_save.setBackgroundResource(R.drawable.rec_bg_d0d0d0)
            bt_save.isClickable = false
        }
    }

    override fun afterTextChanged(s: Editable) {
        pageNum = 0
        (0 until s.length).forEach {
            val matcher = Pattern.compile("[\u4e00-\u9fa5]").matcher(s[it].toString())
            if (matcher.matches()) pageNum += 2
            else pageNum ++
        }
    }
}
