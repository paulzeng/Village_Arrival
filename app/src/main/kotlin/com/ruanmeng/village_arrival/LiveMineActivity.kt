package com.ruanmeng.village_arrival

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.view.View
import com.lzg.extend.StringDialogCallback
import com.lzg.extend.jackson.JacksonDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.ruanmeng.base.*
import com.ruanmeng.model.CommonData
import com.ruanmeng.model.CommonModel
import com.ruanmeng.model.RefreshMessageEvent
import com.ruanmeng.share.BaseHttp
import com.ruanmeng.utils.DialogHelper
import com.ruanmeng.utils.TimeHelper
import com.ruanmeng.utils.Tools
import kotlinx.android.synthetic.main.activity_live_mine.*
import kotlinx.android.synthetic.main.layout_empty.*
import kotlinx.android.synthetic.main.layout_list.*
import net.idik.lib.slimadapter.SlimAdapter
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class LiveMineActivity : BaseActivity() {

    private val list = ArrayList<Any>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_mine)
        init_title("生活互助")

        EventBus.getDefault().register(this@LiveMineActivity)
    }

    override fun init_title() {
        super.init_title()
        live_tab.apply {
            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

                override fun onTabReselected(tab: TabLayout.Tab) {}
                override fun onTabUnselected(tab: TabLayout.Tab) {}

                override fun onTabSelected(tab: TabLayout.Tab) {
                    mPosition = tab.position
                    OkGo.getInstance().cancelTag(this@LiveMineActivity)
                    window.decorView.postDelayed({ runOnUiThread { updateList() } }, 300)
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
                    injector.text(R.id.item_live_title, data.title)
                            .text(R.id.item_live_area, data.district)
                            .text(R.id.item_live_time, TimeHelper.getDiffTime(TimeHelper.getInstance().millisecondToLong(data.createDate)))
                            .text(R.id.item_live_comment, data.commentCtn)

                            .visibility(R.id.item_live_del, if (mPosition == 1) View.GONE else View.VISIBLE)
                            .visibility(R.id.item_live_cancel, if (mPosition == 0) View.GONE else View.VISIBLE)
                            .visibility(R.id.item_live_divider1, if (list.indexOf(data) == list.size - 1) View.GONE else View.VISIBLE)
                            .visibility(R.id.item_live_divider2, if (list.indexOf(data) != list.size - 1) View.GONE else View.VISIBLE)
                            .visibility(R.id.item_live_divider3, if (list.indexOf(data) != 0) View.GONE else View.VISIBLE)

                            .clicked(R.id.item_live_del) {

                                DialogHelper.showHintDialog(baseContext,
                                        "删除发布",
                                        getString(R.string.del_live),
                                        "取消",
                                        "确定",
                                        false) {
                                    if (it == "right") {

                                        OkGo.post<String>(BaseHttp.delete_cooperation)
                                                .tag(this@LiveMineActivity)
                                                .headers("token", getString("token"))
                                                .params("cooperationId", data.cooperationId)
                                                .execute(object : StringDialogCallback(baseContext) {

                                                    override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                                                        showToast(msg)
                                                        val position = list.indexOf(data)
                                                        list.remove(data)
                                                        mAdapter.notifyItemRemoved(position)
                                                        empty_view.apply { if (list.isEmpty()) visible() else gone() }
                                                    }

                                                })
                                    }
                                }
                            }

                            .clicked(R.id.item_live_cancel) {

                                OkGo.post<String>(BaseHttp.delete_collect_cooperatio)
                                        .tag(this@LiveMineActivity)
                                        .headers("token", getString("token"))
                                        .params("cooperationId", data.cooperationId)
                                        .execute(object : StringDialogCallback(baseContext) {

                                            override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                                                showToast(msg)
                                                val position = list.indexOf(data)
                                                list.remove(data)
                                                mAdapter.notifyItemRemoved(position)
                                                empty_view.apply { if (list.isEmpty()) visible() else gone() }
                                            }

                                        })
                            }

                            .clicked(R.id.item_live) {
                                val intent = Intent(baseContext, LiveDetailActivity::class.java)
                                intent.putExtra("cooperationId", data.cooperationId)
                                startActivity(intent)
                            }
                }
                .attachTo(recycle_list)
    }

    override fun getData(pindex: Int) {
        OkGo.post<CommonModel>(if (mPosition == 0) BaseHttp.mycooperation_list else BaseHttp.cooperation_collect_list)
                .tag(this@LiveMineActivity)
                .headers("token", getString("token"))
                .params("page", pindex)
                .execute(object : JacksonDialogCallback<CommonModel>(baseContext, CommonModel::class.java) {

                    override fun onSuccess(response: Response<CommonModel>) {

                        list.apply {
                            if (pindex == 1) {
                                clear()
                                pageNum = pindex
                            }
                            addItems(response.body().cooperationList)
                            if (count(response.body().cooperationList) > 0) pageNum++
                        }

                        mAdapter.updateData(list)
                    }

                    override fun onFinish() {
                        super.onFinish()
                        swipe_refresh.isRefreshing = false
                        isLoadingMore = false

                        empty_view.apply { if (list.isEmpty()) visible() else gone() }
                    }

                })
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

    override fun finish() {
        EventBus.getDefault().unregister(this@LiveMineActivity)
        super.finish()
    }

    @Subscribe
    fun onMessageEvent(event: RefreshMessageEvent) {
        when (event.type) {
            "添加评论" -> updateList()
            "收藏成功", "取消收藏" -> if (mPosition == 1) updateList()
        }
    }
}
