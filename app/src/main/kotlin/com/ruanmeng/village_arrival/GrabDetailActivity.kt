package com.ruanmeng.village_arrival

import android.os.Bundle
import com.ruanmeng.base.BaseActivity

class GrabDetailActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grab_detail)
        init_title("订单详情", "取消订单")
    }

    override fun init_title() {
        super.init_title()
        @Suppress("DEPRECATION")
        tvRight.setTextColor(resources.getColor(R.color.light))
    }
}
