package com.ruanmeng.village_arrival

import android.os.Bundle
import com.ruanmeng.base.BaseActivity

class WithdrawResultActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_withdraw_result)
        init_title("结果")
    }
}
