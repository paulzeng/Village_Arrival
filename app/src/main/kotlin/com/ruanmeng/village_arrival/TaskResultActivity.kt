package com.ruanmeng.village_arrival

import android.os.Bundle
import android.view.View
import com.ruanmeng.base.BaseActivity
import com.ruanmeng.utils.ActivityStack
import com.ruanmeng.utils.DialogHelper
import kotlinx.android.synthetic.main.activity_task_result.*

class TaskResultActivity : BaseActivity() {

    private var mTitle = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_result)
        mTitle = intent.getStringExtra("title")
        init_title(mTitle)
    }

    override fun init_title() {
        super.init_title()
        when (mTitle) {
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
                if (mTitle == "支付成功") {
                    DialogHelper.showHintDialog(baseContext,
                            "取消订单",
                            getString(R.string.cancel_grab),
                            "取消",
                            "确定") {

                    }
                } else {
                    when (intent.getStringExtra("hint")) {
                        "提交订单", "发布列表" -> {
                            intent.setClass(baseContext, IssueDetailActivity::class.java)
                            startActivity(intent)
                            ActivityStack.screenManager.popActivities(
                                    this@TaskResultActivity::class.java,
                                    IssuePayActivity::class.java)
                        }
                        "发布详情" -> ActivityStack.screenManager.popActivities(
                                this@TaskResultActivity::class.java,
                                IssuePayActivity::class.java)
                    }
                }
            }
            R.id.task_look -> {
                if (mTitle == "支付失败") {
                    ActivityStack.screenManager.popActivities(this@TaskResultActivity::class.java)
                } else {
                    when (intent.getStringExtra("hint")) {
                        "提交订单", "发布列表" -> {
                            intent.setClass(baseContext, IssueDetailActivity::class.java)
                            startActivity(intent)
                            ActivityStack.screenManager.popActivities(this@TaskResultActivity::class.java)
                        }
                        "发布详情" -> ActivityStack.screenManager.popActivities(this@TaskResultActivity::class.java)
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (mTitle == "支付失败") ActivityStack.screenManager.popActivities(IssuePayActivity::class.java)
    }
}
