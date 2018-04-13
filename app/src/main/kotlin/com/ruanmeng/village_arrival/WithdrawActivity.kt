package com.ruanmeng.village_arrival

import android.os.Bundle
import com.ruanmeng.base.BaseActivity

class WithdrawActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_withdraw)
        init_title("申请提现", "说明")
    }
}
