package com.ruanmeng.village_arrival

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.view.GravityCompat
import android.text.Html
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import cn.jpush.android.api.JPushInterface
import com.amap.api.maps.AMap
import com.amap.api.maps.AMapUtils
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*
import com.amap.api.maps.model.animation.ScaleAnimation
import com.amap.api.services.core.AMapException
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.geocoder.GeocodeResult
import com.amap.api.services.geocoder.GeocodeSearch
import com.amap.api.services.geocoder.RegeocodeQuery
import com.amap.api.services.geocoder.RegeocodeResult
import com.flyco.animation.FadeEnter.FadeEnter
import com.flyco.animation.FadeExit.FadeExit
import com.flyco.dialog.widget.popup.BubblePopup
import com.github.library.bubbleview.BubbleLinearLayout
import com.lzg.extend.BaseResponse
import com.lzg.extend.StringDialogCallback
import com.lzg.extend.jackson.JacksonDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.ruanmeng.base.*
import com.ruanmeng.model.CommonData
import com.ruanmeng.model.CommonModel
import com.ruanmeng.model.RefreshMessageEvent
import com.ruanmeng.share.BaseHttp
import com.ruanmeng.share.Const
import com.ruanmeng.utils.CommonUtil
import com.ruanmeng.utils.DensityUtil
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONObject
import java.util.*

class MainActivity : BaseActivity() {

    private val list = ArrayList<CommonData>()
    private val listMaker = ArrayList<Marker>()
    private lateinit var aMap: AMap
    private var centerLatLng: LatLng? = null
    private var locationLatLng: LatLng? = null
    private lateinit var geocoderSearch: GeocodeSearch

    private val listAddress = ArrayList<CommonData>() //常用地址
    private var nowAddress = ""   //当前地址
    private var nowCity = ""      //当前市
    private var nowDistrict = ""  //当前区
    private var nowCtn = "0"      //当前位置抢单数
    private var cyCtn = "0"       //常用位置抢单数

    @SuppressLint("HandlerLeak", "SetTextI18n")
    private var handler = object : Handler() {
        @Suppress("DEPRECATION")
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                0 -> {
                    main_order1.text = "${if (nowCtn.isEmpty()) "0" else nowCtn}单"
                    main_address.text = nowAddress

                    if (listAddress.isEmpty()) main_order2.text = "您还没有添加常用地址"
                    else {
                        val hint = "您常用地址附近有<font color='${resources.getColor(R.color.red)}'>${if (cyCtn.isEmpty()) "0" else cyCtn}单</font>可抢"
                        main_order2.text = Html.fromHtml(hint)
                    }
                }
                1 -> {
                    OkGo.getInstance().cancelTag("周边信息")
                    val target = msg.obj as LatLng
                    geocoderSearch.getFromLocationAsyn(RegeocodeQuery(
                            LatLonPoint(target.latitude, target.longitude),
                            100f,
                            GeocodeSearch.AMAP))
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        transparentStatusBar(false)
        main_map.onCreate(savedInstanceState)
        init_title()

        JPushInterface.resumePush(applicationContext)
        //设置别名（先注册）
        JPushInterface.setAlias(
                applicationContext,
                Const.JPUSH_SEQUENCE,
                getString("token"))

        EventBus.getDefault().register(this@MainActivity)
    }

    override fun onStart() {
        super.onStart()
        getPersonData()
        getAddressData()

        if (centerLatLng == null) {
            if (locationLatLng != null) {
                aMap.animateCamera(CameraUpdateFactory.changeLatLng(locationLatLng))
            }
        } else getNearData(centerLatLng!!.latitude, centerLatLng!!.longitude)
    }

    override fun onStop() {
        super.onStop()
        if (centerLatLng != null && locationLatLng != null) {
            val distance = AMapUtils.calculateLineDistance(centerLatLng, locationLatLng)
            if (distance > 0.5) centerLatLng = null
        }
    }

    override fun init_title() {
        aMap = main_map.map

        aMap.apply {
            myLocationStyle = MyLocationStyle().apply {
                //连续定位、蓝点不会移动到地图中心点，并且蓝点会跟随设备移动
                myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW_NO_CENTER)
                interval(5000)                     //设置连续定位模式下的定位间隔
                strokeColor(Color.TRANSPARENT)     //设置定位蓝点精度圆圈的边框颜色
                radiusFillColor(Color.TRANSPARENT) //设置定位蓝点精度圆圈的填充颜色
                showMyLocation(true)               //设置是否显示定位小蓝点
                //设置定位蓝点的icon图标方法
                myLocationIcon(BitmapDescriptorFactory.fromResource(R.mipmap.gps_point))

                setOnMyLocationChangeListener {
                    if (locationLatLng == null) {
                        locationLatLng = LatLng(it.latitude, it.longitude)
                        aMap.animateCamera(CameraUpdateFactory.changeLatLng(locationLatLng))
                    } else locationLatLng = LatLng(it.latitude, it.longitude)
                }
            }

            isTrafficEnabled = false       //实时交通状况
            mapType = AMap.MAP_TYPE_NORMAL //矢量地图模式
            isMyLocationEnabled = true     //触发定位并显示定位层
            showIndoorMap(true)            //设置是否显示室内地图
            moveCamera(CameraUpdateFactory.zoomTo(16f)) //缩放级别

            /**
             * 地图状态发生变化的监听
             *
             * 调用AMap.animateCamera(CameraUpdate)、AMap.moveCamera(CameraUpdate)及手势操作地图时会触发该回调
             */
            setOnCameraChangeListener(object : AMap.OnCameraChangeListener {

                override fun onCameraChangeFinish(position: CameraPosition) {
                    centerLatLng = position.target

                    handler.removeMessages(1)
                    val msg = Message()
                    msg.what = 1
                    msg.obj = position.target
                    handler.sendMessageDelayed(msg, 500)
                }

                override fun onCameraChange(position: CameraPosition) {
                    listMaker.forEach { it.destroy() }
                    listMaker.clear()
                }

            })

            /**
             * 定制marker的信息窗口
             */
            setInfoWindowAdapter(object : AMap.InfoWindowAdapter {

                override fun getInfoContents(marker: Marker): View? = null

                @SuppressLint("InflateParams", "SetTextI18n")
                override fun getInfoWindow(marker: Marker): View {

                    val view = LayoutInflater.from(baseContext).inflate(R.layout.pop_main_marker, null) as View
                    val img1 = view.findViewById<ImageView>(R.id.pop_main_img1)
                    val addr1 = view.findViewById<TextView>(R.id.pop_main_addr1)
                    val addr2 = view.findViewById<TextView>(R.id.pop_main_addr2)
                    val money = view.findViewById<TextView>(R.id.pop_main_money)
                    val grab = view.findViewById<TextView>(R.id.pop_main_grab)

                    val data = list[marker.snippet.toInt()]
                    img1.setImageResource(if (data.type == "1") R.mipmap.index_lab05 else R.mipmap.index_lab01)
                    addr1.text = data.buyAddress + data.buyDetailAdress
                    addr2.text = data.receiptAddress + data.receiptDetailAdress
                    money.text = data.commission + "元"
                    grab.setBackgroundResource(if (data.type == "1") R.drawable.rec_bg_blue_shade else R.drawable.rec_bg_red_shade)

                    grab.setOnClickListener {
                        val intent = Intent(baseContext, GrabDetailActivity::class.java)
                        intent.putExtra("isAll", true)
                        intent.putExtra("goodsOrderId", data.goodsOrderId)
                        startActivity(intent)
                    }

                    return view
                }

            })

            /**
             * marker的信息窗口点击事件监听
             */
            setOnInfoWindowClickListener {
                val data = list[it.snippet.toInt()]
                val intent = Intent(baseContext, GrabDetailActivity::class.java)
                intent.putExtra("isAll", true)
                intent.putExtra("goodsOrderId", data.goodsOrderId)
                startActivity(intent)
            }

            /**
             * marker点击事件监听
             *
             * true 返回true表示该点击事件已被处理，不再往下传递，返回false则继续往下传递
             */
            setOnMarkerClickListener {
                if (it.title == "抢单") {
                    if (it.isInfoWindowShown) it.hideInfoWindow()
                    else it.showInfoWindow()
                }
                return@setOnMarkerClickListener true
            }
        }

        aMap.uiSettings.apply {
            isScaleControlsEnabled = false    //比例尺
            isZoomControlsEnabled = false     //缩放按钮
            isCompassEnabled = false          //指南针
            isMyLocationButtonEnabled = false //定位按钮
            isTiltGesturesEnabled = true      //倾斜手势
            isRotateGesturesEnabled = false   //旋转手势
            setLogoBottomMargin(-50)          //隐藏logo
        }

        geocoderSearch = GeocodeSearch(baseContext)
        geocoderSearch.setOnGeocodeSearchListener(object : GeocodeSearch.OnGeocodeSearchListener {

            override fun onRegeocodeSearched(result: RegeocodeResult?, code: Int) {
                if (code == AMapException.CODE_AMAP_SUCCESS) {
                    if (result?.regeocodeAddress != null
                            && result.regeocodeAddress.formatAddress != null) {

                        val neighborhood = result.regeocodeAddress.neighborhood
                        val building = result.regeocodeAddress.building
                        val street = result.regeocodeAddress.streetNumber.street
                        val township = result.regeocodeAddress.township
                        nowCity = result.regeocodeAddress.city
                        nowDistrict = result.regeocodeAddress.district

                        nowAddress = if (neighborhood.isEmpty()) {
                            if (building.isEmpty()) {
                                if (street.isEmpty()) {
                                    township
                                } else street
                            } else building
                        } else neighborhood

                        main_title.text = result.regeocodeAddress.district
                        getNearData(result.regeocodeQuery.point.latitude, result.regeocodeQuery.point.longitude)
                    }
                }
            }

            override fun onGeocodeSearched(result: GeocodeResult, code: Int) {}

        })

        main_center.setOnClickListener {
            if (drawer.isDrawerOpen(GravityCompat.START)) drawer.closeDrawer(GravityCompat.START)
            else drawer.openDrawer(GravityCompat.START)
        }

        nav_msg.setOnClickListener(this@MainActivity)
        nav_setting.setOnClickListener(this@MainActivity)
        nav_info.setOnClickListener(this@MainActivity)
        nav_issue.setOnClickListener(this@MainActivity)
        nav_grab.setOnClickListener(this@MainActivity)
        nav_person.setOnClickListener(this@MainActivity)
        nav_account.setOnClickListener(this@MainActivity)
        nav_addr.setOnClickListener(this@MainActivity)
        nav_status.setOnClickListener(this@MainActivity)
        nav_share.setOnClickListener(this@MainActivity)
        nav_live.setOnClickListener(this@MainActivity)
    }

    @Suppress("DEPRECATION")
    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.main_msg -> startActivity<MessageActivity>()
            R.id.main_grab -> {
                val intent = Intent(baseContext, TaskGrabActivity::class.java)
                intent.putExtra("lat", aMap.cameraPosition.target.latitude.toString())
                intent.putExtra("lng", aMap.cameraPosition.target.longitude.toString())
                intent.putExtra("city", nowCity)
                intent.putExtra("district", nowDistrict)
                startActivity(intent)
            }
            R.id.main_live -> {
                val intent = Intent(baseContext, LiveHomeActivity::class.java)
                intent.putExtra("title", nowCity)
                startActivity(intent)
            }
            R.id.main_often_ll -> if (listAddress.isEmpty()) startActivity<AddressActivity>()
            R.id.main_issue -> {
                val inflate = View.inflate(baseContext, R.layout.pop_main_issue, null)
                val popBuy = inflate.findViewById<LinearLayout>(R.id.pop_buy)
                val popGet = inflate.findViewById<LinearLayout>(R.id.pop_get)
                val popBubble = inflate.findViewById<BubbleLinearLayout>(R.id.pop_bubble)

                val width = CommonUtil.getScreenWidth(baseContext) - DensityUtil.dp2px(40f)
                popBubble.layoutParams = ViewGroup.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT)

                val bubblePopup = BubblePopup(baseContext, inflate)
                bubblePopup.anchorView(main_issue)
                        .bubbleColor(resources.getColor(R.color.transparent))
                        .showAnim(FadeEnter().duration(300))
                        .dismissAnim(FadeExit().duration(300))
                        .gravity(Gravity.TOP)
                        .show()

                popBuy.setOnClickListener {
                    bubblePopup.dismiss()
                    window.decorView.postDelayed({
                        runOnUiThread {
                            val intent = Intent(baseContext, TaskActivity::class.java)
                            intent.putExtra("type", "buy")
                            intent.putExtra("title", main_title.text.toString())
                            startActivity(intent)
                        }
                    }, 500)
                }
                popGet.setOnClickListener {
                    bubblePopup.dismiss()
                    window.decorView.postDelayed({
                        runOnUiThread {
                            val intent = Intent(baseContext, TaskActivity::class.java)
                            intent.putExtra("type", "get")
                            intent.putExtra("title", main_title.text.toString())
                            startActivity(intent)
                        }
                    }, 300)
                }
            }
        }
    }

    override fun onClick(v: View) {
        drawer.closeDrawer(GravityCompat.START)

        window.decorView.postDelayed({
            runOnUiThread {
                when (v.id) {
                    R.id.nav_msg -> startActivity<MessageActivity>()
                    R.id.nav_setting -> startActivity<SettingActivity>()
                    R.id.nav_info -> startActivity<InfoActivity>()
                    R.id.nav_issue -> startActivity<IssueActivity>()
                    R.id.nav_grab -> startActivity<GrabActivity>()
                    R.id.nav_person -> startActivity<InfoActivity>()
                    R.id.nav_account -> startActivity<AccountActivity>()
                    R.id.nav_addr -> startActivity<AddressActivity>()
                    R.id.nav_status -> startActivity<ApplyActivity>()
                    R.id.nav_share -> startActivity<ShareActivity>()
                    R.id.nav_live -> startActivity<LiveMineActivity>()
                }
            }
        }, 300)
    }

    private fun getPersonData() {
        OkGo.post<String>(BaseHttp.user_msg_data)
                .tag(this@MainActivity)
                .headers("token", getString("token"))
                .execute(object : StringDialogCallback(baseContext, false) {

                    @SuppressLint("SetTextI18n")
                    override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                        val obj = JSONObject(response.body()).getJSONObject("userMsg")
                                ?: JSONObject()
                        putString("nickName", obj.getString("nickName"))
                        putString("userhead", obj.getString("userhead"))
                        putString("sex", obj.getString("sex"))
                        putString("pass", obj.getString("pass"))
                        putString("status", obj.getString("astatus"))

                        nav_name.text = getString("nickName")
                        nav_tel.text = "手机  ${CommonUtil.phoneReplaceWithStar(getString("mobile"))}"

                        if (nav_img.getTag(R.id.nav_img) == null) {
                            nav_img.loadImage(BaseHttp.baseImg + getString("userhead"))
                            nav_img.setTag(R.id.nav_img, getString("userhead"))
                        } else {
                            if (nav_img.getTag(R.id.nav_img) != getString("userhead")) {
                                nav_img.loadImage(BaseHttp.baseImg + getString("userhead"))
                                nav_img.setTag(R.id.nav_img, getString("userhead"))
                            }
                        }

                        nav_status.setRightString(when (getString("status")) {
                            "-1" -> "审核中"
                            "0" -> "未通过"
                            "1" -> "已审核"
                            else -> "未审核"
                        })
                    }

                })
    }

    private fun getNearData(lat: Double, lng: Double) {
        OkGo.post<BaseResponse<CommonModel>>(BaseHttp.frist_index_data)
                .tag("周边信息")
                .isMultipart(true)
                .headers("token", getString("token"))
                .params("nowlat", lat)
                .params("nowlng", lng)
                .params("city", nowCity)
                .params("district", nowDistrict)
                .execute(object : JacksonDialogCallback<BaseResponse<CommonModel>>(baseContext, true) {

                    @SuppressLint("SetTextI18n")
                    override fun onSuccess(response: Response<BaseResponse<CommonModel>>) {

                        list.apply {
                            clear()
                            addItems(response.body().`object`.orders)
                        }

                        listMaker.forEach { it.destroy() }
                        listMaker.clear()

                        if (list.isNotEmpty()) {
                            list.filter { it.status == "1" }.forEach {
                                //绘制marker
                                val markerOption = MarkerOptions().position(LatLng(it.receiptLat.toDouble(), it.receiptLng.toDouble()))
                                        .icon(BitmapDescriptorFactory.fromResource(when (it.type) {
                                            "0" -> R.mipmap.index_xx02
                                            else -> R.mipmap.index_xx01
                                        }))
                                        .title("抢单")
                                        .snippet(list.indexOf(it).toString())
                                        .anchor(0.45f, 0.5f) //设置Marker的锚点
                                        .draggable(false)    //设置Marker是否可拖动
                                markerOption.isFlat = true
                                val marker = aMap.addMarker(markerOption)
                                startGrowAnimation(marker)
                                listMaker.add(marker)
                            }
                        }

                        nowCtn = response.body().`object`.nowCtn
                        cyCtn = response.body().`object`.cyCtn
                        main_grab_num.text = "有${response.body().`object`.nowCtn}个任务"
                        main_live_num.text = "有${response.body().`object`.msgCtn}条消息"
                        handler.sendEmptyMessage(0)
                    }

                })
    }

    private fun getAddressData() {
        OkGo.post<BaseResponse<ArrayList<CommonData>>>(BaseHttp.my_commonaddress_list)
                .tag(this@MainActivity)
                .headers("token", getString("token"))
                .params("type", 0)
                .execute(object : JacksonDialogCallback<BaseResponse<ArrayList<CommonData>>>(baseContext) {

                    override fun onSuccess(response: Response<BaseResponse<ArrayList<CommonData>>>) {

                        listAddress.apply {
                            clear()
                            addItems(response.body().`object`)
                        }
                        handler.sendEmptyMessage(0)
                    }

                })
    }

    private fun startGrowAnimation(marker: Marker) {
        val animation = ScaleAnimation(0f, 1f, 0f, 1f)
        animation.setInterpolator(LinearInterpolator())
        //整个移动所需要的时间
        animation.setDuration(300)
        //设置动画
        marker.setAnimation(animation)
        //开始动画
        marker.startAnimation()
    }

    private var exitTime: Long = 0
    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            if (System.currentTimeMillis() - exitTime > 2000) {
                showToast("再按一次退出程序")
                exitTime = System.currentTimeMillis()
            } else super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        main_map.onResume()
    }

    override fun onPause() {
        super.onPause()
        main_map.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        main_map.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        OkGo.getInstance().cancelTag("周边信息")
        main_map.onDestroy()
    }

    override fun finish() {
        EventBus.getDefault().unregister(this@MainActivity)
        super.finish()
    }

    @Subscribe
    fun onMessageEvent(event: RefreshMessageEvent) {
        when (event.type) {
            "新增地址", "删除地址", "支付成功", "抢单成功",
            "快速抢单", "客户取消", "客户同意", "骑手同意",
            "添加评论" -> {
                getNearData(
                        aMap.cameraPosition.target.latitude,
                        aMap.cameraPosition.target.longitude)
            }
        }
    }
}
