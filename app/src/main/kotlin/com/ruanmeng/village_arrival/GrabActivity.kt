package com.ruanmeng.village_arrival

import android.content.Intent
import android.net.Uri
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
import com.ruanmeng.utils.*
import kotlinx.android.synthetic.main.activity_grab.*
import kotlinx.android.synthetic.main.layout_empty.*
import kotlinx.android.synthetic.main.layout_list.*
import net.idik.lib.slimadapter.SlimAdapter
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class GrabActivity : BaseActivity() {

    private val list = ArrayList<Any>()
    private var mStatus = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grab)
        init_title("我的抢单")

        EventBus.getDefault().register(this@GrabActivity)
    }

    override fun init_title() {
        super.init_title()
        grab_tab.apply {
            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

                override fun onTabReselected(tab: TabLayout.Tab) {}
                override fun onTabUnselected(tab: TabLayout.Tab) {}

                override fun onTabSelected(tab: TabLayout.Tab) {
                    mStatus = when (tab.position) {
                        1 -> "2"
                        2 -> "3"
                        else -> ""
                    }
                    OkGo.getInstance().cancelTag(this@GrabActivity)
                    window.decorView.postDelayed({ runOnUiThread { updateList() } }, 300)
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
                    injector.text(R.id.item_grab_time, when (data.status) {
                        "-2" -> "取消时间：${TimeHelper.getDiffTime(TimeHelper.getInstance().millisecondToLong(data.cancelTime))}"
                        "1" -> "等待时间：${TimeHelper.getDiffTime(TimeHelper.getInstance().millisecondToLong(data.payTime))}"
                        "-1", "2" -> "已接单：${TimeHelper.getDiffTimeAfter(TimeHelper.getInstance().millisecondToLong(data.grabsingleTime))}"
                        "3" -> "完成时间：${TimeHelper.getDiffTime(TimeHelper.getInstance().millisecondToLong(data.arriveTime))}"
                        else -> "下单时间：${TimeHelper.getDiffTime(TimeHelper.getInstance().millisecondToLong(data.createDate))}"
                    })
                            .text(R.id.item_grab_status, when (data.status) {
                                "-3" -> "已删除"
                                "-2" -> "已取消"
                                "-1" -> "取消中"
                                "0" -> "待支付"
                                "1" -> "待抢单"
                                "2" -> "进行中"
                                "3" -> "已完成"
                                else -> ""
                            })
                            .text(R.id.item_grab_name, when (data.type) {
                                "1" -> "顺风商品：${data.goods}"
                                else -> "代买商品：${data.goods}"
                            })
                            .image(R.id.item_grab_img1, if (data.type == "1") R.mipmap.index_lab05 else R.mipmap.index_lab01)
                            .text(R.id.item_grab_addr1, data.buyAddress + data.buyDetailAdress)
                            .text(R.id.item_grab_addr2, data.receiptAddress + data.receiptDetailAdress)
                            .text(R.id.item_grab_name1, "${data.buyname}  ${data.buyMobile}")
                            .text(R.id.item_grab_name2, "${data.receiptName}  ${data.receiptMobile}")
                            .text(R.id.item_grab_yong, data.commission)
                            .text(R.id.item_grab_yu, "（商品预估：${data.goodsPrice}元）")

                            .visibility(R.id.item_grab_name1, if (data.buyMobile.isEmpty()) View.GONE else View.VISIBLE)
                            .visibility(R.id.item_grab_call1, if (data.buyMobile.isEmpty()) View.GONE else View.VISIBLE)
                            .visibility(R.id.item_grab_yu, if (data.goodsPrice.isEmpty()) View.GONE else View.VISIBLE)
                            .visibility(R.id.item_grab_divider, if (list.indexOf(data) != 0) View.GONE else View.VISIBLE)

                            .clicked(R.id.item_grab_call1) { _ ->
                                if (data.buyMobile.isEmpty()) {
                                    showToast("电话号码为空")
                                    return@clicked
                                }

                                DialogHelper.showHintDialog(baseContext,
                                        "拨打电话",
                                        "电话号码：${data.buyMobile}，确定要拨打吗？ ",
                                        "取消",
                                        "确定",
                                        true) {
                                    if (it == "right") {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + data.buyMobile))
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        startActivity(intent)
                                    }
                                }
                            }

                            .clicked(R.id.item_grab_call2) { _ ->
                                if (data.receiptMobile.isEmpty()) {
                                    showToast("电话号码为空")
                                    return@clicked
                                }

                                DialogHelper.showHintDialog(baseContext,
                                        "拨打电话",
                                        "电话号码：${data.receiptMobile}，确定要拨打吗？ ",
                                        "取消",
                                        "确定",
                                        true) {
                                    if (it == "right") {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + data.receiptMobile))
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        startActivity(intent)
                                    }
                                }
                            }

                            .clicked(R.id.item_grab) {
                                val intent = Intent(baseContext, GrabDetailActivity::class.java)
                                intent.putExtra("goodsOrderId", data.goodsOrderId)
                                startActivity(intent)
                            }
                }
                .attachTo(recycle_list)
    }

    override fun getData(pindex: Int) {
        OkGo.post<BaseResponse<ArrayList<CommonData>>>(BaseHttp.grabsingle_order_list)
                .tag(this@GrabActivity)
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
        EventBus.getDefault().unregister(this@GrabActivity)
        super.finish()
    }

    @Subscribe
    fun onMessageEvent(event: RefreshMessageEvent) {
        when (event.type) {
            "完成订单", "取消抢单", "骑手取消", "骑手同意", "骑手推送" -> updateList()
        }
    }
}
