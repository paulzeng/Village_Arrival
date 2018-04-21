package com.ruanmeng.village_arrival

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.RadioGroup
import com.ruanmeng.base.BaseActivity
import com.ruanmeng.base.gone
import com.ruanmeng.base.startActivity
import com.ruanmeng.model.LocationMessageEvent
import com.ruanmeng.utils.DialogHelper
import com.ruanmeng.utils.TimeHelper
import kotlinx.android.synthetic.main.activity_task.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class TaskActivity : BaseActivity() {

    private var buyLat = ""
    private var buyLng = ""
    private var buyAddress = "就近购买"
    private lateinit var deliveryTime: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)
        init_title(intent.getStringExtra("title"))

        EventBus.getDefault().register(this@TaskActivity)
    }

    override fun init_title() {
        super.init_title()

        deliveryTime = TimeHelper.getInstance().getNowTime("yyyy-MM-dd HH:mm")
        task_group.setOnCheckedChangeListener(this@TaskActivity)
        task_get_name.gone()
        task_put_name.gone()

        when (intent.getStringExtra("type")) {
            "buy" -> task_check1.isChecked = true
            "get" -> task_check2.isChecked = true
        }
    }

    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.bt_submit -> startActivity<IssuePayActivity>()
            R.id.task_get -> {
                val intent = Intent(baseContext, AddressAddActivity::class.java)
                intent.putExtra("title", if (task_check1.isChecked) "购买地址" else "取货地址")
                startActivity(intent)
            }
            R.id.task_put -> {
                val intent = Intent(baseContext, AddressAddActivity::class.java)
                intent.putExtra("title", "收货地址")
                startActivity(intent)
            }
            R.id.task_time_ll -> {
                DialogHelper.showDateDialog(baseContext) { hint, date ->
                    task_time.text = hint
                    deliveryTime = date
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
        when (checkedId) {
            R.id.task_check1 -> {
                task_name.hint = "请输入要买的商品：如香烟，酒水"
                task_get_hint.text = "购买店铺"
                task_get_addr.hint = "未填写则就近购买"
                task_get_addr.setHintTextColor(resources.getColor(R.color.orange))
                task_price_ll.visibility = View.VISIBLE
                task_open.visibility = View.GONE
            }
            R.id.task_check2 -> {
                task_name.hint = "请输入货物类型"
                task_get_hint.text = "取货地址"
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

    override fun finish() {
        EventBus.getDefault().unregister(this@TaskActivity)
        super.finish()
    }

    @Subscribe
    fun onMessageEvent(event: LocationMessageEvent) {
        when (event.type) {
            "购买地址", "取货地址" -> {}
            "收货地址" -> {}
        }
    }
}
