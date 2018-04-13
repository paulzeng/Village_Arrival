package com.ruanmeng.village_arrival

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.ruanmeng.base.BaseActivity
import com.ruanmeng.base.load_Linear
import com.ruanmeng.base.refresh
import com.ruanmeng.model.CommonData
import kotlinx.android.synthetic.main.layout_list.*
import net.idik.lib.slimadapter.SlimAdapter
import java.util.ArrayList

class ProblemActivity : BaseActivity() {

    private val list = ArrayList<Any>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_problem)
        init_title("常见问题")

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
        swipe_refresh.refresh { getData() }
        recycle_list.load_Linear(baseContext, swipe_refresh)

        mAdapter = SlimAdapter.create()
                .register<CommonData>(R.layout.item_problem_list) { data, injector ->
                    injector.visibility(R.id.item_problem_divider1, if (list.indexOf(data) == list.size - 1) View.GONE else View.VISIBLE)
                            .visibility(R.id.item_problem_divider2, if (list.indexOf(data) != list.size - 1) View.GONE else View.VISIBLE)
                            .visibility(R.id.item_problem_divider3, if (list.indexOf(data) != 0) View.GONE else View.VISIBLE)

                            .clicked(R.id.item_problem) {
                                val intent = Intent(baseContext, WebActivity::class.java)
                                intent.putExtra("title", "常见问题")
                                startActivity(intent)
                            }
                }
                .attachTo(recycle_list)
    }
}
