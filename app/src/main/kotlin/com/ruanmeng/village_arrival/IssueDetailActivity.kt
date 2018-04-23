package com.ruanmeng.village_arrival

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import com.lzg.extend.BaseResponse
import com.lzg.extend.jackson.JacksonDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.ruanmeng.base.*
import com.ruanmeng.model.CommonData
import com.ruanmeng.model.RefreshMessageEvent
import com.ruanmeng.share.BaseHttp
import kotlinx.android.synthetic.main.activity_issue_detail.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class IssueDetailActivity : BaseActivity() {

    private var mStauts = ""
    private var mType = ""
    private var sendTelephone = ""
    private var mCommission = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_issue_detail)
        init_title("订单详情")

        EventBus.getDefault().register(this@IssueDetailActivity)

        getData()
    }

    override fun init_title() {
        super.init_title()
        @Suppress("DEPRECATION")
        tvRight.setTextColor(resources.getColor(R.color.light))
        issue_info_ll.gone()
        issue_commission_ll.gone()
        issue_add.gone()
        bt_press.gone()
        issue_status.gone()
    }

    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.bt_press -> when (bt_press.text.toString()) {
                "立即付款" -> {
                    if (mCommission.isEmpty()) return

                    intent.setClass(baseContext, IssuePayActivity::class.java)
                    intent.putExtra("commission", mCommission)
                    startActivity(intent)
                }
                "立即评价" -> {
                    intent.setClass(baseContext, IssueCommentActivity::class.java)
                    startActivity(intent)
                }
            }
            R.id.tv_nav_right -> {
                if (mStauts == "0" || mStauts == "1" || mStauts == "2") {

                }
            }
        }
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
                            "0" -> {
                                issue_total_ll.visible()
                                issue_commission_ll.gone()
                                issue_check_ll.gone()
                            }
                            "1" -> {
                                issue_total_ll.gone()
                                issue_commission_ll.visible()
                                issue_check_ll.visible()
                            }
                        }

                        if (data.sendUserInfoId.isNotEmpty()) {
                            issue_info_ll.visible()
                            sendTelephone = data.sendTelephone
                            issue_name.text = "抢单员：${data.sendNickName}"
                            issue_img.loadImage(BaseHttp.baseImg + data.sendUserHead)
                            issue_rating.rating = if (data.userGrade.isEmpty()) 0f else data.userGrade.toFloat()
                        }

                        issue_type1.text = if (mType == "1") "顺风商品：" else "代买商品："
                        issue_type2.text = if (mType == "1") "取件时间：" else "购买时间："
                        issue_product.text = data.goods
                        issue_time.text = data.createDate
                        issue_img1.setImageResource(if (mType == "1") R.mipmap.index_lab05 else R.mipmap.index_lab01)
                        issue_addr1.text = data.buyAddress + data.buyDetailAdress
                        issue_name1.text = "${data.buyname}  ${data.buyMobile}"
                        if (data.buyAddress == "就近购买") issue_name1.gone()
                        issue_addr2.text = data.receiptAddress + data.receiptDetailAdress
                        issue_name2.text = "${data.receiptName}  ${data.receiptMobile}"
                        issue_check.text = when (data.inspection) {
                            "0" -> "否"
                            "1" -> "是"
                            else -> ""
                        }
                        issue_memo.text = data.mome
                        grab_order.text = "订单编号：${data.goodsOrderId}"
                        grab_submit.text = "下单时间：${data.createDate}"
                        grab_pay.text = "付款时间：${data.payTime}"
                        if (data.payTime.isEmpty()) grab_pay.gone() else grab_pay.visible()

                        mCommission = data.commission
                        val tip = data.tip.toDouble()

                        issue_total.text = String.format("%.2f", mCommission.toDouble() + tip)
                        issue_yong.text = "${data.commission}元"
                        issue_fee.text = "${data.tip}元"
                        if (tip == 0.0) issue_fee_ll.gone() else issue_fee_ll.visible()
                        issue_commission.text = data.commission
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
            "支付成功" -> getData()
        }
    }
}
