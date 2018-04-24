package com.ruanmeng.village_arrival

import android.os.Bundle
import android.view.View
import com.flyco.dialog.widget.ActionSheetDialog
import com.lzg.extend.BaseResponse
import com.lzg.extend.StringDialogCallback
import com.lzg.extend.jackson.JacksonDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.ruanmeng.base.*
import com.ruanmeng.model.CommonData
import com.ruanmeng.share.BaseHttp
import com.ruanmeng.utils.ActivityStack
import kotlinx.android.synthetic.main.activity_apply.*
import org.json.JSONObject
import java.util.ArrayList

class ApplyActivity : BaseActivity() {

    private val list = ArrayList<CommonData>()
    private var commonAddressId = ""
    private var mStatus = ""
    private var mPass = ""
    private var mSex = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apply)
        init_title("立即抢单")

        getData()
    }

    override fun onStart() {
        super.onStart()
        apply_real.setRightString(when (getString("pass")) {
            "-1" -> "认证中"
            "0" -> "未通过"
            "1" -> "已认证"
            else -> "未认证"
        })

        getAddressData()
    }

    @Suppress("DEPRECATION")
    override fun init_title() {
        super.init_title()
        apply_phone.setRightString(getString("mobile"))

        apply_gender.setOnClickListener {
            val dialog = ActionSheetDialog(this, arrayOf("男", "女"), null)
            dialog.isTitleShow(false)
                    .lvBgColor(resources.getColor(R.color.white))
                    .dividerColor(resources.getColor(R.color.divider))
                    .dividerHeight(0.5f)
                    .itemTextColor(resources.getColor(R.color.black))
                    .itemHeight(40f)
                    .itemTextSize(15f)
                    .cancelText(resources.getColor(R.color.light))
                    .cancelTextSize(15f)
                    .layoutAnimation(null)
                    .show()
            dialog.setOnOperItemClickL { _, _, position, _ ->
                dialog.dismiss()

                apply_gender.setRightString(if (position == 0) "男" else "女")
                mSex = if (position == 0) "1" else "0"
            }
        }
        apply_real.setOnClickListener { startActivity<RealActivity>() }
        apply_addr.setOnClickListener { startActivity<AddressActivity>() }
    }

    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.bt_apply -> {
                if (mPass != "1") {
                    showToast("未通过实名认证，无法提交申请")
                    return
                }
                if (commonAddressId.isEmpty()) {
                    showToast("未添加常用地址，无法提交申请")
                    return
                }

                OkGo.post<String>(BaseHttp.add_applyodd)
                        .tag(this@ApplyActivity)
                        .headers("token", getString("token"))
                        .params("commonAddressId", commonAddressId)
                        .params("moblie", getString("mobile"))
                        .params("sex", mSex)
                        .execute(object : StringDialogCallback(baseContext) {

                            override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                                showToast(msg)
                                putString("status", "-1")
                                ActivityStack.screenManager.popActivities(this@ApplyActivity::class.java)
                            }

                        })
            }
        }
    }
    
    override fun getData() {
        OkGo.post<String>(BaseHttp.applyodd_data)
                .tag(this@ApplyActivity)
                .headers("token", getString("token"))
                .execute(object : StringDialogCallback(baseContext) {

                    override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                        val obj = JSONObject(response.body())
                        val reason = obj.optString("reason")
                        mSex = obj.optString("sex")
                        mPass = obj.optString("pass")
                        mStatus = obj.optString("status")
                        if (mSex.isEmpty()) mSex = "1"

                        apply_gender.setRightString(if (mSex == "0") "女" else "男")
                        apply_real.setRightString(when (mPass) {
                            "-1" -> "认证中"
                            "0" -> "未通过"
                            "1" -> "已认证"
                            else -> "未认证"
                        })

                        when (mStatus) {
                            "-1" -> apply_expand.expand()
                            "0" -> {
                                apply_expand.expand()
                                apply_hint.text = if (reason.isNotEmpty()) "审核失败" else "审核失败：$reason"
                                bt_apply.visible()
                                bt_apply.text = "重新申请"
                            }
                            "1" -> {
                                apply_expand.expand()
                                apply_hint.text = "抢单申请审核成功"
                            }
                            else -> {
                                apply_expand.collapse()
                                bt_apply.visible()
                            }
                        }
                    }

                })
    }

    private fun getAddressData() {
        OkGo.post<BaseResponse<ArrayList<CommonData>>>(BaseHttp.my_commonaddress_list)
                .tag(this@ApplyActivity)
                .headers("token", getString("token"))
                .params("type", 0)
                .execute(object : JacksonDialogCallback<BaseResponse<ArrayList<CommonData>>>(baseContext) {

                    override fun onSuccess(response: Response<BaseResponse<ArrayList<CommonData>>>) {

                        list.apply {
                            clear()
                            addItems(response.body().`object`)
                        }

                        if (list.isEmpty())  apply_addr.setRightString("未添加")
                        else {
                            apply_addr.setRightString("已添加")
                            commonAddressId = list[0].commonAddressId
                        }
                    }

                })
    }
}
