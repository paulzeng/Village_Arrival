package com.ruanmeng.village_arrival

import android.os.Bundle
import android.view.View
import com.ruanmeng.base.BaseActivity
import com.ruanmeng.base.startActivity

class IssueDetailActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_issue_detail)
        init_title("订单详情")
    }

    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.bt_press -> startActivity<IssuePayActivity>()
        }
    }
}
