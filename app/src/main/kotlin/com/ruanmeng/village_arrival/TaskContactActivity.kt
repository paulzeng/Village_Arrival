package com.ruanmeng.village_arrival

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.ruanmeng.base.*
import com.ruanmeng.utils.ActivityStack
import com.ruanmeng.utils.DialogHelper
import kotlinx.android.synthetic.main.activity_task_contact.*

class TaskContactActivity : BaseActivity() {

    private var buyMobile = ""
    private var receiptMobile = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_contact)
        init_title("抢单")
    }

    override fun init_title() {
        super.init_title()
        val mTitle = intent.getStringExtra("title")
        task_hint.text = mTitle
        when (mTitle) {
            "抢单成功" -> {
                val type = intent.getStringExtra("type")
                val buyAddress = intent.getStringExtra("buyAddress")
                val buyName = intent.getStringExtra("buyName")
                val receiptAddress = intent.getStringExtra("receiptAddress")
                val receiptName = intent.getStringExtra("receiptName")
                buyMobile = intent.getStringExtra("buyMobile")
                receiptMobile = intent.getStringExtra("receiptMobile")

                task_img.setImageResource(R.mipmap.pay_success)
                task_result.text = getString(R.string.grab_success)
                task_look.visible()
                task_contact_ll.visible()

                task_img1.setImageResource(if (type == "1") R.mipmap.index_lab05 else R.mipmap.index_lab01)
                task_addr1.text = buyAddress
                task_name1.text = buyName
                task_addr2.text = receiptAddress
                task_name2.text = receiptName
                if (buyMobile.isEmpty()) {
                    task_name1.gone()
                    task_call1.gone()
                }
            }
            "抢单失败" -> {
                task_img.setImageResource(R.mipmap.pay_error)
                task_result.text = intent.getStringExtra("message")
                task_look.invisible()
                task_contact_ll.gone()
            }
        }
    }

    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.task_call1 -> {
                if (buyMobile.isEmpty()) {
                    showToast("电话号码为空")
                    return
                }

                DialogHelper.showHintDialog(baseContext,
                        "拨打电话",
                        "电话号码：$buyMobile，确定要拨打吗？ ",
                        "取消",
                        "确定",
                        true) {
                    if (it == "right") {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$buyMobile"))
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }
                }
            }
            R.id.task_call2 -> {
                if (receiptMobile.isEmpty()) {
                    showToast("电话号码为空")
                    return
                }

                DialogHelper.showHintDialog(baseContext,
                        "拨打电话",
                        "电话号码：$receiptMobile，确定要拨打吗？ ",
                        "取消",
                        "确定",
                        true) {
                    if (it == "right") {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$receiptMobile"))
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }
                }
            }
            R.id.task_look -> {
                if (!intent.getBooleanExtra("isDetail", false)) {
                    intent.setClass(baseContext, GrabDetailActivity::class.java)
                    startActivity(intent)
                }
                ActivityStack.screenManager.popActivities(this@TaskContactActivity::class.java)
            }
        }
    }
}
