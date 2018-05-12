package com.ruanmeng.village_arrival

import android.annotation.SuppressLint
import android.content.Intent
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
import com.cuieney.sdk.rxpay.RxPay
import com.lzg.extend.StringDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.lzy.okgo.utils.OkLogger
import com.ruanmeng.base.BaseActivity
import com.ruanmeng.base.getString
import com.ruanmeng.base.showToast
import com.ruanmeng.base.startActivity
import com.ruanmeng.share.BaseHttp
import com.ruanmeng.utils.startIncreaseAnimator
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_account.*
import org.json.JSONObject
import java.math.BigDecimal
import java.text.DecimalFormat

class AccountActivity : BaseActivity() {

    private var mBalance = 0f
    private var mEnsureSum = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)
        init_title("我的账户", "开发票")
    }

    override fun onStart() {
        super.onStart()
        getData()
    }

    override fun init_title() {
        super.init_title()

        account_promise.setOnClickListener { startActivity<MarginActivity>() }
        account_detail.setOnClickListener { startActivity<AccountDetailActivity>() }
        bt_withdraw.setOnClickListener {
            val intent = Intent(baseContext, WithdrawActivity::class.java)
            intent.putExtra("balance", (mBalance - mEnsureSum).toString())
            startActivity(intent)
        }
        tvRight.setOnClickListener { startActivity<AccountTicketActivity>() }
    }

    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.account_recharge -> showSheetDialog()
        }
    }

    override fun getData() {
        OkGo.post<String>(BaseHttp.my_account_info)
                .tag(this@AccountActivity)
                .headers("token", getString("token"))
                .execute(object : StringDialogCallback(baseContext) {

                    override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                        val obj = JSONObject(response.body())
                        val balance = obj.optString("balance", "0").toFloat()
                        mEnsureSum = obj.optString("ensureSum", "0").toDouble()
                        val profitSum = obj.optString("profitSum", "0").toDouble()

                        val mProfitSum = BigDecimal(profitSum).setScale(2, BigDecimal.ROUND_DOWN).toDouble()
                        mBalance = BigDecimal(balance.toDouble()).setScale(2, BigDecimal.ROUND_DOWN).toFloat()

                        account_total.startIncreaseAnimator(mBalance)
                        account_commission.text = DecimalFormat(",##0.##").format(mProfitSum)
                        account_save.text = DecimalFormat(",##0.##").format(mEnsureSum)

                        account_promise.setRightString(when {
                            mEnsureSum > 0 && mEnsureSum < 100 -> "保证金不足"
                            mEnsureSum >= 100 -> "已缴纳保证金"
                            else -> "未缴纳保证金，立即充值缴纳"
                        })
                    }

                })
    }

    private fun getThirdPayData(count: String, type: String) {
        OkGo.post<String>(BaseHttp.recharge_balance_request)
                .tag(this@AccountActivity)
                .headers("token", getString("token"))
                .params("rechargeSum", count)
                .params("payType", type)
                .execute(object : StringDialogCallback(baseContext) {

                    override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                        val obj = JSONObject(response.body()).optString("object")
                        val data = JSONObject(response.body()).optJSONObject("object") ?: JSONObject()
                        when (type) {
                            "AliPay" -> RxPay(baseContext).requestAlipay(obj)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({
                                        if (it) {
                                            showToast("支付成功")
                                            window.decorView.postDelayed({ getData() }, 300)
                                        } else showToast("支付失败")
                                    }) { OkLogger.printStackTrace(it) }
                            "WxPay" -> RxPay(baseContext).requestWXpay(data)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({
                                        if (it) {
                                            showToast("支付成功")
                                            window.decorView.postDelayed({ getData() }, 300)
                                        } else showToast("支付失败")
                                    }) { OkLogger.printStackTrace(it) }
                        }
                    }

                })
    }

    @SuppressLint("InflateParams")
    private fun showSheetDialog() {
        val view = LayoutInflater.from(baseContext).inflate(R.layout.dialog_account_pay, null) as View
        val payCount = view.findViewById<EditText>(R.id.pay_count)
        val payGroup = view.findViewById<RadioGroup>(R.id.pay_group)
        val btPay = view.findViewById<Button>(R.id.bt_pay)
        val dialog = BottomSheetDialog(baseContext)

        payGroup.check(R.id.pay_check1)

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
                showToast("请输入充值金额")
                return@setOnClickListener
            }

            if (payCount.text.toString().toDouble() == 0.0) {
                showToast("输入金额为0元，请重新输入")
                return@setOnClickListener
            }

            dialog.dismiss()

            when (payGroup.checkedRadioButtonId) {
                R.id.pay_check1 -> window.decorView.postDelayed({
                    getThirdPayData(payCount.text.toString(), "AliPay")
                }, 300)
                R.id.pay_check2 -> window.decorView.postDelayed({
                    getThirdPayData(payCount.text.toString(), "WxPay")
                }, 300)
            }
        }

        dialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        dialog.setContentView(view)
        dialog.show()
    }
}
