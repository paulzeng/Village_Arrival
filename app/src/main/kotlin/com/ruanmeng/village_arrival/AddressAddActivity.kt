package com.ruanmeng.village_arrival

import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.view.View
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.CameraPosition
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MyLocationStyle
import com.amap.api.services.core.AMapException
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.core.PoiItem
import com.amap.api.services.poisearch.PoiResult
import com.amap.api.services.poisearch.PoiSearch
import com.lzg.extend.BaseResponse
import com.lzg.extend.jackson.JacksonDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.ruanmeng.base.BaseActivity
import com.ruanmeng.base.addItems
import com.ruanmeng.base.getString
import com.ruanmeng.base.load_Linear
import com.ruanmeng.model.CommonData
import com.ruanmeng.share.BaseHttp
import com.ruanmeng.utils.Tools
import com.ruanmeng.utils.gone
import com.ruanmeng.utils.visible
import kotlinx.android.synthetic.main.activity_address_add.*
import kotlinx.android.synthetic.main.layout_title.*
import net.idik.lib.slimadapter.SlimAdapter
import net.idik.lib.slimadapter.ex.loadmore.SimpleLoadMoreViewCreator
import net.idik.lib.slimadapter.ex.loadmore.SlimMoreLoader

class AddressAddActivity : BaseActivity() {

    private lateinit var aMap: AMap
    private var centerLatLng: LatLng? = null
    private lateinit var query: PoiSearch.Query
    private lateinit var poiSearch: PoiSearch

    private var isFirstEnter = true
    private var list = ArrayList<Any>()
    private var listAddress = ArrayList<Any>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_address_add)
        address_map.onCreate(savedInstanceState)
        init_title("我的地址")
    }

    @Suppress("DEPRECATION")
    override fun init_title() {
        super.init_title()
        nav_right.visibility = View.VISIBLE
        aMap = address_map.map

        aMap.uiSettings.apply {
            isScaleControlsEnabled = false    //比例尺
            isZoomControlsEnabled = false     //缩放按钮
            isCompassEnabled = false          //指南针
            isMyLocationButtonEnabled = false //定位按钮
            isTiltGesturesEnabled = false     //倾斜手势
            isRotateGesturesEnabled = false   //旋转手势
            setLogoBottomMargin(-50)          //隐藏logo
        }

        aMap.apply {
            isTrafficEnabled = false       //实时交通状况
            mapType = AMap.MAP_TYPE_NORMAL //矢量地图模式
            isMyLocationEnabled = true     //触发定位并显示定位层
            showIndoorMap(true)            //设置是否显示室内地图
            moveCamera(CameraUpdateFactory.zoomTo(17f)) //缩放级别

            myLocationStyle = MyLocationStyle().apply {
                //定位一次，且将视角移动到地图中心点
                myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE)
                strokeColor(Color.TRANSPARENT)     //设置定位蓝点精度圆圈的边框颜色
                radiusFillColor(Color.TRANSPARENT) //设置定位蓝点精度圆圈的填充颜色
                showMyLocation(true)               //设置是否显示定位小蓝点
                //设置定位蓝点的icon图标方法
                myLocationIcon(BitmapDescriptorFactory.fromResource(R.mipmap.gps_point))

                setOnMyLocationChangeListener {
                    centerLatLng = LatLng(it.latitude, it.longitude)
                    aMap.animateCamera(CameraUpdateFactory.changeLatLng(centerLatLng))
                }
            }

            setOnCameraChangeListener(object : AMap.OnCameraChangeListener {

                override fun onCameraChangeFinish(position: CameraPosition) {
                    if (isFirstEnter) {
                        window.decorView.postDelayed({
                            isFirstEnter = false
                            pageNum = 1
                            getData(pageNum)
                        }, 500)
                    } else {
                        pageNum = 1
                        getData(pageNum)
                    }
                }

                override fun onCameraChange(position: CameraPosition) {}

            })

            setOnMapTouchListener { address_card.gone() }
        }

        initPoiSearch()

        address_tab.apply {
            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

                override fun onTabReselected(tab: TabLayout.Tab) {}
                override fun onTabUnselected(tab: TabLayout.Tab) {}

                override fun onTabSelected(tab: TabLayout.Tab) {
                    address_card.gone()
                    tab.position
                }

            })

            addTab(this.newTab().setText("附近地址"), true)
            addTab(this.newTab().setText("常用地址"), false)

            post { Tools.setIndicator(this, 40, 40) }
        }

        address_list.load_Linear(baseContext)
        mAdapter = SlimAdapter.create()
                .register<PoiItem>(R.layout.item_map_list) { data, injector ->
                    injector.text(R.id.item_map_name, data.title)
                    injector.text(R.id.item_map_hint, data.snippet)

                            .clicked(R.id.item_map) {
                                address_card.visible()
                            }
                }
                .enableLoadMore(object : SlimMoreLoader(
                        baseContext,
                        SimpleLoadMoreViewCreator(baseContext).setNoMoreHint("没有更多数据了...")) {
                    override fun hasMore(): Boolean = true
                    override fun onLoadMore(handler: Handler) = getData(pageNum)
                })
                .attachTo(address_list)
    }

    private fun initPoiSearch() {
        query = PoiSearch.Query("", "", "")
        query.pageSize = 100
        poiSearch = PoiSearch(this, query)
        poiSearch.setOnPoiSearchListener(object : PoiSearch.OnPoiSearchListener {

            override fun onPoiItemSearched(poiItem: PoiItem, code: Int) {}

            override fun onPoiSearched(result: PoiResult?, code: Int) {
                if (code != AMapException.CODE_AMAP_SUCCESS) {
                    empty_view.visibility = View.VISIBLE
                    return
                }

                if (result != null && result.query != null) {
                    if (pageNum == 1) list.clear()
                    list.addItems(result.pois)
                    if (list.size > 0) pageNum++
                    mAdapter.updateData(list)

                    empty_view.visibility = if (list.size == 0) View.VISIBLE else View.GONE
                } else {
                    empty_view.visibility = View.VISIBLE
                }
            }

        })
    }

    override fun getData(pindex: Int) {
        query.pageNum = pindex

        val latLng = aMap.cameraPosition.target
        poiSearch.bound = PoiSearch.SearchBound(LatLonPoint(latLng.latitude, latLng.longitude), 20000)
        poiSearch.searchPOIAsyn()
    }

    private fun getAddressData() {
        OkGo.post<BaseResponse<java.util.ArrayList<CommonData>>>(BaseHttp.my_commonaddress_list)
                .tag(this@AddressAddActivity)
                .headers("token", getString("token"))
                .params("type", 0)
                .execute(object : JacksonDialogCallback<BaseResponse<java.util.ArrayList<CommonData>>>(baseContext) {

                    override fun onSuccess(response: Response<BaseResponse<java.util.ArrayList<CommonData>>>) {

                        listAddress.apply {
                            clear()
                            addItems(response.body().`object`)
                        }

                        mAdapter.updateData(listAddress)
                    }

                })
    }
}
