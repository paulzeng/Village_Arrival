package com.ruanmeng.village_arrival

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import com.amap.api.services.core.AMapException
import com.amap.api.services.core.PoiItem
import com.amap.api.services.poisearch.PoiResult
import com.amap.api.services.poisearch.PoiSearch
import com.ruanmeng.base.*
import com.ruanmeng.model.LocationMessageEvent
import com.ruanmeng.utils.ActivityStack
import com.ruanmeng.utils.KeyboardHelper
import kotlinx.android.synthetic.main.activity_address_search.*
import kotlinx.android.synthetic.main.layout_title_search.*
import net.idik.lib.slimadapter.SlimAdapter
import net.idik.lib.slimadapter.ex.loadmore.SimpleLoadMoreViewCreator
import net.idik.lib.slimadapter.ex.loadmore.SlimMoreLoader
import org.greenrobot.eventbus.EventBus

class AddressSearchActivity : BaseActivity() {

    private var list = ArrayList<Any>()
    private lateinit var query: PoiSearch.Query
    private lateinit var poiSearch: PoiSearch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_address_search)
        setToolbarVisibility(false)
        init_title()

        search_edit.requestFocus()
    }

    override fun init_title() {
        address_list.load_Linear(baseContext)
        mAdapter = SlimAdapter.create()
                .register<PoiItem>(R.layout.item_map_list) { data, injector ->
                    injector.text(R.id.item_map_name, "（${data.cityName}）${data.title}")
                    injector.text(R.id.item_map_hint, data.snippet)

                            .clicked(R.id.item_map) {
                                EventBus.getDefault().post(LocationMessageEvent(
                                        intent.getStringExtra("title"),
                                        "",
                                        data.title, data.snippet,
                                        "", "",
                                        data.latLonPoint.latitude.toString(),
                                        data.latLonPoint.longitude.toString()))
                                ActivityStack.screenManager.popActivities(this@AddressSearchActivity::class.java)
                            }
                }
                .enableLoadMore(object : SlimMoreLoader(
                        baseContext,
                        SimpleLoadMoreViewCreator(baseContext)
                                .setLoadingHint("正在加载...")
                                .setNoMoreHint("没有更多数据了")
                                .setPullToLoadMoreHint("加载更多")
                                .setErrorHint("加载失败")) {
                    override fun hasMore(): Boolean = search_edit.text.isNotEmpty()
                    override fun onLoadMore(handler: Handler) = getData(pageNum)
                })
                .attachTo(address_list)

        query = PoiSearch.Query(
                "",
                "",
                intent.getStringExtra("city"))
        poiSearch = PoiSearch(this, query)
        poiSearch.setOnPoiSearchListener(object : PoiSearch.OnPoiSearchListener {

            override fun onPoiItemSearched(poiItem: PoiItem, code: Int) {}

            override fun onPoiSearched(result: PoiResult?, code: Int) {
                cancelLoadingDialog()
                if (code != AMapException.CODE_AMAP_SUCCESS) return

                if (result != null && result.query != null) {
                    if (pageNum == 1) {
                        if (list.isNotEmpty()) address_list.scrollToPosition(0)
                        list.clear()
                    }
                    if (result.pois.isNotEmpty()) pageNum++
                    list.addItems(result.pois)
                    mAdapter.updateData(list)

                    empty_view.apply { if (list.isEmpty()) visible() else gone() }
                }
            }

        })

        search_edit.addTextChangedListener(this@AddressSearchActivity)
        search_edit.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                KeyboardHelper.hideSoftInput(baseContext) //隐藏软键盘

                if (search_edit.text.isBlank()) showToast("请输入关键字")
                else {
                    empty_view.gone()
                    if (list.isNotEmpty()) {
                        list.clear()
                        mAdapter.notifyDataSetChanged()
                    }

                    query = PoiSearch.Query(
                            search_edit.text.toString(),
                            "",
                            intent.getStringExtra("city"))
                    poiSearch.query = query

                    pageNum = 1
                    getData(pageNum)
                }
            }
            return@setOnEditorActionListener false
        }

        search_cancel.setOnClickListener { search_edit.setText("") }
    }

    override fun getData(pindex: Int) {
        if (pindex == 1) showLoadingDialog()
        query.pageSize = 20
        query.pageNum = pindex
        poiSearch.searchPOIAsyn()
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (s.isEmpty() && list.isNotEmpty()) {
            list.clear()
            mAdapter.notifyDataSetChanged()
        }
    }
}
