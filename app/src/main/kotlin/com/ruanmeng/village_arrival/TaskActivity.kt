package com.ruanmeng.village_arrival

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.RadioGroup
import com.amap.api.AMapLocationHelper
import com.amap.api.services.core.AMapException
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.geocoder.GeocodeResult
import com.amap.api.services.geocoder.GeocodeSearch
import com.amap.api.services.geocoder.RegeocodeQuery
import com.amap.api.services.geocoder.RegeocodeResult
import com.lzg.extend.BaseResponse
import com.lzg.extend.StringDialogCallback
import com.lzg.extend.jackson.JacksonDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.ruanmeng.base.*
import com.ruanmeng.model.CommonData
import com.ruanmeng.model.CommonModel
import com.ruanmeng.model.LocationMessageEvent
import com.ruanmeng.model.RefreshMessageEvent
import com.ruanmeng.share.BaseHttp
import com.ruanmeng.utils.ActivityStack
import com.ruanmeng.utils.DialogHelper
import com.ruanmeng.utils.TimeHelper
import kotlinx.android.synthetic.main.activity_task.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONObject
import java.util.*

class TaskActivity : BaseActivity() {

    private var type = ""
    private var locationLat = ""
    private var locationLng = ""
    private var locationProvince = ""
    private var locationCity = ""
    private var locationDistrict = ""
    private var locationTownship = ""
    private var buyLat = ""
    private var buyLng = ""
    private var buyProvince = ""
    private var buyCity = ""
    private var buyDistrict = ""
    private var buyTownship = ""
    private var buyAddress = "就近购买"
    private var buyDetailAdress = ""
    private var buyName = ""
    private var buyMobile = ""
    private var buycommonAddressId = ""
    private var receiptLat = ""
    private var receiptLng = ""
    private var receiptProvince = ""
    private var receiptCity = ""
    private var receiptDistrict = ""
    private var receiptTownship = ""
    private var receiptAddress = ""
    private var receiptDetailAdress = ""
    private var receiptcommonAddressId = ""
    private var receiptName = ""
    private var receiptMobile = ""
    private var weightPriceId = ""
    private var goodstypeId = ""
    private var voucherId = ""
    private var deliveryTime: String = ""
    private val list = ArrayList<CommonData>()
    private val listType = ArrayList<CommonData>()
    private val listCoupon = ArrayList<CommonData>()

    private lateinit var geocoderSearch: GeocodeSearch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)
        init_title(intent.getStringExtra("title"))

        EventBus.getDefault().register(this@TaskActivity)

        startLocation()
        getData()
        getCouponData()
    }

    override fun init_title() {
        super.init_title()
        task_get_name.gone()
        task_put_name.gone()
        task_group.setOnCheckedChangeListener(this@TaskActivity)

        when (intent.getStringExtra("type")) {
            "buy" -> task_check1.isChecked = true
            "get" -> task_check2.isChecked = true
        }

        task_price.addTextChangedListener(this@TaskActivity)

        //逆向地理编码
        geocoderSearch = GeocodeSearch(baseContext)
        geocoderSearch.setOnGeocodeSearchListener(object : GeocodeSearch.OnGeocodeSearchListener {

            override fun onRegeocodeSearched(result: RegeocodeResult?, code: Int) {
                if (code == AMapException.CODE_AMAP_SUCCESS) {
                    if (result?.regeocodeAddress != null
                            && result.regeocodeAddress.formatAddress != null) {

                        locationProvince = result.regeocodeAddress.province
                        locationCity = result.regeocodeAddress.city
                        locationDistrict = result.regeocodeAddress.district
                        locationTownship = result.regeocodeAddress.township

                        if (intent.getStringExtra("type") == "buy") {
                            buyLat = locationLat
                            buyLng = locationLng
                            buyProvince = locationProvince
                            buyCity = locationCity
                            buyDistrict = locationDistrict
                            buyTownship = locationTownship
                        }
                    }
                }
            }

            override fun onGeocodeSearched(result: GeocodeResult, code: Int) {}

        })

        task_coupon.setOnClickListener {
            if (listCoupon.isEmpty()) {
                showToast("暂无可用优惠券")
                return@setOnClickListener
            }

            if (task_check2.isChecked && task_get_addr.text.isEmpty()) {
                showToast("请选择取货地址")
                return@setOnClickListener
            }

            if (task_put_addr.text.isEmpty()) {
                showToast("请选择收货地址")
                return@setOnClickListener
            }

            if (task_weight.text.isEmpty()) {
                showToast("请选择商品重量")
                return@setOnClickListener
            }

            val intent = Intent(baseContext, TaskCouponActivity::class.java)
            intent.putExtra("list", listCoupon)
            startActivity(intent)
        }

        task_type.setOnClickListener { _ ->
            if (listType.isEmpty()) showToast("暂无相关数据")
            else {
                val listItems = ArrayList<String>()
                listType.mapTo(listItems) { it.goodstypeId }
                DialogHelper.showItemDialog(
                        baseContext,
                        "选择货物类型",
                        listItems) { _, name ->
                    goodstypeId = name
                    task_type.setRightString(name)
                }
            }
        }
    }

    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.task_get -> {
                val intent = Intent(baseContext, AddressAddActivity::class.java)
                intent.putExtra("title", if (task_check1.isChecked) "购买地址" else "取货地址")
                startActivity(intent)
            }
            R.id.task_put -> {
                val intent = Intent(baseContext, AddressAddActivity::class.java)
                intent.putExtra("title", "收货地址")
                startActivity(intent)
            }
            R.id.task_time_ll -> {
                DialogHelper.showDateDialog(baseContext, true) { hint, date ->
                    task_time.text = hint
                    deliveryTime = date
                }
            }
            R.id.task_weight_ll -> {
                if (list.isEmpty()) showToast("暂无相关数据")
                else {
                    val listItems = ArrayList<String>()
                    list.mapTo(listItems) { it.weightDescribe }
                    DialogHelper.showItemDialog(
                            baseContext,
                            "选择商品重量",
                            listItems) { position, name ->
                        if (position == list.size - 1) return@showItemDialog
                        weightPriceId = list[position].weightPriceId
                        task_weight.text = name

                        getFeeData()
                    }
                }
            }
            R.id.bt_submit -> {
                if (task_check1.isChecked
                        && buyAddress == "就近购买"
                        && buyTownship.isEmpty()) {
                    showToast("当前位置信息获取失败")
                    return
                }

                if (task_check1.isChecked && task_name.text.isBlank()) {
                    task_name.requestFocus()
                    showToast("请输入商品信息")
                    return
                }

                if (task_check2.isChecked && goodstypeId.isEmpty()) {
                    showToast("请选择货物类型")
                    return
                }

                if (task_check2.isChecked && task_get_addr.text.isEmpty()) {
                    showToast("请选择取货地址")
                    return
                }

                if (task_put_addr.text.isEmpty()) {
                    showToast("请选择收货地址")
                    return
                }

                if (task_weight.text.isEmpty()) {
                    showToast("请选择商品重量")
                    return
                }

                if (deliveryTime.isEmpty())
                    deliveryTime = TimeHelper.getInstance().getNowTime("yyyy-MM-dd HH:mm")

                OkGo.post<String>(BaseHttp.add_goodsorde)
                        .tag(this@TaskActivity)
                        .isMultipart(true)
                        .headers("token", getString("token"))
                        .params("type", type)
                        .params("goods", if (task_check1.isChecked) task_name.text.toString() else goodstypeId)
                        .params("receiptTime", deliveryTime)
                        .params("weightPriceId", weightPriceId)
                        .params("mome", task_memo.text.toString())
                        .params("commission", task_fee.text.toString())
                        .params("goodsPrice", task_price.text.toString())
                        .params("tip", "0")
                        .params("inspection", if (task_open1.isChecked) 1 else 0)
                        .params("buycommonAddressId", buycommonAddressId)
                        .params("buyAddress", buyAddress)
                        .params("buyDetailAdress", buyDetailAdress)
                        .params("buyLat", buyLat)
                        .params("buyLng", buyLng)
                        .params("buyProvince", buyProvince)
                        .params("buyCity", buyCity)
                        .params("buyDistrict", buyDistrict)
                        .params("buyTownship", buyTownship)
                        .params("buyname", buyName)
                        .params("buyMobile", buyMobile)
                        .params("commonAddressId", receiptcommonAddressId)
                        .params("receiptAddress", receiptAddress)
                        .params("receiptDetailAdress", receiptDetailAdress)
                        .params("receiptLat", receiptLat)
                        .params("receiptLng", receiptLng)
                        .params("receiptProvince", receiptProvince)
                        .params("receiptCity", receiptCity)
                        .params("receiptDistrict", receiptDistrict)
                        .params("receiptTownship", receiptTownship)
                        .params("receiptName", receiptName)
                        .params("receiptMobile", receiptMobile)
                        .params("voucherId", voucherId)
                        .execute(object : StringDialogCallback(baseContext) {

                            override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                                val obj = JSONObject(response.body()).getJSONObject("object") ?: JSONObject()
                                val status = obj.optString("status")
                                if (status == "1") {
                                    EventBus.getDefault().post(RefreshMessageEvent("支付成功"))
                                    val intent = Intent(baseContext, TaskResultActivity::class.java)
                                    intent.putExtra("goodsOrderId", obj.optString("goodsOrderId"))
                                    intent.putExtra("title", "支付成功")
                                    intent.putExtra("hint", "提交订单")
                                    startActivity(intent)
                                    ActivityStack.screenManager.popActivities(this@TaskActivity::class.java)
                                } else {
                                    val intent = Intent(baseContext, IssuePayActivity::class.java)
                                    intent.putExtra("hint", "提交订单")
                                    intent.putExtra("goodsOrderId", obj.optString("goodsOrderId"))
                                    intent.putExtra("commission", obj.optString("realPaySum", "0.00"))
                                    startActivity(intent)
                                    ActivityStack.screenManager.popActivities(this@TaskActivity::class.java)
                                }
                            }

                        })
            }
        }
    }

    private fun startLocation() {
        AMapLocationHelper.getInstance(baseContext).clearCodes()
        AMapLocationHelper.getInstance(baseContext).startLocation(2) { location, isSuccessed, codes ->
            if (isSuccessed && 2 in codes) {
                locationLat = location.latitude.toString()
                locationLng = location.longitude.toString()

                window.decorView.post {
                    geocoderSearch.getFromLocationAsyn(RegeocodeQuery(LatLonPoint(
                            location.latitude, location.longitude),
                            100f, GeocodeSearch.AMAP))
                }
            }
        }
    }

    override fun getData() {
        OkGo.post<BaseResponse<CommonModel>>(BaseHttp.weightprice_list_data)
                .tag(this@TaskActivity)
                .headers("token", getString("token"))
                .params("flage", "new")
                .execute(object : JacksonDialogCallback<BaseResponse<CommonModel>>(baseContext) {

                    override fun onSuccess(response: Response<BaseResponse<CommonModel>>) {
                        listType.addItems(response.body().`object`.goodstype)
                        list.addItems(response.body().`object`.weightprice)
                        /*if (list.isNotEmpty()) {
                            weightPriceId = list[0].weightPriceId
                            task_weight.text = list[0].weightDescribe
                        }*/
                    }

                })
    }

    private fun getCouponData() {
        OkGo.post<BaseResponse<ArrayList<CommonData>>>(BaseHttp.user_voucer_list)
                .tag(this@TaskActivity)
                .headers("token", getString("token"))
                .execute(object : JacksonDialogCallback<BaseResponse<ArrayList<CommonData>>>(baseContext) {

                    override fun onSuccess(response: Response<BaseResponse<ArrayList<CommonData>>>) {
                        listCoupon.addItems(response.body().`object`)
                        if (listCoupon.isEmpty()) task_coupon.setRightString("暂无可用")
                        else task_coupon.setRightString("选择优惠券")
                    }

                })
    }

    private fun getFeeData() {
        if (buyLat.isNotEmpty() && buyLng.isNotEmpty()
                && receiptLat.isNotEmpty() && receiptLng.isNotEmpty()
                && weightPriceId.isNotEmpty()) {
            OkGo.post<String>(BaseHttp.order_commission)
                    .tag(this@TaskActivity)
                    .headers("token", getString("token"))
                    .params("weightPriceId", weightPriceId)
                    .params("lat1Str", buyLat)
                    .params("lng1Str", buyLng)
                    .params("lat2Str", receiptLat)
                    .params("lng2Str", receiptLng)
                    .params("voucherId", voucherId)
                    .params("buyType", if (buyAddress == "就近购买") "1" else "")
                    .execute(object : StringDialogCallback(baseContext, false) {

                        override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {
                            val obj = JSONObject(response.body())
                            task_fee.text = obj.optString("object", "0.00")
                        }

                    })
        }
    }

    @Suppress("DEPRECATION")
    override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
        when (checkedId) {
            R.id.task_check1 -> {
                type = "0"
                task_name.hint = "请输入要买的商品：如香烟，酒水"
                task_get_hint.text = "购买地址"
                task_get_addr.hint = "未填写则就近购买(2公里内)"
                task_get_addr.setHintTextColor(resources.getColor(R.color.orange))
                task_name.visible()
                task_type.gone()
                task_price_ll.visible()
                task_open.gone()

                buyLat = locationLat
                buyLng = locationLng
                buyProvince = locationProvince
                buyCity = locationCity
                buyDistrict = locationDistrict
                buyTownship = locationTownship
                buyAddress = "就近购买"
                buyDetailAdress = ""
                buycommonAddressId = ""
                buyName = ""
                buyMobile = ""
            }
            R.id.task_check2 -> {
                type = "1"
                task_name.hint = "请输入货物类型"
                task_type.setRightString("请选择货物类型")
                task_get_hint.text = "取货地址"
                task_get_addr.hint = "请选择取货地址"
                task_get_addr.setHintTextColor(resources.getColor(R.color.colorControlNormal))
                task_name.gone()
                task_type.visible()
                task_price_ll.gone()
                task_open.visible()

                goodstypeId = ""
                receiptLat = ""
                receiptLng = ""
                receiptProvince = ""
                receiptCity = ""
                receiptDistrict = ""
                receiptTownship = ""
                receiptAddress = ""
                receiptDetailAdress = ""
                receiptcommonAddressId = ""
                receiptName = ""
                receiptMobile = ""
            }
        }

        task_name.setText("")
        task_get_addr.text = ""
        task_put_addr.text = ""
        task_get_name.text = ""
        task_put_name.text = ""
        task_get_name.gone()
        task_put_name.gone()
        task_time.text = "现在送货"
        deliveryTime = ""
        // val hint = "商品重量<small><font color='${resources.getColor(R.color.light)}'>（默认2公斤以内）</font></small>"
        weightPriceId = ""
        task_weight.text = ""
        task_memo.setText("")
        task_price.setText("")
        task_open.check(R.id.task_open2)
        task_fee.text = "0"
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (s.isNotEmpty()) {
            if ("." == s.toString()) {
                task_price.setText("0.")
                task_price.setSelection(task_price.text.length) //设置光标的位置
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

    override fun finish() {
        EventBus.getDefault().unregister(this@TaskActivity)
        super.finish()
    }

    @SuppressLint("SetTextI18n")
    @Subscribe
    fun onMessageEvent(event: LocationMessageEvent) {
        when (event.type) {
            "购买地址", "取货地址" -> {
                buycommonAddressId = event.addressId
                buyLat = event.lat
                buyLng = event.lng
                buyProvince = if (buycommonAddressId.isEmpty()) event.province else ""
                buyCity = if (buycommonAddressId.isEmpty()) event.city else ""
                buyDistrict = if (buycommonAddressId.isEmpty()) event.district else ""
                buyTownship = if (buycommonAddressId.isEmpty()) event.township else ""
                buyAddress = event.address
                buyDetailAdress = event.detail
                buyName = event.name
                buyMobile = event.mobile

                task_get_addr.text = event.address + event.detail
                if (event.name.isNotEmpty()) {
                    task_get_name.visible()
                    task_get_name.text = "${event.name}  ${event.mobile}"
                }
            }
            "收货地址" -> {
                receiptcommonAddressId = event.addressId
                receiptLat = event.lat
                receiptLng = event.lng
                receiptProvince = if (receiptcommonAddressId.isEmpty()) event.province else ""
                receiptCity = if (receiptcommonAddressId.isEmpty()) event.city else ""
                receiptDistrict = if (receiptcommonAddressId.isEmpty()) event.district else ""
                receiptTownship = if (receiptcommonAddressId.isEmpty()) event.township else ""
                receiptAddress = event.address
                receiptDetailAdress = event.detail
                receiptName = event.name
                receiptMobile = event.mobile

                task_put_addr.text = event.address + event.detail
                if (event.name.isNotEmpty()) {
                    task_put_name.visible()
                    task_put_name.text = "${event.name}  ${event.mobile}"
                }
            }
            "优惠券" -> {
                voucherId = event.addressId
                @Suppress("DEPRECATION")
                task_coupon.setRightTVColor(resources.getColor(R.color.red))
                task_coupon.setRightString("已优惠${event.address}元")
            }
        }

        getFeeData()
    }
}
