package com.ruanmeng.village_arrival

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.View
import com.lzg.extend.StringDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.ruanmeng.base.BaseActivity
import com.ruanmeng.base.getString
import com.ruanmeng.base.showToast
import com.ruanmeng.base.startActivity
import com.ruanmeng.model.RefreshMessageEvent
import com.ruanmeng.share.BaseHttp
import com.ruanmeng.utils.ActivityStack
import kotlinx.android.synthetic.main.activity_issue_pay.*
import org.greenrobot.eventbus.EventBus

class IssuePayActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_issue_pay)
        init_title("支付订单")

        pay_check1.performClick()
    }

    @Suppress("DEPRECATION")
    override fun init_title() {
        super.init_title()

        pay_total.text = Html.fromHtml(String.format(
                "<big><big><big>%1\$s</big></big></big>    元",
                intent.getStringExtra("commission")))

        pay_group.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.pay_check1 -> mPosition = 0
                R.id.pay_check2 -> mPosition = 1
                R.id.pay_check3 -> mPosition = 2
            }
        }
    }

    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.bt_pay -> {
                when (mPosition) {
                    0 -> {
                        OkGo.post<String>(BaseHttp.balance_pay_order)
                                .tag(this@IssuePayActivity)
                                .headers("token", getString("token"))
                                .params("goodsOrderId", intent.getStringExtra("goodsOrderId"))
                                .execute(object : StringDialogCallback(baseContext) {

                                    override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                                        EventBus.getDefault().post(RefreshMessageEvent("支付成功"))
                                        val intent = Intent(baseContext, TaskResultActivity::class.java)
                                        intent.putExtra("title", "支付成功")
                                        startActivity(intent)
                                        ActivityStack.screenManager.popActivities(this@IssuePayActivity::class.java)
                                    }

                                    override fun onSuccessResponseErrorCode(response: Response<String>, msg: String, msgCode: String) {
                                        val intent = Intent(baseContext, TaskResultActivity::class.java)
                                        intent.putExtra("title", "支付失败")
                                        intent.putExtra("message", msg)
                                        startActivity(intent)
                                    }

                                })
                    }
                }
            }
        }
    }
}
