package com.ruanmeng.village_arrival

import android.os.Bundle
import com.ruanmeng.base.BaseActivity
import com.ruanmeng.base.loadImage
import com.ruanmeng.base.startActivity
import com.ruanmeng.model.CommonData
import com.ruanmeng.share.BaseHttp
import com.ruanmeng.utils.TopDecoration
import com.ruanmeng.view.FullyGridLayoutManager
import com.sunfusheng.glideimageview.GlideImageView
import kotlinx.android.synthetic.main.activity_live_more.*
import net.idik.lib.slimadapter.SlimAdapter
import java.util.ArrayList

class LiveMoreActivity : BaseActivity() {

    private lateinit var list: ArrayList<CommonData>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_more)
        init_title("更多服务")

        @Suppress("UNCHECKED_CAST")
        list = intent.getSerializableExtra("list") as ArrayList<CommonData>
        mAdapter.updateData(list)
    }

    override fun init_title() {
        super.init_title()
        more_list.apply {
            layoutManager = FullyGridLayoutManager(baseContext, 3).apply { setScrollEnabled(false) }
            addItemDecoration(TopDecoration(15))

            mAdapter = SlimAdapter.create()
                    .register<CommonData>(R.layout.item_live_grid) { data, injector ->
                        injector.text(R.id.item_live_name, data.moduleName)
                                .with<GlideImageView>(R.id.item_live_img) {
                                    it.loadImage(BaseHttp.baseImg + data.moduleIcon)
                                }
                                .clicked(R.id.item_live) {
                                    when (data.moduleType) {
                                        "0" -> startActivity<LiveContactActivity>()
                                        "1" -> {
                                            intent.setClass(baseContext, LiveActivity::class.java)
                                            intent.putExtra("type", data.appmoduleId)
                                            startActivity(intent)
                                        }
                                    }
                                }
                    }
                    .attachTo(this)
        }
    }
}
