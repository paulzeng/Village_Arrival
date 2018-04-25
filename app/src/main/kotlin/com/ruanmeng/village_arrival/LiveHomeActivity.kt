package com.ruanmeng.village_arrival

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.allen.library.SuperTextView
import com.jude.rollviewpager.RollPagerView
import com.lzg.extend.jackson.JacksonDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.ruanmeng.adapter.LoopAdapter
import com.ruanmeng.base.*
import com.ruanmeng.model.CommonData
import com.ruanmeng.model.CommonModel
import com.ruanmeng.share.BaseHttp
import com.ruanmeng.utils.TimeHelper
import com.ruanmeng.utils.TopDecoration
import com.ruanmeng.view.FullyGridLayoutManager
import com.ruanmeng.view.SwitcherTextView
import com.sunfusheng.glideimageview.GlideImageView
import kotlinx.android.synthetic.main.layout_list.*
import net.idik.lib.slimadapter.SlimAdapter
import net.idik.lib.slimadapter.SlimAdapterEx

class LiveHomeActivity : BaseActivity() {

    private val list = ArrayList<Any>()
    private val listSlider = ArrayList<CommonData>()
    private val listModule = ArrayList<Any>()
    private val listNew = ArrayList<CommonData>()

    private lateinit var banner: RollPagerView
    private lateinit var mLoopAdapter: LoopAdapter
    private lateinit var recycler: RecyclerView
    private lateinit var tvSwitcher: SwitcherTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_home)
        transparentStatusBar(false)
        init_title()

        swipe_refresh.isRefreshing = true
        getData()
    }

    @SuppressLint("InflateParams")
    override fun init_title() {
        swipe_refresh.refresh { getData() }
        recycle_list.load_Linear(baseContext, swipe_refresh)

        mAdapterEx = SlimAdapter.create(SlimAdapterEx::class.java)
                .apply {
                    val view = LayoutInflater.from(baseContext).inflate(R.layout.header_live_home, null)
                    val tvTitle = view.findViewById<TextView>(R.id.live_title)
                    banner = view.findViewById(R.id.live_banner)
                    recycler = view.findViewById(R.id.live_list)
                    tvSwitcher = view.findViewById(R.id.live_switcher)
                    val tvRecommand = view.findViewById<SuperTextView>(R.id.live_rec)

                    tvTitle.text = intent.getStringExtra("title")
                    mLoopAdapter = LoopAdapter(baseContext, banner)
                    banner.apply {
                        setAdapter(mLoopAdapter)
                        setOnItemClickListener { position -> /*轮播图点击事件*/ }
                    }

                    recycler.apply {
                        layoutManager = FullyGridLayoutManager(baseContext, 3)
                        addItemDecoration(TopDecoration(15))

                        adapter = SlimAdapter.create()
                                .register<CommonData>(R.layout.item_live_grid) { data, injector ->
                                    injector.text(R.id.item_live_name, data.moduleName)
                                            .with<GlideImageView>(R.id.item_live_img) {
                                                it.loadImage(BaseHttp.baseImg + data.moduleIcon)
                                            }
                                            .clicked(R.id.item_live) {}
                                }
                                .attachTo(this)
                        (adapter as SlimAdapter).updateData(listModule)
                    }

                    tvRecommand.setOnClickListener {
                        intent.setClass(baseContext, LiveActivity::class.java)
                        startActivity(intent)
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
            R.id.bt_issue -> startActivity<LiveIssueActivity>()
        }
    }

    override fun getData() {
        OkGo.post<CommonModel>(BaseHttp.index_data)
                .tag(this@LiveHomeActivity)
                .execute(object : JacksonDialogCallback<CommonModel>(baseContext, CommonModel::class.java) {

                    override fun onSuccess(response: Response<CommonModel>) {

                        list.clear()
                        listSlider.clear()
                        listModule.clear()
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

                        (recycler.adapter as SlimAdapter).notifyDataSetChanged()
                        mAdapterEx.updateData(list)
                    }

                    override fun onFinish() {
                        super.onFinish()
                        swipe_refresh.isRefreshing = false
                    }

                })
    }
}
