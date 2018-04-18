package com.ruanmeng.village_arrival

import android.os.Bundle
import android.view.View
import com.lzg.extend.BaseResponse
import com.lzg.extend.jackson.JacksonDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.ruanmeng.adapter.AddressAdapter
import com.ruanmeng.base.*
import com.ruanmeng.model.CommonData
import com.ruanmeng.share.BaseHttp
import kotlinx.android.synthetic.main.activity_address.*
import kotlinx.android.synthetic.main.layout_empty_addr.*
import java.util.ArrayList

class AddressActivity : BaseActivity() {

    private val list = ArrayList<CommonData>()
    private lateinit var adapter: AddressAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_address)
        init_title("常用地址")

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
        address_list.adapter = adapter
    }

    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.empty_add -> startActivity<AddressAddActivity>()
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
                        if (list.isNotEmpty()) mAdapter.updateData(list)
                    }

                    override fun onFinish() {
                        super.onFinish()

                        empty_view.visibility = if (list.size > 0) View.GONE else View.VISIBLE
                    }

                })
    }
}
