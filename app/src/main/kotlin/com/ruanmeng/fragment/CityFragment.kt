package com.ruanmeng.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ruanmeng.base.BaseFragment
import com.ruanmeng.base.load_Linear
import com.ruanmeng.model.CommonData
import com.ruanmeng.village_arrival.R
import kotlinx.android.synthetic.main.fragment_city.*
import net.idik.lib.slimadapter.SlimAdapter
import java.util.ArrayList

class CityFragment : BaseFragment() {

    private lateinit var list: ArrayList<CommonData>
    private var mTitle = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_city, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init_title()

        mTitle = arguments?.getString("title") ?: ""
        @Suppress("UNCHECKED_CAST")
        list = arguments?.getSerializable("list") as ArrayList<CommonData>
        mAdapter.updateData(list)
    }

    override fun init_title() {
        activity?.let { recycle_list.load_Linear(it) }

        mAdapter = SlimAdapter.create()
                .register<CommonData>(R.layout.item_problem_list) { data, injector ->
                    injector.gone(R.id.item_problem_divider3)
                            .text(R.id.item_problem_name, data.areaName)
                            .visibility(R.id.item_problem_arrow, if (mTitle == "区") View.GONE else View.VISIBLE)
                            .visibility(R.id.item_problem_divider1, if (list.indexOf(data) == list.size - 1) View.GONE else View.VISIBLE)
                            .visibility(R.id.item_problem_divider2, if (list.indexOf(data) != list.size - 1) View.GONE else View.VISIBLE)

                            .clicked(R.id.item_problem) {
                                (activity as OnFragmentItemSelectListener).onitemSelected(
                                        mTitle,
                                        data.areaId,
                                        data.areaName)
                            }
                }
                .attachTo(recycle_list)
    }
}
