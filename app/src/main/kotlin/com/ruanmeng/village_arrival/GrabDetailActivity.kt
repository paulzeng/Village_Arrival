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
import com.ruanmeng.share.BaseHttp
import kotlinx.android.synthetic.main.activity_grab_detail.*

class GrabDetailActivity : BaseActivity() {

    private var mStauts = ""
    private var mType = ""
    private var sendTelephone = ""
    private var mCommission = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grab_detail)
        init_title("订单详情")

        getData()
    }

    override fun init_title() {
        super.init_title()
        @Suppress("DEPRECATION")
        tvRight.setTextColor(resources.getColor(R.color.light))
        grab_commission_ll.gone()
        bt_done.gone()
    }

    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.bt_done -> startActivity<TaskContactActivity>()
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

                        when (mStauts) {
                            "-3", "-2", "-1", "1", "0", "3" -> {
                                bt_done.gone()
                                tvRight.text = ""
                            }
                            "2" -> {
                                bt_done.visible()
                                tvRight.text = "取消订单"
                            }
                        }

                        when (mType) {
                            "0" -> {
                                grab_total_ll.visible()
                                grab_commission_ll.gone()
                                grab_check_ll.gone()
                            }
                            "1" -> {
                                grab_total_ll.gone()
                                grab_commission_ll.visible()
                                grab_check_ll.visible()
                            }
                        }

                        grab_type1.text = if (mType == "1") "顺风商品：" else "代买商品："
                        grab_type2.text = if (mType == "1") "取件时间：" else "购买时间："
                        grab_product.text = data.goods
                        grab_time.text = data.createDate
                        grab_img1.setImageResource(if (mType == "1") R.mipmap.index_lab05 else R.mipmap.index_lab01)
                        grab_addr1.text = data.buyAddress + data.buyDetailAdress
                        grab_name1.text = "${data.buyname}  ${data.buyMobile}"
                        if (data.buyAddress == "就近购买") grab_name1.gone()
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

                        mCommission = data.commission
                        val tip = data.tip.toDouble()

                        grab_total.text = String.format("%.2f", mCommission.toDouble() + tip)
                        grab_yong.text = "${data.commission}元"
                        grab_fee.text = "${data.tip}元"
                        if (tip == 0.0) grab_fee_ll.gone() else grab_fee_ll.visible()
                        grab_commission.text = data.commission
                    }

                })
    }
}
