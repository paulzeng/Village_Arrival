package com.ruanmeng.village_arrival

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.view.View
import android.widget.LinearLayout
import com.lzg.extend.BaseResponse
import com.lzg.extend.jackson.JacksonDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.ruanmeng.base.*
import com.ruanmeng.model.CommonData
import com.ruanmeng.model.RefreshMessageEvent
import com.ruanmeng.share.BaseHttp
import com.ruanmeng.utils.DensityUtil
import com.ruanmeng.utils.TimeHelper
import com.ruanmeng.utils.phoneReplaceWithStar
import kotlinx.android.synthetic.main.activity_issue.*
import kotlinx.android.synthetic.main.layout_empty.*
import kotlinx.android.synthetic.main.layout_list.*
import net.idik.lib.slimadapter.SlimAdapter
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class IssueActivity : BaseActivity() {

    private val list = ArrayList<Any>()
    private var mStatus = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_issue)
        init_title("我的发布")

        EventBus.getDefault().register(this@IssueActivity)
    }

    override fun init_title() {
        super.init_title()
        issue_tab.apply {
            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

                override fun onTabReselected(tab: TabLayout.Tab) {}
                override fun onTabUnselected(tab: TabLayout.Tab) {}

                override fun onTabSelected(tab: TabLayout.Tab) {
                    mStatus = when (tab.position) {
                        1 -> "0"
                        2 -> "1"
                        3 -> "2"
                        4 -> "3"
                        else -> ""
                    }
                    OkGo.getInstance().cancelTag(this@IssueActivity)
                    window.decorView.postDelayed({ runOnUiThread { updateList() } }, 300)
                }

            })

            addTab(this.newTab().setText("全部订单"), true)
            addTab(this.newTab().setText("待付款"), false)
            addTab(this.newTab().setText("待抢单"), false)
            addTab(this.newTab().setText("进行中"), false)
            addTab(this.newTab().setText("已完成"), false)
        }

        empty_hint.text = "暂无相关发布信息"
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
                .register<CommonData>(R.layout.item_issue_list) { data, injector ->
                    injector.text(R.id.item_issue_time, when (data.status) {
                        "-2" -> "取消时间：${TimeHelper.getDiffTime(TimeHelper.getInstance().millisecondToLong(data.cancelTime))}"
                        "1" -> "等待时间：${TimeHelper.getDiffTime(TimeHelper.getInstance().millisecondToLong(data.payTime))}"
                        "-1", "2" -> "抢单时间：${TimeHelper.getDiffTime(TimeHelper.getInstance().millisecondToLong(data.grabsingleTime))}"
                        "3" -> "完成时间：${TimeHelper.getDiffTime(TimeHelper.getInstance().millisecondToLong(data.arriveTime))}"
                        else -> "下单时间：${TimeHelper.getDiffTime(TimeHelper.getInstance().millisecondToLong(data.createDate))}"
                    })
                            .text(R.id.item_issue_status, when (data.status) {
                                "-3" -> "已删除"
                                "-2" -> "已取消"
                                "-1" -> "取消中"
                                "0" -> "待支付"
                                "1" -> "待抢单"
                                "2" -> "进行中"
                                "3" -> "已完成"
                                else -> ""
                            })
                            .text(R.id.item_issue_name, when (data.type) {
                                "1" -> "顺风商品：${data.goods}"
                                else -> "代买商品：${data.goods}"
                            })
                            .image(R.id.item_issue_img1, if (data.type == "1") R.mipmap.index_lab05 else R.mipmap.index_lab01)
                            .text(R.id.item_issue_addr1, data.buyAddress + data.buyDetailAdress)
                            .text(R.id.item_issue_addr2, data.receiptAddress + data.receiptDetailAdress)
                            .text(R.id.item_issue_name1, "${data.buyname}  ${data.buyMobile.phoneReplaceWithStar()}")
                            .text(R.id.item_issue_name2, "${data.receiptName}  ${data.receiptMobile.phoneReplaceWithStar()}")
                            .text(R.id.item_issue_yong, data.commission)
                            .text(R.id.item_issue_yu, "（商品预估：${data.goodsPrice}元）")

                            .visibility(R.id.item_issue_name1, if (data.buyMobile.isEmpty()) View.GONE else View.VISIBLE)
                            .visibility(R.id.item_issue_yu, if (data.goodsPrice.isEmpty()) View.GONE else View.VISIBLE)
                            .visibility(R.id.item_issue_pay, if (data.status == "0") View.VISIBLE else View.GONE)
                            .visibility(R.id.item_issue_divider, if (list.indexOf(data) != 0) View.GONE else View.VISIBLE)

                            .clicked(R.id.item_issue_pay) {
                                val intent = Intent(baseContext, IssuePayActivity::class.java)
                                intent.putExtra("hint", "发布列表")
                                intent.putExtra("goodsOrderId", data.goodsOrderId)
                                intent.putExtra("commission", data.commission)
                                startActivity(intent)
                            }

                            .clicked(R.id.item_issue) {
                                val intent = Intent(baseContext, IssueDetailActivity::class.java)
                                intent.putExtra("goodsOrderId", data.goodsOrderId)
                                startActivity(intent)
                            }
                }
                .attachTo(recycle_list)
    }

    override fun getData(pindex: Int) {
        OkGo.post<BaseResponse<ArrayList<CommonData>>>(BaseHttp.my_order_list)
                .tag(this@IssueActivity)
                .headers("token", getString("token"))
                .params("status", mStatus)
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

    private fun updateList() {
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
        EventBus.getDefault().unregister(this@IssueActivity)
        super.finish()
    }

    @Subscribe
    fun onMessageEvent(event: RefreshMessageEvent) {
        when (event.type) {
            "支付成功", "客户取消", "客户同意" -> updateList()
        }
    }
}
