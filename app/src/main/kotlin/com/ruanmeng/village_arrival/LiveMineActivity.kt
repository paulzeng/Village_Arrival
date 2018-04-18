package com.ruanmeng.village_arrival

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.view.View
import com.lzy.okgo.OkGo
import com.ruanmeng.base.BaseActivity
import com.ruanmeng.base.load_Linear
import com.ruanmeng.base.refresh
import com.ruanmeng.model.CommonData
import com.ruanmeng.utils.Tools
import kotlinx.android.synthetic.main.activity_live_mine.*
import kotlinx.android.synthetic.main.layout_empty.*
import kotlinx.android.synthetic.main.layout_list.*
import net.idik.lib.slimadapter.SlimAdapter

class LiveMineActivity : BaseActivity() {

    private val list = ArrayList<Any>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_mine)
        init_title("生活互助")

        list.add(CommonData("1"))
        list.add(CommonData("2"))
        list.add(CommonData("3"))
        list.add(CommonData("4"))
        list.add(CommonData("5"))
        list.add(CommonData("6"))
        list.add(CommonData("7"))
        list.add(CommonData("8"))
        mAdapter.updateData(list)
    }

    override fun init_title() {
        super.init_title()
        live_tab.apply {
            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

                override fun onTabReselected(tab: TabLayout.Tab) {}
                override fun onTabUnselected(tab: TabLayout.Tab) {}

                override fun onTabSelected(tab: TabLayout.Tab) {
                    OkGo.getInstance().cancelTag(this@LiveMineActivity)
                    window.decorView.postDelayed({ runOnUiThread { /*updateList()*/ } }, 300)
                }

            })

            addTab(this.newTab().setText("我的发布"), true)
            addTab(this.newTab().setText("我的收藏"), false)

            post { Tools.setIndicator(this, 40, 40) }
        }

        empty_hint.text = "暂无相关生活互助信息"

        swipe_refresh.refresh { getData(1) }
        recycle_list.load_Linear(baseContext, swipe_refresh) {
            if (!isLoadingMore) {
                isLoadingMore = true
                getData(pageNum)
            }
        }

        mAdapter = SlimAdapter.create()
                .register<CommonData>(R.layout.item_live_list) { data, injector ->
                    injector.visibility(R.id.item_live_divider1, if (list.indexOf(data) == list.size - 1) View.GONE else View.VISIBLE)
                            .visibility(R.id.item_live_divider2, if (list.indexOf(data) != list.size - 1) View.GONE else View.VISIBLE)
                            .visibility(R.id.item_live_divider3, if (list.indexOf(data) != 0) View.GONE else View.VISIBLE)

                            .clicked(R.id.item_live) {
                                val intent = Intent(baseContext, LiveDetailActivity::class.java)
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
