package com.ruanmeng.village_arrival

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import com.lzg.extend.StringDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.ruanmeng.base.BaseActivity
import com.ruanmeng.base.getString
import com.ruanmeng.base.showToast
import com.ruanmeng.share.BaseHttp
import kotlinx.android.synthetic.main.activity_margin.*
import org.json.JSONObject
import java.text.DecimalFormat

class MarginActivity : BaseActivity() {

    private var mBailSum = 36.0
    private var mBailStatus = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_margin)
        init_title("保证金", "说明")

        getData()
    }

    override fun init_title() {
        super.init_title()
        bt_freeze.text = "冻结中"
        bt_freeze.setBackgroundResource(R.drawable.rec_bg_d0d0d0)
        bt_freeze.isClickable = false
    }

    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.bt_freeze -> {
                when (bt_freeze.text.toString()) {
                    "充值" -> showSheetDialog()
                    "解冻" -> {
                        OkGo.post<String>(BaseHttp.un_blocked)
                                .tag(this@MarginActivity)
                                .headers("token", getString("token"))
                                .execute(object : StringDialogCallback(baseContext) {

                                    override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                                        showToast(msg)
                                        mBailSum = 0.0
                                        updateUI()
                                    }

                                })
                    }
                }
            }
        }
    }

    override fun getData() {
        OkGo.post<String>(BaseHttp.check_user_bail)
                .tag(this@MarginActivity)
                .headers("token", getString("token"))
                .execute(object : StringDialogCallback(baseContext) {

                    override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                        val obj = JSONObject(response.body()).getJSONObject("object") ?: JSONObject()

                        mBailSum = obj.optString("baiSum", "0").toDouble()
                        mBailStatus = obj.optString("userBail")

                        margin_expand.expand()
                        updateUI()
                    }
                })
    }

    private fun updateUI() {
        margin_total.text = DecimalFormat(",##0.00").format(mBailSum)
        when {
            mBailSum == 0.0 -> {
                margin_hint.text = "您当前未缴纳保证金"
                margin_status .text = "未缴纳"
                bt_freeze.text = "充值"
                bt_freeze.setBackgroundResource(R.drawable.rec_bg_blue_shade)
                bt_freeze.isClickable = true
            }
            mBailSum >= 100 -> {
                margin_hint.text = getString(R.string.margin_hint)
                margin_status .text = "冻结中"
                if (mBailStatus == "1") bt_freeze.text = "解冻"
                else {
                    bt_freeze.text = "冻结中"
                    bt_freeze.setBackgroundResource(R.drawable.rec_bg_d0d0d0)
                    bt_freeze.isClickable = false
                }
            }
            mBailSum > 0 && mBailSum < 100 -> {
                if (mBailStatus == "1") {
                    margin_hint.text = getString(R.string.margin_hint)
                    margin_status .text = "冻结中"
                    bt_freeze.text = "解冻"
                } else {
                    margin_hint.text = "您当前保证金余额不足"
                    margin_status .text = "余额不足"
                    bt_freeze.text = "充值"
                }
            }
        }
    }

    private fun getPayData(count: String) {}

    @SuppressLint("InflateParams")
    private fun showSheetDialog() {
        val view = LayoutInflater.from(baseContext).inflate(R.layout.dialog_bail_pay, null) as View
        val payCount = view.findViewById<EditText>(R.id.pay_count)
        val payGroup = view.findViewById<RadioGroup>(R.id.pay_group)
        val btPay = view.findViewById<Button>(R.id.bt_pay)
        val dialog = BottomSheetDialog(baseContext)

        payGroup.check(R.id.pay_check1)

        val smallSum = 100 - mBailSum
        payCount.hint = "请输入保证金金额（不少于${DecimalFormat(",##0.00").format(smallSum)}）"
        payCount.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isNotEmpty()) {
                    if ("." == s.toString()) {
                        payCount.setText("0.")
                        payCount.setSelection(payCount.text.length) //设置光标的位置
                    }
                }
            }

            override fun afterTextChanged(s: Editable) {
                val temp = s.toString()
                val posDot = temp.indexOf(".")
                if (posDot < 0) {
                    if (temp.length > 7) s.delete(7, 8)
                } else {
                    if (temp.length - posDot - 1 > 2) s.delete(posDot + 3, posDot + 4)
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        })

        btPay.setOnClickListener {
            if (payCount.text.isEmpty()) {
                showToast("请输入保证金金额")
                return@setOnClickListener
            }

            if (payCount.text.toString().toDouble() < smallSum) {
                showToast("保证金金额不少于${DecimalFormat(",##0.00").format(smallSum)}元")
                return@setOnClickListener
            }

            dialog.dismiss()

            window.decorView.postDelayed({ getPayData(payCount.text.toString()) }, 300)
        }

        dialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        dialog.setContentView(view)
        dialog.show()
    }
}
