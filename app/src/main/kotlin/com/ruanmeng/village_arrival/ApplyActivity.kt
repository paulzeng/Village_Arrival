package com.ruanmeng.village_arrival

import android.os.Bundle
import com.ruanmeng.base.BaseActivity
import com.ruanmeng.base.startActivity
import kotlinx.android.synthetic.main.activity_apply.*

class ApplyActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apply)
        init_title("立即抢单")
    }

    override fun init_title() {
        super.init_title()

        apply_gender.setOnClickListener {  }
        apply_real.setOnClickListener { startActivity<RealActivity>() }
        apply_addr.setOnClickListener { startActivity<AddressActivity>() }
    }
}
