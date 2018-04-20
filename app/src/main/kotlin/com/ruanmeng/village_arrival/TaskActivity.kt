package com.ruanmeng.village_arrival

import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.RadioGroup
import com.ruanmeng.base.BaseActivity
import com.ruanmeng.base.startActivity
import kotlinx.android.synthetic.main.activity_task.*

class TaskActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)
        init_title(intent.getStringExtra("title"))
    }

    override fun init_title() {
        super.init_title()
        task_group.setOnCheckedChangeListener(this@TaskActivity)
        task_get_name.visibility = View.GONE
        task_put_name.visibility = View.GONE

        when (intent.getStringExtra("type")) {
            "buy" -> task_check1.isChecked = true
            "get" -> task_check2.isChecked = true
        }
    }

    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.bt_submit -> startActivity<IssuePayActivity>()
            R.id.task_get -> startActivity<AddressAddActivity>()
            R.id.task_put -> startActivity<AddressAddActivity>()
        }
    }

    @Suppress("DEPRECATION")
    override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
        when (checkedId) {
            R.id.task_check1 -> {
                task_name.hint = "请输入要买的商品：如香烟，酒水"
                task_get_addr.hint = "未填写则就近购买"
                task_get_addr.setHintTextColor(resources.getColor(R.color.orange))
                task_price_ll.visibility = View.VISIBLE
                task_open.visibility = View.GONE
            }
            R.id.task_check2 -> {
                task_name.hint = "请输入货物类型"
                task_get_addr.hint = "请选择取货地址"
                task_get_addr.setHintTextColor(resources.getColor(R.color.colorControlNormal))
                task_price_ll.visibility = View.GONE
                task_open.visibility = View.VISIBLE
            }
        }

        task_name.setText("")
        task_get_addr.text = ""
        task_put_addr.text = ""
        task_time.text = "现在送货"
        val hint = "商品重量<small><font color='${resources.getColor(R.color.light)}'>（默认2公斤以内）</font></small>"
        @Suppress("DEPRECATION")
        task_weight.text = Html.fromHtml(hint)
        task_memo.setText("")
        task_price.setText("")
        task_open.check(-1)
    }
}
