package com.ruanmeng.village_arrival

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.amap.api.AMapLocationHelper
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MyLocationStyle
import com.flyco.animation.FadeEnter.FadeEnter
import com.flyco.animation.FadeExit.FadeExit
import com.flyco.dialog.widget.popup.BubblePopup
import com.github.library.bubbleview.BubbleLinearLayout
import com.lzg.extend.StringDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.ruanmeng.base.*
import com.ruanmeng.share.BaseHttp
import com.ruanmeng.utils.CommonUtil
import com.ruanmeng.utils.DensityUtil
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject

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

    override fun onStart() {
        super.onStart()
        getPersonData()
    }

    override fun init_title() {
        aMap = main_map.map

        aMap.myLocationStyle = MyLocationStyle().apply {
            myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW_NO_CENTER) //连续定位、蓝点不会移动到地图中心点，并且蓝点会跟随设备移动
            interval(5000)
            strokeColor(Color.TRANSPARENT)
            radiusFillColor(Color.TRANSPARENT)
            myLocationIcon(BitmapDescriptorFactory.fromResource(R.mipmap.index_pos))
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

    @Suppress("DEPRECATION")
    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.main_msg -> startActivity<MessageActivity>()
            R.id.main_grab -> startActivity<TaskGrabActivity>()
            R.id.main_live -> startActivity<LiveActivity>()
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

                        val obj = JSONObject(response.body()).getJSONObject("userMsg") ?: JSONObject()
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
                    }

                })
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
