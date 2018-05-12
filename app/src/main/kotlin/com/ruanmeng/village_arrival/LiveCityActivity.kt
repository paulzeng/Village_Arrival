package com.ruanmeng.village_arrival

import android.os.Bundle
import com.lzg.extend.BaseResponse
import com.lzg.extend.jackson.JacksonDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.ruanmeng.base.BaseActivity
import com.ruanmeng.base.addItems
import com.ruanmeng.fragment.CityFragment
import com.ruanmeng.fragment.OnFragmentItemSelectListener
import com.ruanmeng.model.CommonData
import com.ruanmeng.model.RefreshMessageEvent
import com.ruanmeng.share.BaseHttp
import com.ruanmeng.utils.ActivityStack
import org.greenrobot.eventbus.EventBus

class LiveCityActivity : BaseActivity(), OnFragmentItemSelectListener {

    private var listProvince = ArrayList<CommonData>()
    private var listCity = ArrayList<CommonData>()
    private var listDistrict = ArrayList<CommonData>()

    private lateinit var first: CityFragment
    private lateinit var second: CityFragment
    private lateinit var third: CityFragment

    private var mProvince = ""
    private var mCity = ""
    private var mDistrict = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_city)
        init_title("选择省份")

        supportFragmentManager.addOnBackStackChangedListener {
            tvTitle.text = when (supportFragmentManager.backStackEntryCount) {
                2 -> "选择区域"
                1 -> "选择城市"
                else -> "选择省份"
            }
        }

        getProvince()
    }

    override fun onitemSelected(type: String, id: String, name: String) {
        when (type) {
            "省" -> {
                mProvince = name
                getCity()
            }
            "市" -> {
                mCity = name
                getDistrict()
            }
            "区" -> {
                mDistrict = name

                EventBus.getDefault().post(RefreshMessageEvent(
                        "选择城市",
                        mCity,
                        mDistrict))

                ActivityStack.screenManager.popActivities(this@LiveCityActivity::class.java)
            }
        }
    }

    private fun getProvince() {
        OkGo.post<BaseResponse<ArrayList<CommonData>>>(BaseHttp.city_name_data)
                .tag(this@LiveCityActivity)
                .params("areaLevel", "country")
                .execute(object : JacksonDialogCallback<BaseResponse<ArrayList<CommonData>>>(baseContext, true) {

                    override fun onSuccess(response: Response<BaseResponse<ArrayList<CommonData>>>) {
                        listProvince.apply {
                            clear()
                            addItems(response.body().`object`)
                        }

                        if (listProvince.isNotEmpty()) {

                            first = CityFragment()
                            first.arguments = Bundle().apply {
                                putSerializable("list", listProvince)
                                putString("title", "省")
                            }

                            supportFragmentManager
                                    .beginTransaction()
                                    .add(R.id.city_container, first)
                                    .commit()
                        }
                    }

                })
    }

    private fun getCity() {
        OkGo.post<BaseResponse<ArrayList<CommonData>>>(BaseHttp.city_name_data)
                .tag(this@LiveCityActivity)
                .isMultipart(true)
                .params("areaLevel", "province")
                .params("areaName", mProvince)
                .execute(object : JacksonDialogCallback<BaseResponse<ArrayList<CommonData>>>(baseContext, true) {

                    override fun onSuccess(response: Response<BaseResponse<ArrayList<CommonData>>>) {
                        listCity.apply {
                            clear()
                            addItems(response.body().`object`)
                        }

                        if (listCity.isNotEmpty()) {
                            tvTitle.text = "选择城市"
                            second = CityFragment()
                            second.arguments = Bundle().apply {
                                putSerializable("list", listCity)
                                putString("title", "市")
                            }

                            supportFragmentManager.beginTransaction()
                                    .setCustomAnimations(
                                            R.anim.push_left_in,
                                            R.anim.push_left_out,
                                            R.anim.push_right_in,
                                            R.anim.push_right_out)
                                    .replace(R.id.city_container, second)
                                    .addToBackStack(null)
                                    .commit()
                        }
                    }

                })
    }

    private fun getDistrict() {
        OkGo.post<BaseResponse<ArrayList<CommonData>>>(BaseHttp.city_name_data)
                .tag(this@LiveCityActivity)
                .isMultipart(true)
                .params("areaLevel", "city")
                .params("areaName", mCity)
                .execute(object : JacksonDialogCallback<BaseResponse<ArrayList<CommonData>>>(baseContext, true) {

                    override fun onSuccess(response: Response<BaseResponse<ArrayList<CommonData>>>) {
                        listDistrict.apply {
                            clear()
                            addItems(response.body().`object`)
                        }

                        if (listDistrict.isNotEmpty()) {
                            tvTitle.text = "选择区域"
                            third = CityFragment()
                            third.arguments = Bundle().apply {
                                putSerializable("list", listDistrict)
                                putString("title", "区")
                            }

                            supportFragmentManager.beginTransaction()
                                    .setCustomAnimations(
                                            R.anim.push_left_in,
                                            R.anim.push_left_out,
                                            R.anim.push_right_in,
                                            R.anim.push_right_out)
                                    .replace(R.id.city_container, third)
                                    .addToBackStack(null)
                                    .commit()
                        }
                    }

                })
    }
}
