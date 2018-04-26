package com.ruanmeng.village_arrival

import android.content.Intent
import android.os.Bundle
import com.lzg.extend.StringDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.ruanmeng.base.BaseActivity
import com.ruanmeng.base.getString
import com.ruanmeng.base.startActivity
import com.ruanmeng.share.BaseHttp
import com.ruanmeng.utils.startIncreaseAnimator
import kotlinx.android.synthetic.main.activity_account.*
import org.json.JSONObject
import java.text.DecimalFormat

class AccountActivity : BaseActivity() {

    private var mBalance = 0f
    private var mEnsureSum = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)
        init_title("我的账户", "说明")

        getData()
    }

    override fun init_title() {
        super.init_title()

        account_promise.setOnClickListener { startActivity<MarginActivity>() }
        account_detail.setOnClickListener { startActivity<AccountDetailActivity>() }
        bt_withdraw.setOnClickListener {
            val intent = Intent(baseContext, WithdrawActivity::class.java)
            intent.putExtra("balance", mBalance.toString())
            startActivity(intent)
        }
    }

    override fun getData() {
        OkGo.post<String>(BaseHttp.my_account_info)
                .tag(this@AccountActivity)
                .headers("token", getString("token"))
                .execute(object : StringDialogCallback(baseContext) {

                    override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                        val obj = JSONObject(response.body())
                        mBalance = obj.optString("balance", "0").toFloat()
                        mEnsureSum = obj.optString("ensureSum", "0").toDouble()
                        val profitSum = obj.optString("profitSum", "0").toDouble()

                        account_total.startIncreaseAnimator(mBalance)
                        account_commission.text = DecimalFormat(",##0.##").format(profitSum)
                        account_save.text = DecimalFormat(",##0.##").format(mEnsureSum)

                        account_promise.setRightString(when {
                            mEnsureSum > 0 && mEnsureSum < 100 -> "保证金不足"
                            mEnsureSum >= 100 -> "已缴纳保证金"
                            else -> "未缴纳保证金，立即充值缴纳"
                        })
                    }

                })
    }
}
