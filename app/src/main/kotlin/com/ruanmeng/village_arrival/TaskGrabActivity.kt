package com.ruanmeng.village_arrival

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.lzg.extend.BaseResponse
import com.lzg.extend.StringDialogCallback
import com.lzg.extend.jackson.JacksonDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.ruanmeng.base.*
import com.ruanmeng.model.CommonData
import com.ruanmeng.share.BaseHttp
import com.ruanmeng.utils.DensityUtil
import com.ruanmeng.utils.DialogHelper
import com.ruanmeng.utils.TimeHelper
import com.ruanmeng.utils.startRotateAnimator
import com.ruanmeng.view.DropPopWindow
import kotlinx.android.synthetic.main.activity_task_grab.*
import kotlinx.android.synthetic.main.layout_empty.*
import kotlinx.android.synthetic.main.layout_list.*
import kotlinx.android.synthetic.main.layout_title_task.*
import net.idik.lib.slimadapter.SlimAdapter

class TaskGrabActivity : BaseActivity() {

    private val list = ArrayList<Any>()
    private val listArea = ArrayList<CommonData>()
    private var dropPopWindowArea: DropPopWindow? = null
    private var mType = ""
    private var mTownship = ""
    private var mDeliveryTime = "1"
    private var mCommission = "1"
    private var mUrgent = "1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_grab)
        setToolbarVisibility(false)
        init_title()

        swipe_refresh.isRefreshing = true
        getData(pageNum)
    }

    override fun init_title() {
        super.init_title()
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
                .register<CommonData>(R.layout.item_task_list) { data, injector ->
                    injector.text(R.id.item_task_time, TimeHelper.getDiffTime(TimeHelper.getInstance().millisecondToLong(data.payTime)))
                            .text(R.id.item_task_name, when (data.type) {
                                "1" -> "顺风商品：${data.goods}"
                                else -> "代买商品：${data.goods}"
                            })
                            .with<TextView>(R.id.item_task_status) {
                                when (data.type) {
                                    "0" -> {
                                        it.text = "代买订单"
                                        @Suppress("DEPRECATION")
                                        it.setTextColor(resources.getColor(R.color.red))
                                    }
                                    "1" -> {
                                        it.text = "顺风订单"
                                        @Suppress("DEPRECATION")
                                        it.setTextColor(resources.getColor(R.color.colorAccent))
                                    }
                                }
                            }
                            .with<TextView>(R.id.item_task_grab) {
                                when (data.type) {
                                    "0" -> {
                                        it.text = "代买抢单"
                                        @Suppress("DEPRECATION")
                                        it.background = resources.getDrawable(R.drawable.rec_bg_red_shade)
                                    }
                                    "1" -> {
                                        it.text = "顺风抢单"
                                        @Suppress("DEPRECATION")
                                        it.background = resources.getDrawable(R.drawable.rec_bg_blue_shade)
                                    }
                                }
                            }
                            .image(R.id.item_task_img1, if (data.type == "1") R.mipmap.index_lab05 else R.mipmap.index_lab01)
                            .text(R.id.item_task_addr1, data.buyAddress + data.buyDetailAdress)
                            .text(R.id.item_task_addr2, data.receiptAddress + data.receiptDetailAdress)
                            .text(R.id.item_task_yong, data.commission)
                            .text(R.id.item_task_yu, data.goodsPrice)

                            .visibility(R.id.item_task_gu, if (data.goodsPrice.isEmpty()) View.GONE else View.VISIBLE)
                            .visibility(R.id.item_task_divider, if (list.indexOf(data) != 0) View.GONE else View.VISIBLE)

                            .clicked(R.id.item_task_grab) {
                                if (getString("status") != "1") {
                                    showToast("未通过抢单审核，无法进行抢单")
                                    return@clicked
                                }

                                OkGo.post<String>(BaseHttp.grab_order)
                                        .tag(this@TaskGrabActivity)
                                        .headers("token", getString("token"))
                                        .params("goodsOrderId", data.goodsOrderId)
                                        .execute(object : StringDialogCallback(baseContext) {

                                            override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                                                showToast(msg)
                                                list.remove(data)
                                                mAdapter.notifyDataSetChanged()
                                            }

                                        })
                            }

                            .clicked(R.id.item_task) {
                                val intent = Intent(baseContext, GrabDetailActivity::class.java)
                                intent.putExtra("isAll", true)
                                intent.putExtra("goodsOrderId", data.goodsOrderId)
                                startActivity(intent)
                            }
                }
                .attachTo(recycle_list)
    }

    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.task_default -> {
                if (mDeliveryTime == "0") task_time_arrow.startRotateAnimator(180f, 0f)
                if (mCommission == "0") task_yong_arrow.startRotateAnimator(180f, 0f)
                if (mUrgent == "0") task_hurry_arrow.startRotateAnimator(180f, 0f)

                mTownship = ""
                mDeliveryTime = "1"
                mCommission = "1"
                mUrgent = "1"

                task_qu.text = "区域"
                listArea.filter { it.isChecked }.forEach { it.isChecked = false }

                updateList()
            }
            R.id.task_qu_ll -> {
                if (dropPopWindowArea == null || listArea.isEmpty()) {
                    OkGo.post<BaseResponse<ArrayList<CommonData>>>(BaseHttp.area_street2)
                            .tag(this@TaskGrabActivity)
                            .isMultipart(true)
                            .params("areaName", intent.getStringExtra("district"))
                            .execute(object : JacksonDialogCallback<BaseResponse<ArrayList<CommonData>>>(baseContext, true) {

                                @Suppress("DEPRECATION")
                                override fun onSuccess(response: Response<BaseResponse<ArrayList<CommonData>>>) {
                                    listArea.apply {
                                        clear()
                                        addItems(response.body().`object`)
                                    }

                                    if (listArea.isNotEmpty()) showDropArea()
                                }

                            })
                } else {
                    if (dropPopWindowArea!!.isShowing) dropPopWindowArea!!.dismiss()
                    else showDropArea()
                }
            }
            R.id.task_time_ll -> {
                if (mDeliveryTime == "1") {
                    mDeliveryTime = "0"
                    task_time_arrow.startRotateAnimator(0f, 180f)
                } else {
                    mDeliveryTime = "1"
                    task_time_arrow.startRotateAnimator(180f, 0f)
                }

                window.decorView.postDelayed({ runOnUiThread { updateList() } }, 350)
            }
            R.id.task_yong_ll -> {
                if (mCommission == "1") {
                    mCommission = "0"
                    task_yong_arrow.startRotateAnimator(0f, 180f)
                } else {
                    mCommission = "1"
                    task_yong_arrow.startRotateAnimator(180f, 0f)
                }

                window.decorView.postDelayed({ runOnUiThread { updateList() } }, 350)
            }
            R.id.task_hurry_ll -> {
                if (mUrgent == "1") {
                    mUrgent = "0"
                    task_hurry_arrow.startRotateAnimator(0f, 180f)
                } else {
                    mUrgent = "1"
                    task_hurry_arrow.startRotateAnimator(180f, 0f)
                }

                window.decorView.postDelayed({ runOnUiThread { updateList() } }, 350)
            }
            R.id.task_title -> {
                DialogHelper.showDropWindow(
                        baseContext,
                        R.layout.pop_layout_area,
                        task_arrow,
                        task_divider,
                        listOf("全部任务", "代买任务", "顺风任务")) { position, name ->
                    mType = when (position) {
                        1 -> "0"
                        2 -> "1"
                        else -> ""
                    }
                    task_title.text = name
                    window.decorView.postDelayed({ runOnUiThread { updateList() } }, 350)
                }
            }
        }
    }

    private fun showDropArea() {
        dropPopWindowArea = object : DropPopWindow(
                baseContext,
                R.layout.pop_layout_area,
                task_qu_arrow) {

            override fun afterInitView(view: View) {
                val recyclerView = view.findViewById<RecyclerView>(R.id.pop_container)
                recyclerView.apply {
                    load_Linear(baseContext)
                    adapter = SlimAdapter.create()
                            .register<CommonData>(R.layout.item_area_list) { data, injector ->
                                injector.text(R.id.item_area, data.areaName)
                                        .textColor(R.id.item_area,
                                                resources.getColor(if (data.isChecked) R.color.colorAccent else R.color.black_dark))

                                        .visibility(R.id.item_area_divider1,
                                                if (listArea.indexOf(data) == listArea.size - 1) View.GONE else View.VISIBLE)
                                        .visibility(R.id.item_area_divider2,
                                                if (listArea.indexOf(data) != listArea.size - 1) View.GONE else View.VISIBLE)

                                        .clicked(R.id.item_area) {
                                            listArea.filter { it.isChecked }.forEach { it.isChecked = false }
                                            data.isChecked = true
                                            task_qu.text = data.areaName

                                            if (mTownship != data.areaName) {
                                                mTownship = data.areaName
                                                (adapter as SlimAdapter).notifyDataSetChanged()
                                                window.decorView.postDelayed({ runOnUiThread { updateList() } }, 350)
                                            }

                                            dismiss()
                                        }
                            }
                            .attachTo(this)

                    (adapter as SlimAdapter).updateData(listArea)
                }

            }
        }
        dropPopWindowArea!!.showAsDropDown(task_divider2)
    }

    override fun getData(pindex: Int) {
        OkGo.post<BaseResponse<ArrayList<CommonData>>>(BaseHttp.order_list_data)
                .tag(this@TaskGrabActivity)
                .isMultipart(true)
                .headers("token", getString("token"))
                .params("lat", intent.getStringExtra("lat"))
                .params("lng", intent.getStringExtra("lng"))
                .params("city", intent.getStringExtra("city"))
                .params("district", intent.getStringExtra("district"))
                .params("type", mType)
                .params("township", mTownship)
                .params("deliveryTime", mDeliveryTime)
                .params("commission", mCommission)
                .params("urgent", mUrgent)
                .params("status", 1)
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
