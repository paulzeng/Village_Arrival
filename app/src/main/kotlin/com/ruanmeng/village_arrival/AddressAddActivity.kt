package com.ruanmeng.village_arrival

import android.graphics.Color
import android.os.Bundle
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MyLocationStyle
import com.ruanmeng.base.BaseActivity
import kotlinx.android.synthetic.main.activity_address_add.*

class AddressAddActivity : BaseActivity() {

    private lateinit var aMap: AMap
    private var centerLatLng: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_address_add)
        address_map.onCreate(savedInstanceState)
        init_title("我的地址", "确认")
    }

    override fun init_title() {
        super.init_title()
        aMap = address_map.map

        aMap.myLocationStyle = MyLocationStyle().apply {
            myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW_NO_CENTER) //连续定位、蓝点不会移动到地图中心点，并且蓝点会跟随设备移动
            interval(5000)
            strokeColor(Color.TRANSPARENT)
            radiusFillColor(Color.TRANSPARENT)
        }

        aMap.isTrafficEnabled = false       //实时交通状况
        aMap.mapType = AMap.MAP_TYPE_NORMAL //矢量地图模式
        aMap.isMyLocationEnabled = true     //触发定位并显示定位层
        aMap.moveCamera(CameraUpdateFactory.zoomTo(16f)) //缩放级别

        val mUiSettings = aMap.uiSettings
        mUiSettings.isScaleControlsEnabled = false    //比例尺
        mUiSettings.isZoomControlsEnabled = false     //缩放按钮
        mUiSettings.isCompassEnabled = false          //指南针
        mUiSettings.isMyLocationButtonEnabled = false //定位按钮
        mUiSettings.isTiltGesturesEnabled = false     //倾斜手势
        mUiSettings.isRotateGesturesEnabled = false   //旋转手势
        mUiSettings.setLogoBottomMargin(-50)          //隐藏logo
    }
}
