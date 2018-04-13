package com.ruanmeng.village_arrival

import android.os.Bundle
import com.ruanmeng.base.BaseActivity
import com.ruanmeng.base.startActivity
import kotlinx.android.synthetic.main.activity_info.*

class InfoActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)
        init_title("个人资料")
    }

    override fun init_title() {
        super.init_title()

        info_name.setOnClickListener { startActivity<NicknameActivity>() }
        info_real.setOnClickListener { startActivity<RealActivity>() }
    }
}
