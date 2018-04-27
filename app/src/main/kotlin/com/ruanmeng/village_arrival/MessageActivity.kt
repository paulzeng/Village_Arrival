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

class MessageActivity : BaseActivity() {

    private val list = ArrayList<Any>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)
        init_title("消息")

        swipe_refresh.isRefreshing = true
        getData(pageNum)
    }

    override fun init_title() {
        super.init_title()
        empty_hint.text = "暂无相关消息信息！"
        swipe_refresh.refresh { getData(1) }
        recycle_list.load_Linear(baseContext, swipe_refresh) {
            if (!isLoadingMore) {
                isLoadingMore = true
                getData(pageNum)
            }
        }

        mAdapter = SlimAdapter.create()
                .register<CommonData>(R.layout.item_msg_list) { data, injector ->
                    injector.text(R.id.item_msg_title, when (data.msgType) {
                        "0" -> "系统消息"
                        "1" -> "订单消息"
                        "2" -> "互助消息"
                        else -> "消息"
                    })
                            .image(R.id.item_msg_img, when (data.msgType) {
                                "1" -> R.mipmap.per_mes_icon02
                                "2" -> R.mipmap.per_mes_icon03
                                else -> R.mipmap.per_mes_icon01
                            })
                            .text(R.id.item_msg_content, data.content)
                            .visibility(R.id.item_msg_divider1, if (list.indexOf(data) == list.size - 1) View.GONE else View.VISIBLE)
                            .visibility(R.id.item_msg_divider2, if (list.indexOf(data) != list.size - 1) View.GONE else View.VISIBLE)
                            .visibility(R.id.item_msg_divider3, if (list.indexOf(data) != 0) View.GONE else View.VISIBLE)

                            .clicked(R.id.item_msg) {
                                when (data.msgType) {
                                    "0" -> {
                                        val intent = Intent(baseContext, WebActivity::class.java)
                                        intent.putExtra("title", "消息详情")
                                        intent.putExtra("content", data.content)
                                        intent.putExtra("sendDate", data.sendDate)
                                        startActivity(intent)
                                    }
                                    "1" -> {
                                        val intent = Intent(baseContext, IssueDetailActivity::class.java)
                                        intent.putExtra("goodsOrderId", data.msgId)
                                        startActivity(intent)
                                    }
                                    "2" -> {
                                        val intent = Intent(baseContext, LiveDetailActivity::class.java)
                                        intent.putExtra("cooperationId", data.msgId)
                                        startActivity(intent)
                                    }
                                }
                            }
                }
                .attachTo(recycle_list)
    }

    override fun getData(pindex: Int) {
        OkGo.post<BaseResponse<ArrayList<CommonData>>>(BaseHttp.msg_list_data)
                .tag(this@MessageActivity)
                .headers("token", getString("token"))
                .params("page", pindex)
                .execute(object : JacksonDialogCallback<BaseResponse<ArrayList<CommonData>>>(baseContext) {

                    override fun onSuccess(response: Response<BaseResponse<ArrayList<CommonData>>>) {

                        list.apply {
                            if (pindex == 1) {
                                clear()
                                pageNum = pindex
                            }
                            addItems(response.body().`object`)
                            if (count(response.body().`object`) > 0) pageNum++
                        }

                        if (count(response.body().`object`) > 0) mAdapter.updateData(list)
                    }

                    override fun onFinish() {
                        super.onFinish()
                        swipe_refresh.isRefreshing = false
                        isLoadingMore = false

                        empty_view.apply { if (list.isEmpty()) visible() else gone() }
                    }

                })
    }
}
