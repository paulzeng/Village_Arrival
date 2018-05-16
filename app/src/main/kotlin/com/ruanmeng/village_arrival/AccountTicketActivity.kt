package com.ruanmeng.village_arrival

import android.os.Bundle
import android.text.InputFilter
import android.view.View
import com.lzg.extend.StringDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.ruanmeng.base.BaseActivity
import com.ruanmeng.base.getString
import com.ruanmeng.base.showToast
import com.ruanmeng.share.BaseHttp
import com.ruanmeng.utils.ActivityStack
import com.ruanmeng.utils.CommonUtil
import com.ruanmeng.utils.DialogHelper
import com.ruanmeng.utils.NameLengthFilter
import kotlinx.android.synthetic.main.activity_account_ticket.*
import org.json.JSONObject

class AccountTicketActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_ticket)
        init_title("发票信息")

        getData()
    }

    override fun init_title() {
        super.init_title()
        bt_save.setBackgroundResource(R.drawable.rec_bg_d0d0d0)
        bt_save.isClickable = false

        et_name.filters = arrayOf<InputFilter>(NameLengthFilter(16))
        et_title.addTextChangedListener(this@AccountTicketActivity)
        ticket_time.addTextChangedListener(this@AccountTicketActivity)
        et_name.addTextChangedListener(this@AccountTicketActivity)
        et_phone.addTextChangedListener(this@AccountTicketActivity)
        et_detail.addTextChangedListener(this@AccountTicketActivity)
    }

    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.ticket_time_ll -> {
                DialogHelper.showTimeDialog(baseContext) { _, date ->
                    ticket_time.text = date
                }
            }
            R.id.bt_save -> {
                if (!CommonUtil.isMobile(et_phone.text.toString())) {
                    et_phone.requestFocus()
                    et_phone.setText("")
                    showToast("手机号码格式错误，请重新输入")
                    return
                }

                OkGo.post<String>(BaseHttp.add_invoice)
                        .tag(this@AccountTicketActivity)
                        .isMultipart(true)
                        .headers("token", getString("token"))
                        .params("invoiceTitle", et_title.text.trim().toString())
                        .params("invoiceConet", et_content.text.trim().toString())
                        .params("rnumber", et_code.text.toString())
                        .params("contacts", et_name.text.trim().toString())
                        .params("tel", et_phone.text.toString())
                        .params("address", et_detail.text.trim().toString())
                        .params("ym", ticket_time.text.toString())
                        .execute(object : StringDialogCallback(baseContext) {

                            override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                                showToast(msg)
                                ActivityStack.screenManager.popActivities(this@AccountTicketActivity::class.java)
                            }

                        })
            }
        }
    }

    override fun getData() {
        OkGo.post<String>(BaseHttp.hinvoice_data)
                .tag(this@AccountTicketActivity)
                .headers("token", getString("token"))
                .execute(object : StringDialogCallback(baseContext) {

                    override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                        val obj = JSONObject(response.body()).optJSONObject("object") ?: JSONObject()
                        et_title.setText(obj.optString("invoiceTitle"))
                        et_title.setSelection(et_title.text.length)
                        et_content.setText(obj.optString("invoiceConet"))
                        et_code.setText(obj.optString("rnumber"))
                        et_name.setText(obj.optString("contacts"))
                        et_phone.setText(obj.optString("tel"))
                        et_detail.setText(obj.optString("address"))
                    }

                })
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (et_title.text.isNotBlank()
                && ticket_time.text.isNotBlank()
                && et_name.text.isNotBlank()
                && et_phone.text.isNotBlank()
                && et_detail.text.isNotBlank()) {
            bt_save.setBackgroundResource(R.drawable.rec_bg_blue_shade)
            bt_save.isClickable = true
        } else {
            bt_save.setBackgroundResource(R.drawable.rec_bg_d0d0d0)
            bt_save.isClickable = false
        }
    }
}
