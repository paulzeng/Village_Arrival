package com.ruanmeng.village_arrival

import android.os.Bundle
import android.view.View
import com.ruanmeng.base.BaseActivity
import com.ruanmeng.base.load_Linear
import com.ruanmeng.base.refresh
import com.ruanmeng.model.CommonData
import kotlinx.android.synthetic.main.layout_empty.*
import kotlinx.android.synthetic.main.layout_list.*
import net.idik.lib.slimadapter.SlimAdapter
import java.util.ArrayList

class AccountDetailActivity : BaseActivity() {

    private val list = ArrayList<Any>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_detail)
        init_title("账户明细")

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
        empty_hint.text = "暂无相关明细信息！"
        swipe_refresh.refresh { getData(1) }
        recycle_list.load_Linear(baseContext, swipe_refresh) {
            if (!isLoadingMore) {
                isLoadingMore = true
                getData(pageNum)
            }
        }

        mAdapter = SlimAdapter.create()
                .register<CommonData>(R.layout.item_account_list) { data, injector ->
                    injector.visibility(R.id.item_account_divider1, if (list.indexOf(data) == list.size - 1) View.GONE else View.VISIBLE)
                            .visibility(R.id.item_account_divider2, if (list.indexOf(data) != list.size - 1) View.GONE else View.VISIBLE)
                            .visibility(R.id.item_account_divider3, if (list.indexOf(data) != 0) View.GONE else View.VISIBLE)

                            .clicked(R.id.item_account) { }
                }
                .attachTo(recycle_list)
    }
}
