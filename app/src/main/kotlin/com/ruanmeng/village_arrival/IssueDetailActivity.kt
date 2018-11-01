package com.ruanmeng.village_arrival

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.design.widget.BottomSheetDialog
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import com.cuieney.rxpay_annotation.WX
import com.cuieney.sdk.rxpay.RxPay
import com.lzg.extend.BaseResponse
import com.lzg.extend.StringDialogCallback
import com.lzg.extend.jackson.JacksonDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.lzy.okgo.utils.OkLogger
import com.ruanmeng.base.*
import com.ruanmeng.model.CommonData
import com.ruanmeng.model.RefreshMessageEvent
import com.ruanmeng.share.BaseHttp
import com.ruanmeng.utils.DialogHelper
import com.ruanmeng.utils.Tools
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_issue_detail.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONObject
import java.text.DecimalFormat

@WX(packageName = "com.ruanmeng.village_arrival")
class IssueDetailActivity : BaseActivity() {

    private var mStauts = ""
    private var mType = ""
    private var mCommission = ""
    private var sendNickName = ""
    private var sendUserHead = ""
    private var sendTelephone = ""
    private var userGrade = ""

    private var agreeCancel = "0.0"
    private var unAgreeCancel = "0.0"

    @SuppressLint("HandlerLeak")
    private var handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            getData()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_issue_detail)
        init_title("订单详情")

        EventBus.getDefault().register(this@IssueDetailActivity)

        handler.sendEmptyMessage(0)
    }

    override fun init_title() {
        super.init_title()
        @Suppress("DEPRECATION")
        tvRight.setTextColor(resources.getColor(R.color.light))
        issue_info_ll.gone()
        issue_add.gone()
        bt_press.gone()
        issue_status.gone()

        issue_add.setOnClickListener { showSheetDialog() }
    }

    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.bt_press -> when (bt_press.text.toString()) {
                "立即付款" -> {
                    if (mCommission.isEmpty()) return

                    intent.setClass(baseContext, IssuePayActivity::class.java)
                    intent.putExtra("hint", "发布详情")
                    intent.putExtra("commission", mCommission)
                    startActivity(intent)
                }
                "立即评价" -> {
                    intent.setClass(baseContext, IssueCommentActivity::class.java)
                    intent.putExtra("sendNickName", sendNickName)
                    intent.putExtra("sendUserHead", sendUserHead)
                    intent.putExtra("sendTelephone", sendTelephone)
                    intent.putExtra("userGrade", userGrade)
                    startActivity(intent)
                }
            }
            R.id.issue_call -> {
                if (sendTelephone.isEmpty()) {
                    showToast("电话号码为空")
                    return
                }

                DialogHelper.showHintDialog(baseContext,
                        "拨打电话",
                        "抢单员电话：$sendTelephone，确定要拨打吗？ ",
                        "取消",
                        "确定",
                        true) {
                    if (it == "right") {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$sendTelephone"))
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }
                }
            }
            R.id.tv_nav_right -> {
                when (mStauts) {
                    "0", "1" -> {
                        DialogHelper.showHintDialog(baseContext,
                                "取消订单",
                                getString(R.string.cancel_grab),
                                "取消",
                                "确定",
                                false) {
                            if (it == "right") getCancelData()
                        }
                    }
                    "2" -> showCancelDialog()
                }
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun showSheetDialog() {
        val view = LayoutInflater.from(baseContext).inflate(R.layout.dialog_issue_pay, null) as View
        val payCount = view.findViewById<EditText>(R.id.pay_count)
        val payGroup = view.findViewById<RadioGroup>(R.id.pay_group)
        val btPay = view.findViewById<Button>(R.id.bt_pay)
        val dialog = BottomSheetDialog(baseContext)

        payGroup.check(R.id.pay_check1)
        payCount.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isNotEmpty()) {
                    if ("." == s.toString()) {
                        payCount.setText("0.")
                        payCount.setSelection(payCount.text.length) //设置光标的位置
                    }
                }
            }

            override fun afterTextChanged(s: Editable) {
                val temp = s.toString()
                val posDot = temp.indexOf(".")
                if (posDot < 0) {
                    if (temp.length > 7) s.delete(7, 8)
                } else {
                    if (temp.length - posDot - 1 > 2) s.delete(posDot + 3, posDot + 4)
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        })
        btPay.setOnClickListener {
            if (payCount.text.isEmpty()) {
                showToast("请输入小费金额")
                return@setOnClickListener
            }

            if (payCount.text.toString().toDouble() == 0.0) {
                showToast("输入金额为0元，请重新输入")
                return@setOnClickListener
            }

            dialog.dismiss()

            when (payGroup.checkedRadioButtonId) {
                R.id.pay_check1 -> window.decorView.postDelayed({ getPayData(payCount.text.toString()) }, 300)
                R.id.pay_check2 -> window.decorView.postDelayed({
                    getThirdPayData(payCount.text.toString(), "AliPay")
                }, 300)
                R.id.pay_check3 -> window.decorView.postDelayed({
                    getThirdPayData(payCount.text.toString(), "WxPay")
                }, 300)
            }
        }

        dialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        dialog.setContentView(view)
        dialog.show()
    }

    @Suppress("DEPRECATION")
    private fun showCancelDialog() {
        val hintAgree = DecimalFormat("0.##").format(agreeCancel.toDouble() * 100)
        val hint = "订单派送中，您确定要取消订单？<br>" +
                "骑手同意取消，<font color='#F23030'>支付违约金金额为$hintAgree%顺送费</font><br>" +
                "骑手不同意取消，<font color='#F23030'>订单继续派送</font>"

        DialogHelper.showHintDialog(baseContext,
                "取消订单",
                Html.fromHtml(hint),
                "取消",
                "确定",
                false) {
            if (it == "right") getCancelData()
        }
    }

    @Suppress("DEPRECATION")
    private fun showCancelAgreeDialog() {
        val hint = "骑手申请取消订单<br>" +
                "您同意取消，此单将等待其他人抢单<br>" +
                "您不同意取消，接单一小时内<font color='#F23030'>获得违约金金额为50%顺送费</font><br>" +
                "接单超过一小时<font color='#F23030'>获得违约金金额为100%顺送费</font>"

        DialogHelper.showHintDialog(baseContext,
                "骑手取消订单",
                Html.fromHtml(hint),
                "同意取消",
                "不同意取消",
                false) {
            OkGo.post<String>(BaseHttp.customer_cancel_order)
                    .tag(this@IssueDetailActivity)
                    .headers("token", getString("token"))
                    .params("goodsOrderId", intent.getStringExtra("goodsOrderId"))
                    .params("agree", if (it == "right") "0" else "1")
                    .execute(object : StringDialogCallback(baseContext) {

                        override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                            showToast(msg)
                            EventBus.getDefault().post(RefreshMessageEvent("客户同意"))
                            handler.sendEmptyMessage(0)
                        }

                    })
        }
    }

    private fun getCancelData() {
        OkGo.post<String>(BaseHttp.cancel_order)
                .tag(this@IssueDetailActivity)
                .headers("token", getString("token"))
                .params("goodsOrderId", intent.getStringExtra("goodsOrderId"))
                .execute(object : StringDialogCallback(baseContext) {

                    override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                        showToast(msg)
                        EventBus.getDefault().post(RefreshMessageEvent("客户取消"))
                        handler.sendEmptyMessage(0)
                    }

                })
    }

    override fun getData() {
        OkGo.post<BaseResponse<CommonData>>(BaseHttp.goodsorde_dealis)
                .tag(this@IssueDetailActivity)
                .headers("token", getString("token"))
                .params("goodsOrderId", intent.getStringExtra("goodsOrderId"))
                .execute(object : JacksonDialogCallback<BaseResponse<CommonData>>(baseContext, true) {

                    @SuppressLint("SetTextI18n")
                    override fun onSuccess(response: Response<BaseResponse<CommonData>>) {
                        val data = response.body().`object` ?: return

                        mStauts = data.status
                        mType = data.type
                        agreeCancel = data.agreeCancel
                        unAgreeCancel = data.unAgreeCancel

                        when (mStauts) {
                            "-3" -> {
                                issue_status.visible()
                                bt_press.gone()
                                issue_add.gone()
                                issue_status.text = "已删除"
                                tvRight.text = ""
                            }
                            "-2" -> {
                                issue_status.visible()
                                bt_press.gone()
                                issue_add.gone()
                                issue_status.text = "已取消"
                                tvRight.text = ""
                            }
                            "-1" -> {
                                issue_status.visible()
                                bt_press.gone()
                                issue_add.gone()
                                issue_status.text = "取消中"
                                tvRight.text = ""
                            }
                            "1" -> {
                                issue_status.visible()
                                bt_press.gone()
                                issue_add.visible()
                                issue_status.text = "待抢单"
                                tvRight.text = "取消订单"
                            }
                            "2" -> {
                                issue_status.visible()
                                bt_press.gone()
                                issue_add.gone()
                                issue_status.text = "进行中"
                                tvRight.text = "取消订单"
                            }
                            "0" -> {
                                issue_status.gone()
                                bt_press.visible()
                                issue_add.gone()
                                bt_press.text = "立即付款"
                                tvRight.text = "取消订单"
                            }
                            "3" -> {
                                issue_status.gone()
                                bt_press.visible()
                                issue_add.gone()
                                bt_press.text = "立即评价"
                                tvRight.text = ""
                            }
                        }

                        when (mType) {
                            "0" -> issue_check_ll.gone()
                            "1" -> issue_check_ll.visible()
                        }

                        if (data.sendUserInfoId.isNotEmpty()) {
                            issue_info_ll.visible()
                            sendNickName = Tools.decodeUnicode(data.sendNickName)
                            sendUserHead = data.sendUserHead
                            sendTelephone = data.sendTelephone
                            userGrade = data.userGrade
                            issue_name.text = "抢单员：$sendNickName"
                            issue_img.loadImage(BaseHttp.baseImg + sendUserHead)
                            issue_rating.rating = if (userGrade.isEmpty()) 0f else userGrade.toFloat()
                        }

                        issue_type1.text = if (mType == "1") "顺风商品：" else "代买商品："
                        issue_type2.text = if (mType == "1") "取件时间：" else "购买时间："
                        issue_product.text = data.goods
                        issue_weight.text = data.weightDescribe
                        issue_time.text = data.createDate
                        issue_send.text = data.receiptTime
                        issue_img1.setImageResource(if (mType == "1") R.mipmap.index_lab05 else R.mipmap.index_lab01)
                        issue_addr1.text = data.buyAddress + data.buyDetailAdress
                        issue_name1.text = "${data.buyname}  ${data.buyMobile}"
                        if (data.buyMobile.isEmpty()) issue_name1.gone()
                        issue_addr2.text = data.receiptAddress + data.receiptDetailAdress
                        issue_name2.text = "${data.receiptName}  ${data.receiptMobile}"
                        issue_check.text = when (data.inspection) {
                            "0" -> "否"
                            "1" -> "是"
                            else -> ""
                        }
                        issue_memo.text = data.mome
                        issue_order.text = "订单编号：${data.goodsOrderId}"
                        issue_submit.text = "下单时间：${data.createDate}"
                        issue_pay.text = "付款时间：${data.payTime}"
                        if (data.payTime.isEmpty()) issue_pay.gone() else issue_pay.visible()

                        mCommission = data.commission
                        val tip = if (data.tip.isEmpty()) 0.0 else data.tip.toDouble()

                        issue_commission.text = data.commission
                        @Suppress("DEPRECATION")
                        issue_commission_fee.text = Html.fromHtml("（小费：<font color='#F23030'>${data.tip}元）</font>")
                        if (tip == 0.0) issue_commission_fee.gone() else issue_commission_fee.visible()

                        if (mStauts == "-1" && data.cancelType == "1") window.decorView.postDelayed({
                            runOnUiThread { showCancelAgreeDialog() }
                        }, 300)
                    }

                })
    }

    private fun getPayData(count: String) {
        OkGo.post<String>(BaseHttp.add_order_tip_balance)
                .tag(this@IssueDetailActivity)
                .headers("token", getString("token"))
                .params("goodsOrderId", intent.getStringExtra("goodsOrderId"))
                .params("renewPrice", count)
                .execute(object : StringDialogCallback(baseContext) {

                    override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                        showToast(msg)
                        handler.sendEmptyMessage(0)
                    }

                })
    }

    private fun getThirdPayData(count: String, type: String) {
        OkGo.post<String>(BaseHttp.pay_renew)
                .tag(this@IssueDetailActivity)
                .headers("token", getString("token"))
                .params("goodsOrderId", intent.getStringExtra("goodsOrderId"))
                .params("renewPrice", count)
                .params("payType", type)
                .execute(object : StringDialogCallback(baseContext) {

                    override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                        val obj = JSONObject(response.body()).optString("object")
                        val data = JSONObject(response.body()).optString("object")
                        when (type) {
                            "AliPay" -> RxPay(baseContext).requestAlipay(obj)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({
                                        if (it) {
                                            showToast("支付成功")
                                            handler.sendEmptyMessage(0)
                                        } else showToast("支付失败")
                                    }) { OkLogger.printStackTrace(it) }
                            "WxPay" -> RxPay(baseContext).requestWXpay(data)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({
                                        if (it) {
                                            showToast("支付成功")
                                            handler.sendEmptyMessage(0)
                                        } else showToast("支付失败")
                                    }) { OkLogger.printStackTrace(it) }
                        }
                    }

                })
    }

    override fun finish() {
        EventBus.getDefault().unregister(this@IssueDetailActivity)
        super.finish()
    }

    @Subscribe
    fun onMessageEvent(event: RefreshMessageEvent) {
        when (event.type) {
            "支付成功", "评价成功" -> handler.sendEmptyMessage(0)
        }
    }
}
