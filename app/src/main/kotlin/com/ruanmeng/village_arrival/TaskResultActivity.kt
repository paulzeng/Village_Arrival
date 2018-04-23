package com.ruanmeng.village_arrival

import android.os.Bundle
import android.view.View
import com.ruanmeng.base.BaseActivity
import com.ruanmeng.utils.ActivityStack
import kotlinx.android.synthetic.main.activity_task_result.*

class TaskResultActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_result)
        init_title(intent.getStringExtra("title"))
    }

    override fun init_title() {
        super.init_title()
        when (intent.getStringExtra("title")) {
            "支付成功" -> {
                task_img.setImageResource(R.mipmap.pay_success)
                task_hint.text = "支付成功"
                task_result.text = "接单中，请耐心等待"
                task_cancel.text = "取消订单"
                task_look.text = "查看订单"
            }
            "支付失败" -> {
                task_img.setImageResource(R.mipmap.pay_error)
                task_hint.text = "支付失败"
                task_result.text = intent.getStringExtra("message")
                task_cancel.text = "查看订单"
                task_look.text = "重新支付"
            }
        }
    }

    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.task_cancel -> {
                when (task_cancel.text.toString()) {
                    "取消订单" -> {

                    }
                    "查看订单" -> {
                        ActivityStack.screenManager.popActivities(this@TaskResultActivity::class.java, IssuePayActivity::class.java)
                    }
                }
            }
            R.id.task_look -> {
                when (task_look.text.toString()) {
                    "查看订单" -> ActivityStack.screenManager.popActivities(this@TaskResultActivity::class.java)
                    "重新支付" -> {
                        ActivityStack.screenManager.popActivities(this@TaskResultActivity::class.java, IssuePayActivity::class.java)
                    }
                }
            }
        }
    }
}
