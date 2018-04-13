package com.ruanmeng.village_arrival

import android.os.Bundle
import android.view.View
import com.ruanmeng.adapter.AddressAdapter
import com.ruanmeng.base.BaseActivity
import com.ruanmeng.base.load_Linear
import com.ruanmeng.base.startActivity
import com.ruanmeng.model.CommonData
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

//        list.add(CommonData("1"))
//        list.add(CommonData("2"))
//        list.add(CommonData("3"))
//        adapter.notifyDataSetChanged()

        empty_view.visibility = if (list.size > 0) View.GONE else View.VISIBLE
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
}
