package com.ruanmeng.village_arrival

import android.graphics.Color
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.view.View
import com.amap.api.AMapLocationHelper
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MyLocationStyle
import com.ruanmeng.base.BaseActivity
import com.ruanmeng.base.showToast
import com.ruanmeng.base.startActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

    private lateinit var aMap: AMap
    private var centerLatLng: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        transparentStatusBar(false)
        main_map.onCreate(savedInstanceState)
        init_title()

        startLocation()
    }

    override fun init_title() {
        aMap = main_map.map

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

    private fun startLocation() {
        AMapLocationHelper.getInstance(baseContext).clearCodes()
        AMapLocationHelper.getInstance(baseContext).startLocation(1) { location, isSuccessed, codes ->
            if (isSuccessed && 1 in codes) {
                centerLatLng = LatLng(location.latitude, location.longitude)
                aMap.animateCamera(CameraUpdateFactory.changeLatLng(centerLatLng))
                main_title.text = location.district
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
                    R.id.nav_live -> { }
                }
            }
        }, 300)
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
        main_map.onDestroy()
    }
}
