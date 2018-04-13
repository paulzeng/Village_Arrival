package com.ruanmeng.village_arrival

import android.os.Bundle
import com.ruanmeng.base.BaseActivity
import com.ruanmeng.base.startActivity
import kotlinx.android.synthetic.main.activity_account.*

class AccountActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)
        init_title("我的账户", "说明")
    }

    override fun init_title() {
        super.init_title()

        account_promise.setOnClickListener { startActivity<MarginActivity>() }
        account_detail.setOnClickListener { startActivity<AccountDetailActivity>() }
        bt_withdraw.setOnClickListener { startActivity<WithdrawActivity>() }
    }
}
