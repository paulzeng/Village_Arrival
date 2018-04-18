package com.ruanmeng.village_arrival

import android.os.Bundle
import com.ruanmeng.base.BaseActivity
import com.ruanmeng.base.startActivity
import kotlinx.android.synthetic.main.activity_live_done.*

class LiveDoneActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_done)
        init_title("结果")
    }

    override fun init_title() {
        super.init_title()
        bt_look.setOnClickListener { startActivity<LiveMineActivity>() }
    }
}
