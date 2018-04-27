package com.ruanmeng.village_arrival

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.lzg.extend.BaseResponse
import com.lzg.extend.jackson.JacksonDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.ruanmeng.base.*
import com.ruanmeng.model.CommonData
import com.ruanmeng.share.BaseHttp
import kotlinx.android.synthetic.main.layout_empty.*
import kotlinx.android.synthetic.main.layout_list.*
import net.idik.lib.slimadapter.SlimAdapter
import java.util.ArrayList

class ProblemActivity : BaseActivity() {

    private val list = ArrayList<Any>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_problem)
        init_title("常见问题")

        swipe_refresh.isRefreshing = true
        getData()
    }

    override fun init_title() {
        super.init_title()
        swipe_refresh.refresh { getData() }
        recycle_list.load_Linear(baseContext, swipe_refresh)

        mAdapter = SlimAdapter.create()
                .register<CommonData>(R.layout.item_problem_list) { data, injector ->
                    injector.text(R.id.item_problem_name, data.title)
                            .visibility(R.id.item_problem_divider1, if (list.indexOf(data) == list.size - 1) View.GONE else View.VISIBLE)
                            .visibility(R.id.item_problem_divider2, if (list.indexOf(data) != list.size - 1) View.GONE else View.VISIBLE)
                            .visibility(R.id.item_problem_divider3, if (list.indexOf(data) != 0) View.GONE else View.VISIBLE)

                            .clicked(R.id.item_problem) {
                                val intent = Intent(baseContext, WebActivity::class.java)
                                intent.putExtra("title", "常见问题")
                                intent.putExtra("content", data.content)
                                startActivity(intent)
                            }
                }
                .attachTo(recycle_list)
    }

    override fun getData() {
        OkGo.post<BaseResponse<ArrayList<CommonData>>>(BaseHttp.common_problem)
                .tag(this@ProblemActivity)
                .headers("token", getString("token"))
                .params("type", 2)
                .execute(object : JacksonDialogCallback<BaseResponse<ArrayList<CommonData>>>(baseContext) {

                    override fun onSuccess(response: Response<BaseResponse<ArrayList<CommonData>>>) {

                        list.apply {
                                clear()
                            addItems(response.body().`object`)
                        }

                        mAdapter.updateData(list)
                    }

                    override fun onFinish() {
                        super.onFinish()
                        swipe_refresh.isRefreshing = false
                    }

                })
    }
}
