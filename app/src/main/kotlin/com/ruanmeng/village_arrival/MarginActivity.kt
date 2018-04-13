package com.ruanmeng.village_arrival

import android.os.Bundle
import com.ruanmeng.base.BaseActivity

class MarginActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_margin)
        init_title("保证金", "说明")
    }
}
