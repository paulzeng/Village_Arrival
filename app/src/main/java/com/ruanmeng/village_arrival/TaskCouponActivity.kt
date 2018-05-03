package com.ruanmeng.village_arrival

import android.os.Bundle
import android.view.View
import com.ruanmeng.base.BaseActivity
import com.ruanmeng.base.load_Linear
import com.ruanmeng.model.CommonData
import com.ruanmeng.model.LocationMessageEvent
import com.ruanmeng.utils.ActivityStack
import kotlinx.android.synthetic.main.activity_task_coupon.*
import net.idik.lib.slimadapter.SlimAdapter
import org.greenrobot.eventbus.EventBus
import java.util.ArrayList

class TaskCouponActivity : BaseActivity() {

    private lateinit var list: ArrayList<CommonData>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_coupon)
        init_title("优惠券")

        @Suppress("UNCHECKED_CAST")
        list = intent.getSerializableExtra("list") as ArrayList<CommonData>
        mAdapter.updateData(list)
    }

    override fun init_title() {
        super.init_title()
        coupon_list.load_Linear(baseContext)

        mAdapter = SlimAdapter.create()
                .register<CommonData>(R.layout.item_coupon_list) { data, injector ->
                    injector.text(R.id.item_coupon_money, "￥${data.voucherSum}")
                            .text(R.id.item_coupon_time, "有效期至：${data.endTime}")
                            .visibility(R.id.item_coupon_divider, if (list.indexOf(data) != 0) View.GONE else View.VISIBLE)

                            .clicked(R.id.item_coupon) {
                                EventBus.getDefault().post(LocationMessageEvent("优惠券", data.voucherId, data.voucherSum))
                                ActivityStack.screenManager.popActivities(this@TaskCouponActivity::class.java)
                            }
                }
                .attachTo(coupon_list)
    }
}
