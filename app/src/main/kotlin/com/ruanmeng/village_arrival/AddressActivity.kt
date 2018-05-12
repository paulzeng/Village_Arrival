package com.ruanmeng.village_arrival

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.lzg.extend.BaseResponse
import com.lzg.extend.StringDialogCallback
import com.lzg.extend.jackson.JacksonDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.ruanmeng.adapter.AddressAdapter
import com.ruanmeng.base.*
import com.ruanmeng.model.CommonData
import com.ruanmeng.model.RefreshMessageEvent
import com.ruanmeng.share.BaseHttp
import com.ruanmeng.utils.DialogHelper
import kotlinx.android.synthetic.main.activity_address.*
import kotlinx.android.synthetic.main.layout_empty_addr.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.ArrayList

class AddressActivity : BaseActivity() {

    private val list = ArrayList<CommonData>()
    private lateinit var adapter: AddressAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_address)
        init_title("常用地址")

        EventBus.getDefault().register(this@AddressActivity)

        getData()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // Only if you need to restore open/close state when
        // the orientation is changed
        adapter.saveStates(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        // Only if you need to restore open/close state when
        // the orientation is changed
        adapter.restoreStates(savedInstanceState)
    }

    override fun init_title() {
        super.init_title()
        address_list.load_Linear(baseContext)
        adapter = AddressAdapter(baseContext, list)
        adapter.setOnItemClickListener {
            val intent = Intent(baseContext, AddressAddActivity::class.java)
            intent.putExtra("title", "我的地址")
            startActivity(intent)
        }
        adapter.setOnItemDeleteClickListener { position ->

            DialogHelper.showHintDialog(baseContext,
                    "删除地址",
                    getString(R.string.del_address),
                    "取消",
                    "确定",
                    false) {
                if (it == "right") {
                    OkGo.post<String>(BaseHttp.delete_commonaddress)
                            .tag(this@AddressActivity)
                            .headers("token", getString("token"))
                            .params("commonAddressId", list[position].commonAddressId)
                            .execute(object : StringDialogCallback(baseContext) {

                                override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                                    showToast(msg)
                                    list.removeAt(position)
                                    adapter.notifyItemRemoved(position)
                                    empty_view.apply { if (list.isNotEmpty()) gone() else visible() }
                                    EventBus.getDefault().post(RefreshMessageEvent("删除地址"))
                                }

                            })
                }
            }
        }
        address_list.adapter = adapter
    }

    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.empty_add -> {
                val intent = Intent(baseContext, AddressAddActivity::class.java)
                intent.putExtra("title", "我的地址")
                startActivity(intent)
            }
        }
    }

    override fun getData() {
        OkGo.post<BaseResponse<ArrayList<CommonData>>>(BaseHttp.my_commonaddress_list)
                .tag(this@AddressActivity)
                .headers("token", getString("token"))
                .params("type", 0)
                .execute(object : JacksonDialogCallback<BaseResponse<ArrayList<CommonData>>>(baseContext, true) {

                    override fun onSuccess(response: Response<BaseResponse<ArrayList<CommonData>>>) {

                        list.apply {
                            clear()
                            addItems(response.body().`object`)
                        }
                        if (list.isNotEmpty()) adapter.notifyDataSetChanged()
                    }

                    override fun onFinish() {
                        super.onFinish()
                        empty_view.apply { if (list.isNotEmpty()) gone() else visible() }
                    }

                })
    }

    override fun finish() {
        EventBus.getDefault().unregister(this@AddressActivity)
        super.finish()
    }

    @Subscribe
    fun onMessageEvent(event: RefreshMessageEvent) {
        when (event.type) {
            "新增地址" -> {
                empty_view.gone()
                getData()
            }
        }
    }
}
