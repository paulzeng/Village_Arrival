package com.ruanmeng.village_arrival

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.view.View
import android.widget.LinearLayout
import com.lzy.okgo.OkGo
import com.ruanmeng.base.BaseActivity
import com.ruanmeng.base.load_Linear
import com.ruanmeng.base.refresh
import com.ruanmeng.model.CommonData
import com.ruanmeng.utils.DensityUtil
import com.ruanmeng.utils.Tools
import kotlinx.android.synthetic.main.activity_grab.*
import kotlinx.android.synthetic.main.layout_empty.*
import kotlinx.android.synthetic.main.layout_list.*
import net.idik.lib.slimadapter.SlimAdapter

class GrabActivity : BaseActivity() {

    private val list = ArrayList<Any>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grab)
        init_title("我的抢单")

        list.add(CommonData("1"))
        list.add(CommonData("2"))
        list.add(CommonData("3"))
        list.add(CommonData("4"))
        list.add(CommonData("5"))
        list.add(CommonData("6"))
        list.add(CommonData("7"))
        list.add(CommonData("8"))
        mAdapter.updateData(list)

        empty_view.visibility = if (list.size > 0) View.GONE else View.VISIBLE
    }

    override fun init_title() {
        super.init_title()
        grab_tab.apply {
            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

                override fun onTabReselected(tab: TabLayout.Tab) {}
                override fun onTabUnselected(tab: TabLayout.Tab) {}

                override fun onTabSelected(tab: TabLayout.Tab) {
                    OkGo.getInstance().cancelTag(this@GrabActivity)
                    window.decorView.postDelayed({ runOnUiThread { /*updateList()*/ } }, 300)
                }

            })

            addTab(this.newTab().setText("全部订单"), true)
            addTab(this.newTab().setText("进行中"), false)
            addTab(this.newTab().setText("已完成"), false)

            post { Tools.setIndicator(this, 20, 20) }
        }

        empty_hint.text = "暂无相关抢单信息"
        empty_hint.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = DensityUtil.dp2px(0f) }
        empty_img.setImageResource(R.mipmap.ord_icon01)
        empty_img.layoutParams = LinearLayout.LayoutParams(
                DensityUtil.dp2px(120f),
                DensityUtil.dp2px(120f)).apply { topMargin = DensityUtil.dp2px(100f) }

        swipe_refresh.refresh { getData(1) }
        recycle_list.load_Linear(baseContext, swipe_refresh) {
            if (!isLoadingMore) {
                isLoadingMore = true
                getData(pageNum)
            }
        }

        mAdapter = SlimAdapter.create()
                .register<CommonData>(R.layout.item_grab_list) { data, injector ->
                    injector.visibility(R.id.item_grab_divider, if (list.indexOf(data) != 0) View.GONE else View.VISIBLE)

                            .clicked(R.id.item_grab) {
                                val intent = Intent(baseContext, GrabDetailActivity::class.java)
                                startActivity(intent)
                            }
                }
                .attachTo(recycle_list)
    }

    fun updateList() {
        swipe_refresh.isRefreshing = true

        empty_view.visibility = View.GONE
        if (list.isNotEmpty()) {
            list.clear()
            mAdapter.notifyDataSetChanged()
        }

        pageNum = 1
        getData(pageNum)
    }
}