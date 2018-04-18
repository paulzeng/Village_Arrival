package com.ruanmeng.village_arrival

import android.os.Bundle
import android.text.Html
import com.ruanmeng.base.BaseActivity
import kotlinx.android.synthetic.main.activity_issue_pay.*

class IssuePayActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_issue_pay)
        init_title("支付订单")
    }

    @Suppress("DEPRECATION")
    override fun init_title() {
        super.init_title()

        pay_total.text = Html.fromHtml(String.format("<big><big><big>%1\$s</big></big></big>    元", "500"))
    }
}
