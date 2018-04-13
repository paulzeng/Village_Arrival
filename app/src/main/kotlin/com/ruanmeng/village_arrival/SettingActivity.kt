package com.ruanmeng.village_arrival

import android.os.Bundle
import com.ruanmeng.base.BaseActivity
import com.ruanmeng.base.startActivity
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        init_title("设置")
    }

    override fun init_title() {
        super.init_title()

        setting_feedback.setOnClickListener { startActivity<FeedbackActivity>() }
        setting_about.setOnClickListener { startActivity<AboutActivity>() }
        setting_problem.setOnClickListener { startActivity<ProblemActivity>() }
    }
}
