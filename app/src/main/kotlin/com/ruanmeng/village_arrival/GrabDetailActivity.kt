package com.ruanmeng.village_arrival

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AlertDialog
import android.text.Html
import android.view.View
import com.lzg.extend.BaseResponse
import com.lzg.extend.StringDialogCallback
import com.lzg.extend.jackson.JacksonDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.ruanmeng.base.*
import com.ruanmeng.model.CommonData
import com.ruanmeng.model.RefreshMessageEvent
import com.ruanmeng.share.BaseHttp
import com.ruanmeng.utils.DialogHelper
import com.ruanmeng.utils.TimeHelper
import kotlinx.android.synthetic.main.activity_grab_detail.*
import org.greenrobot.eventbus.EventBus
import java.text.DecimalFormat

class GrabDetailActivity : BaseActivity() {

    private var mStauts = ""
    private var mType = ""
    private var buyMobile = ""
    private var receiptMobile = ""

    private var agreeCancel = "0.0"
    private var unAgreeCancel = "0.0"
    private var grabsingleTime = ""

    @SuppressLint("HandlerLeak")
    private var handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            getData()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grab_detail)
        init_title("订单详情")

        handler.sendEmptyMessage(0)
    }

    override fun init_title() {
        super.init_title()
        @Suppress("DEPRECATION")
        tvRight.setTextColor(resources.getColor(R.color.light))
        bt_done.gone()
    }

    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.bt_done -> {
                when (bt_done.text.toString()) {
                    "代买抢单", "顺风抢单" -> {
                        when (getString("status")) {
                            "-1" -> showToast("您的抢单申请正在审核中，请耐心等待!")
                            "1" -> {
                                OkGo.post<String>(BaseHttp.grab_order)
                                        .tag(this@GrabDetailActivity)
                                        .headers("token", getString("token"))
                                        .params("goodsOrderId", intent.getStringExtra("goodsOrderId"))
                                        .execute(object : StringDialogCallback(baseContext) {

                                            override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {
                                                handler.sendEmptyMessage(0)
                                                EventBus.getDefault().post(RefreshMessageEvent("抢单成功"))

                                                intent.setClass(baseContext, TaskContactActivity::class.java)
                                                intent.putExtra("title", "抢单成功")
                                                intent.putExtra("isDetail", true)
                                                intent.putExtra("type", mType)
                                                intent.putExtra("buyAddress", grab_addr1.text.toString())
                                                intent.putExtra("buyName", grab_name1.text.toString())
                                                intent.putExtra("receiptAddress", grab_addr2.text.toString())
                                                intent.putExtra("receiptName", grab_name2.text.toString())
                                                intent.putExtra("buyMobile", grab_name2.text.toString())
                                                intent.putExtra("receiptMobile", grab_name2.text.toString())
                                                startActivity(intent)
                                            }

                                            override fun onSuccessResponseErrorCode(response: Response<String>, msg: String, msgCode: String) {
                                                intent.setClass(baseContext, TaskContactActivity::class.java)
                                                intent.putExtra("title", "抢单失败")
                                                intent.putExtra("message", msg)
                                                startActivity(intent)
                                            }

                                        })
                            }
                            else -> {
                                AlertDialog.Builder(baseContext)
                                        .setTitle("温馨提示")
                                        .setMessage("您还未通过抢单审核，是否现在去申请抢单？")
                                        .setPositiveButton("去申请") { _, _ -> startActivity<ApplyActivity>() }
                                        .setNegativeButton("取消") { _, _ -> }
                                        .create()
                                        .show()
                                return
                            }
                        }
                    }
                    "我已完成" -> {
                        DialogHelper.showDoneDialog(baseContext, false) {

                            OkGo.post<String>(BaseHttp.complete_order)
                                    .tag(this@GrabDetailActivity)
                                    .headers("token", getString("token"))
                                    .params("goodsOrderId", intent.getStringExtra("goodsOrderId"))
                                    .params("code", it)
                                    .execute(object : StringDialogCallback(baseContext) {

                                        override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                                            showToast(msg)
                                            EventBus.getDefault().post(RefreshMessageEvent("完成订单"))
                                            handler.sendEmptyMessage(0)
                                        }

                                    })
                        }
                    }
                }
            }
            R.id.tv_nav_right -> showCancelDialog()
        }
    }

    @Suppress("DEPRECATION")
    private fun showCancelDialog() {
        val hintAgree = DecimalFormat("0.##").format(agreeCancel.toDouble() * 100)
        val hintUnAgree = DecimalFormat("0.##").format(unAgreeCancel.toDouble() * 100)
        val hintGrab = TimeHelper.getDiffTimeAfter(TimeHelper.getInstance().millisecondToLong(grabsingleTime))
        val hint = "您已接单$hintGrab<br>" +
                "用户同意取消，<font color='#F23030'>支付违约金金额为$hintAgree%佣金</font><br>" +
                "用户不同意您需要<font color='#F23030'>支付佣金的$hintUnAgree%作为违约金</font>"

        DialogHelper.showHintDialog(baseContext,
                "取消订单",
                Html.fromHtml(hint),
                "取消",
                "确定",
                false) {
            if (it == "right") {
                OkGo.post<String>(BaseHttp.rider_cancel_order)
                        .tag(this@GrabDetailActivity)
                        .headers("token", getString("token"))
                        .params("goodsOrderId", intent.getStringExtra("goodsOrderId"))
                        .execute(object : StringDialogCallback(baseContext) {

                            override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                                showToast(msg)
                                EventBus.getDefault().post(RefreshMessageEvent("骑手取消"))
                                handler.sendEmptyMessage(0)
                            }

                        })
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun showCancelAgreeDialog() {
        val hintAgree = DecimalFormat("0.##").format(agreeCancel.toDouble() * 100)
        val hintUnAgree = DecimalFormat("0.##").format(unAgreeCancel.toDouble() * 100)
        val hint = "客户取消订单<br>" +
                "您同意取消，<font color='#F23030'>获得违约金金额为$hintAgree%佣金</font><br>" +
                "您不同意取消，<font color='#F23030'>获得违约金金额为$hintUnAgree%佣金</font>"

        DialogHelper.showHintDialog(baseContext,
                "客户取消订单通知",
                Html.fromHtml(hint),
                "不同意",
                "同意",
                false) {
            OkGo.post<String>(BaseHttp.rider_deal_cancel_order)
                    .tag(this@GrabDetailActivity)
                    .headers("token", getString("token"))
                    .params("goodsOrderId", intent.getStringExtra("goodsOrderId"))
                    .params("agree", if (it == "right") "1" else "0")
                    .execute(object : StringDialogCallback(baseContext) {

                        override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                            showToast(msg)
                            EventBus.getDefault().post(RefreshMessageEvent("骑手同意"))
                            handler.sendEmptyMessage(0)
                        }

                    })
        }
    }

    override fun getData() {
        OkGo.post<BaseResponse<CommonData>>(BaseHttp.goodsorde_dealis)
                .tag(this@GrabDetailActivity)
                .headers("token", getString("token"))
                .params("goodsOrderId", intent.getStringExtra("goodsOrderId"))
                .execute(object : JacksonDialogCallback<BaseResponse<CommonData>>(baseContext, true) {

                    @SuppressLint("SetTextI18n")
                    override fun onSuccess(response: Response<BaseResponse<CommonData>>) {
                        val data = response.body().`object` ?: return

                        mStauts = data.status
                        mType = data.type
                        buyMobile = data.buyMobile
                        receiptMobile = data.receiptMobile
                        agreeCancel = data.agreeCancel
                        unAgreeCancel = data.unAgreeCancel
                        grabsingleTime = data.grabsingleTime

                        when (mStauts) {
                            "-3", "-2", "-1", "0", "3" -> {
                                bt_done.gone()
                                tvRight.text = ""
                            }
                            "1" -> {
                                if (intent.getBooleanExtra("isAll", false)) {
                                    bt_done.visible()
                                    bt_done.setBackgroundResource(if (mType == "0") R.drawable.rec_bg_red_shade else R.drawable.rec_bg_blue_shade)
                                    bt_done.text = if (mType == "0") "代买抢单" else "顺风抢单"
                                } else bt_done.gone()
                                tvRight.text = ""
                            }
                            "2" -> {
                                bt_done.visible()
                                bt_done.setBackgroundResource(R.drawable.rec_bg_blue_shade)
                                bt_done.text = "我已完成"
                                tvRight.text = "取消订单"
                            }
                        }

                        when (mType) {
                            "0" -> grab_check_ll.gone()
                            "1" -> grab_check_ll.visible()
                        }

                        grab_type1.text = if (mType == "1") "顺风商品：" else "代买商品："
                        grab_type2.text = if (mType == "1") "取件时间：" else "购买时间："
                        grab_product.text = data.goods
                        grab_time.text = data.createDate
                        grab_img1.setImageResource(if (mType == "1") R.mipmap.index_lab05 else R.mipmap.index_lab01)
                        grab_addr1.text = data.buyAddress + data.buyDetailAdress
                        grab_name1.text = "${data.buyname}  ${data.buyMobile}"
                        if (data.buyMobile.isEmpty()) grab_name1.gone()
                        grab_addr2.text = data.receiptAddress + data.receiptDetailAdress
                        grab_name2.text = "${data.receiptName}  ${data.receiptMobile}"
                        grab_check.text = when (data.inspection) {
                            "0" -> "否"
                            "1" -> "是"
                            else -> ""
                        }
                        grab_memo.text = data.mome
                        grab_order.text = "订单编号：${data.goodsOrderId}"
                        grab_submit.text = "下单时间：${data.createDate}"
                        grab_pay.text = "付款时间：${data.payTime}"
                        if (data.payTime.isEmpty()) grab_pay.gone() else grab_pay.visible()

                        val tip = if (data.tip.isEmpty()) 0.0 else data.tip.toDouble()
                        grab_commission.text = data.commission
                        @Suppress("DEPRECATION")
                        grab_commission_fee.text = Html.fromHtml("（小费：<font color='#F23030'>${data.tip}元）</font>")
                        if (tip == 0.0) grab_commission_fee.gone() else grab_commission_fee.visible()

                        if (mStauts == "-1" && data.cancelType == "0") window.decorView.postDelayed({
                            runOnUiThread { showCancelAgreeDialog() }
                        }, 300)
                    }

                })
    }
}
