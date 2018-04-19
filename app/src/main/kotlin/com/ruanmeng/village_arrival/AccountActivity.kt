package com.ruanmeng.village_arrival

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
        bt_withdraw.setOnClickListener { startActivity<WithdrawActivity>() }
    }

    override fun getData() {
        OkGo.post<String>(BaseHttp.my_account_info)
                .tag(this@AccountActivity)
                .headers("token", getString("token"))
                .execute(object : StringDialogCallback(baseContext) {

                    override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                        val obj = JSONObject(response.body())
                        val balance = obj.optString("balance", "0")
                        val ensureSum = obj.optString("ensureSum", "0")
                        val profitSum = obj.optString("profitSum", "0")

                        account_total.startIncreaseAnimator(balance.toFloat())
                        account_commission.text = DecimalFormat(",##0.##").format(profitSum.toDouble())
                        account_save.text = DecimalFormat(",##0.##").format(ensureSum.toDouble())

                        if (ensureSum.toDouble() > 0) account_promise.setRightString("已缴纳保证金")
                    }

                })
    }
}
