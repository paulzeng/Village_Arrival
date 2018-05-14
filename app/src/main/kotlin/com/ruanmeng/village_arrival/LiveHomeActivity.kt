package com.ruanmeng.village_arrival

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.jude.rollviewpager.RollPagerView
import com.lzg.extend.jackson.JacksonDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.ruanmeng.adapter.LoopAdapter
import com.ruanmeng.base.*
import com.ruanmeng.model.CommonData
import com.ruanmeng.model.CommonModel
import com.ruanmeng.model.RefreshMessageEvent
import com.ruanmeng.share.BaseHttp
import com.ruanmeng.utils.TimeHelper
import com.ruanmeng.utils.TopDecoration
import com.ruanmeng.view.FullyGridLayoutManager
import com.ruanmeng.view.SwitcherTextView
import com.sunfusheng.glideimageview.GlideImageView
import kotlinx.android.synthetic.main.layout_list.*
import kotlinx.android.synthetic.main.layout_title_task.*
import net.idik.lib.slimadapter.SlimAdapter
import net.idik.lib.slimadapter.SlimAdapterEx
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class LiveHomeActivity : BaseActivity() {

    private val list = ArrayList<Any>()
    private val listSlider = ArrayList<CommonData>()
    private val listModule = ArrayList<CommonData>()
    private val listModuleSix = ArrayList<CommonData>()
    private val listNew = ArrayList<CommonData>()

    private lateinit var banner: RollPagerView
    private lateinit var mLoopAdapter: LoopAdapter
    private lateinit var recycler: RecyclerView
    private lateinit var tvSwitcher: SwitcherTextView

    private var mCity = ""
    private var mDistrict = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_home)
        setToolbarVisibility(false)
        init_title()

        EventBus.getDefault().register(this@LiveHomeActivity)

        swipe_refresh.isRefreshing = true
        getData()
    }

    @SuppressLint("InflateParams")
    override fun init_title() {
        mCity = intent.getStringExtra("city")
        mDistrict = intent.getStringExtra("title")
        task_title.text = mDistrict
        task_right.visible()

        swipe_refresh.refresh { getData() }
        recycle_list.load_Linear(baseContext, swipe_refresh)

        mAdapterEx = SlimAdapter.create(SlimAdapterEx::class.java)
                .apply {
                    val view = LayoutInflater.from(baseContext).inflate(R.layout.header_live_home, null)
                    banner = view.findViewById(R.id.live_banner)
                    recycler = view.findViewById(R.id.live_list)
                    tvSwitcher = view.findViewById(R.id.live_switcher)

                    mLoopAdapter = LoopAdapter(baseContext, banner)
                    banner.apply {
                        setAdapter(mLoopAdapter)
                        setOnItemClickListener { /*轮播图点击事件*/ }
                    }

                    recycler.apply {
                        layoutManager = FullyGridLayoutManager(baseContext, 3).apply { setScrollEnabled(false) }
                        addItemDecoration(TopDecoration(15))

                        adapter = SlimAdapter.create()
                                .register<CommonData>(R.layout.item_live_grid) { data, injector ->
                                    injector.text(R.id.item_live_name, data.moduleName)
                                            .with<GlideImageView>(R.id.item_live_img) {
                                                if (data.moduleType != "2")
                                                    it.loadImage(BaseHttp.baseImg + data.moduleIcon, R.mipmap.default_logo)
                                                else it.setImageResource(R.mipmap.ass_icon06)
                                            }
                                            .clicked(R.id.item_live) {
                                                when (data.moduleType) {
                                                    "0" -> {
                                                        intent = Intent(baseContext, LiveContactActivity::class.java)
                                                        intent.putExtra("city", mCity)
                                                        intent.putExtra("district", mDistrict)
                                                        startActivity(intent)
                                                    }
                                                    "1" -> {
                                                        intent = Intent(baseContext, LiveActivity::class.java)
                                                        intent.putExtra("city", mCity)
                                                        intent.putExtra("district", mDistrict)
                                                        intent.putExtra("type", data.appmoduleId)
                                                        startActivity(intent)
                                                    }
                                                    "2" -> {
                                                        intent = Intent(baseContext, LiveMoreActivity::class.java)
                                                        intent.putExtra("city", mCity)
                                                        intent.putExtra("district", mDistrict)
                                                        intent.putExtra("list", listModule.filter { it.moduleType != "0" } as ArrayList<CommonData>)
                                                        startActivity(intent)
                                                    }
                                                }
                                            }
                                }
                                .attachTo(this)
                        (adapter as SlimAdapter).updateData(listModuleSix)
                    }

                    addHeader(view)
                }
                .register<CommonData>(R.layout.item_live_list) { data, injector ->
                    injector.text(R.id.item_live_title, data.title)
                            .text(R.id.item_live_area, data.district)
                            .text(R.id.item_live_comment, data.commentCtn)
                            .text(R.id.item_live_time,
                                    TimeHelper.getDiffTime(TimeHelper.getInstance().millisecondToLong(data.createDate)))
                            .gone(R.id.item_live_del)
                            .gone(R.id.item_live_cancel)
                            .gone(R.id.item_live_divider3)
                            .visibility(R.id.item_live_divider1, if (list.indexOf(data) == list.size - 1) View.GONE else View.VISIBLE)
                            .visibility(R.id.item_live_divider2, if (list.indexOf(data) != list.size - 1) View.GONE else View.VISIBLE)
                            .visibility(R.id.item_live_divider4, if (list.indexOf(data) != list.size - 1) View.GONE else View.VISIBLE)

                            .clicked(R.id.item_live) {
                                val intent = Intent(baseContext, LiveDetailActivity::class.java)
                                intent.putExtra("cooperationId", data.cooperationId)
                                startActivity(intent)
                            }
                }
                .attachTo(recycle_list)
    }

    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.bt_issue -> showSheetDialog()
            R.id.task_title -> startActivity<LiveCityActivity>()
            R.id.task_right -> {
                intent = Intent(baseContext, LiveSearchActivity::class.java)
                intent.putExtra("city", mCity)
                intent.putExtra("district", mDistrict)
                startActivity(intent)
            }
        }
    }

    override fun getData() {
        OkGo.post<CommonModel>(BaseHttp.index_data)
                .tag(this@LiveHomeActivity)
                .isMultipart(true)
                .params("city", mCity)
                .params("district", mDistrict)
                .execute(object : JacksonDialogCallback<CommonModel>(baseContext, CommonModel::class.java) {

                    override fun onSuccess(response: Response<CommonModel>) {

                        list.clear()
                        listSlider.clear()
                        listModule.clear()
                        listModuleSix.clear()
                        listNew.clear()
                        list.addItems(response.body().cooperationList)
                        listSlider.addItems(response.body().sliders)
                        listModule.addItems(response.body().modules)
                        listNew.addItems(response.body().nnews)

                        val imgs = ArrayList<String>()
                        listSlider.mapTo(imgs) { it.sliderImg }
                        mLoopAdapter.setImgs(imgs)
                        if (imgs.size < 2) {
                            banner.pause()
                            banner.setHintViewVisibility(false)
                        } else {
                            banner.resume()
                            banner.setHintViewVisibility(true)
                        }

                        if (listNew.isNotEmpty()) {
                            tvSwitcher.setData(listNew, {
                                val intent = Intent(baseContext, WebActivity::class.java)
                                intent.putExtra("title", "详情")
                                intent.putExtra("newsnoticeId", listNew[it].newsnoticeId)
                                startActivity(intent)
                            }, tvSwitcher)
                        }

                        if (listModule.size > 5) {
                            (0 until 5).forEach { listModuleSix.add(listModule[it]) }
                            listModuleSix.add(CommonData().apply {
                                moduleName = "更多服务"
                                moduleType = "2"
                            })
                        } else listModuleSix.addAll(listModule)

                        (recycler.adapter as SlimAdapter).notifyDataSetChanged()
                        mAdapterEx.updateData(list)
                    }

                    override fun onFinish() {
                        super.onFinish()
                        swipe_refresh.isRefreshing = false
                    }

                })
    }

    override fun finish() {
        EventBus.getDefault().unregister(this@LiveHomeActivity)
        super.finish()
    }

    @Suppress("DEPRECATION")
    @SuppressLint("InflateParams")
    private fun showSheetDialog() {
        val listItem = ArrayList<CommonData>()
        listModule.filter { it.moduleType == "1" }.mapTo(listItem) { it }.forEach { it.isChecked = false }

        val view = LayoutInflater.from(baseContext).inflate(R.layout.dialog_live_type, null) as View
        val tvCancel = view.findViewById(R.id.tv_dialog_live_cancle) as TextView
        val tvOk = view.findViewById(R.id.tv_dialog_live_ok) as TextView
        val recyclerView = view.findViewById(R.id.dialog_live_list) as RecyclerView
        val dialog = BottomSheetDialog(baseContext)

        tvCancel.setOnClickListener { dialog.dismiss() }
        tvOk.setOnClickListener {
            dialog.dismiss()

            window.decorView.postDelayed({
                val intent = Intent(baseContext, LiveIssueActivity::class.java)
                intent.putExtra("type", listItem.filter { it.isChecked }[0].appmoduleId)
                startActivity(intent)
            }, 350)
        }

        recyclerView.apply {
            itemAnimator = DefaultItemAnimator()
            layoutManager = LinearLayoutManager(baseContext)
            adapter = SlimAdapter.create()
                    .register<CommonData>(R.layout.item_area_list, { data, injector ->
                        injector.text(R.id.item_area, data.moduleName)
                                .textColor(R.id.item_area, resources.getColor(if (data.isChecked) R.color.colorAccent else R.color.black))
                                .gone(R.id.item_area_divider1)
                                .visible(R.id.item_area_divider2)

                                .clicked(R.id.item_area) {
                                    listItem.filter { it.isChecked }.forEach { it.isChecked = false }
                                    data.isChecked = true
                                    (adapter as SlimAdapter).notifyDataSetChanged()
                                }
                    })
                    .attachTo(recyclerView)

            (adapter as SlimAdapter).updateData(listItem)
        }

        dialog.setContentView(view)
        dialog.show()
    }

    @Subscribe
    fun onMessageEvent(event: RefreshMessageEvent) {
        when (event.type) {
            "发布需求", "添加评论" -> {
                swipe_refresh.isRefreshing = true
                getData()
            }
            "选择城市" -> {
                mDistrict = event.name
                mCity = event.id
                task_title.text = mDistrict

                swipe_refresh.isRefreshing = true
                getData()
            }
        }
    }
}
