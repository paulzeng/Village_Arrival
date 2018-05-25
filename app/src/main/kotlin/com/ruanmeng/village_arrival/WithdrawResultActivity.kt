package com.ruanmeng.village_arrival

import android.os.Bundle
import com.ruanmeng.base.BaseActivity
import kotlinx.android.synthetic.main.activity_withdraw_result.*

class WithdrawResultActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_withdraw_result)
        init_title("结果")
    }

    override fun init_title() {
        super.init_title()
        when (intent.getStringExtra("title")) {
            "提现成功" -> {
                result_img.setImageResource(R.mipmap.ass_lab_icon09)
                result_hint.text = "提现成功"
                result_content.text = "顺送费将在2个工作日内到账"
            }
            "提现失败" -> {
                result_img.setImageResource(R.mipmap.ass_lab_icon10)
                result_hint.text = "提现失败"
                result_content.text = intent.getStringExtra("message")
            }
        }

        bt_back.setOnClickListener { onBackPressed() }
    }
}
