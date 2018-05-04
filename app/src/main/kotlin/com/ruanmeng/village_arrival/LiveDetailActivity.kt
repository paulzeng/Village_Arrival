package com.ruanmeng.village_arrival

import android.os.Bundle
import android.view.View
import com.lzg.extend.BaseResponse
import com.lzg.extend.StringDialogCallback
import com.lzg.extend.jackson.JacksonDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.maning.imagebrowserlibrary.MNImageBrowser
import com.ruanmeng.base.*
import com.ruanmeng.model.CommonData
import com.ruanmeng.model.CommonModel
import com.ruanmeng.model.RefreshMessageEvent
import com.ruanmeng.share.BaseHttp
import com.ruanmeng.utils.TimeHelper
import com.ruanmeng.utils.Tools
import com.sunfusheng.glideimageview.GlideImageView
import kotlinx.android.synthetic.main.activity_live_detail.*
import kotlinx.android.synthetic.main.header_live.*
import kotlinx.android.synthetic.main.layout_list.*
import net.idik.lib.slimadapter.SlimAdapter
import net.idik.lib.slimadapter.SlimAdapterEx
import org.greenrobot.eventbus.EventBus

class LiveDetailActivity : BaseActivity() {

    private val list = ArrayList<Any>()
    private var collectStatus = ""
    private var imgs = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_detail)
        init_title("生活互助详情")

        swipe_refresh.isRefreshing = false
        getData(pageNum)
        getData()
    }

    override fun init_title() {
        super.init_title()
        ivRight.visibility = View.VISIBLE

        swipe_refresh.refresh { getData(1) }
        recycle_list.load_Linear(baseContext, swipe_refresh) {
            if (!isLoadingMore) {
                isLoadingMore = true
                getData(pageNum)
            }
        }

        mAdapterEx = SlimAdapter.create(SlimAdapterEx::class.java)
                .addHeader(baseContext, R.layout.header_live)
                .register<CommonData>(R.layout.item_live_detail) { data, injector ->
                    injector.text(R.id.item_live_title, Tools.decodeUnicode(data.nickName))
                            .text(R.id.item_live_time, TimeHelper.getDiffTime(TimeHelper.getInstance().millisecondToLong(data.createDate)))
                            .text(R.id.item_live_content, Tools.decodeUnicode(data.content))

                            .with<GlideImageView>(R.id.item_live_img) {
                                it.loadImage(BaseHttp.baseImg + data.userhead)
                            }

                            .visibility(R.id.item_live_divider1, if (list.indexOf(data) == list.size - 1) View.GONE else View.VISIBLE)
                            .visibility(R.id.item_live_divider2, if (list.indexOf(data) != list.size - 1) View.GONE else View.VISIBLE)
                            .visibility(R.id.item_live_divider3, if (list.indexOf(data) != 0) View.GONE else View.VISIBLE)
                }
                .attachTo(recycle_list)
    }

    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.live_img -> MNImageBrowser.showImageBrowser(
                    baseContext,
                    live_img,
                    0,
                    arrayListOf(BaseHttp.baseImg + imgs))
            R.id.iv_nav_right -> {
                if (collectStatus == "1") {
                    OkGo.post<String>(BaseHttp.delete_collect_cooperatio)
                            .tag(this@LiveDetailActivity)
                            .headers("token", getString("token"))
                            .params("cooperationId", intent.getStringExtra("cooperationId"))
                            .execute(object : StringDialogCallback(baseContext) {

                                override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                                    showToast(msg)
                                    collectStatus = "0"
                                    ivRight.setImageResource(R.mipmap.ass_check_icon02)
                                    EventBus.getDefault().post(RefreshMessageEvent("取消收藏"))
                                }

                            })
                } else {
                    OkGo.post<String>(BaseHttp.cooperation_collect)
                            .tag(this@LiveDetailActivity)
                            .headers("token", getString("token"))
                            .params("cooperationId", intent.getStringExtra("cooperationId"))
                            .execute(object : StringDialogCallback(baseContext) {

                                override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                                    showToast(msg)
                                    collectStatus = "1"
                                    ivRight.setImageResource(R.mipmap.ass_check_icon01)
                                    EventBus.getDefault().post(RefreshMessageEvent("收藏成功"))
                                }

                            })
                }
            }
            R.id.live_send -> {
                if (live_input.text.trim().isEmpty()) {
                    showToast("请输入回复内容")
                    return
                }

                OkGo.post<String>(BaseHttp.add_cooperation_collect)
                        .tag(this@LiveDetailActivity)
                        .isMultipart(true)
                        .headers("token", getString("token"))
                        .params("cooperationId", intent.getStringExtra("cooperationId"))
                        .params("content", live_input.text.trim().toString())
                        .execute(object : StringDialogCallback(baseContext) {

                            override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                                showToast(msg)
                                live_input.setText("")
                                ivRight.setImageResource(R.mipmap.ass_check_icon01)
                                EventBus.getDefault().post(RefreshMessageEvent("添加评论"))

                                val commentCtn = live_comment.text.toString().toInt()
                                live_comment.text = (commentCtn + 1).toString()
                                updateList()
                            }

                        })
            }
        }
    }

    override fun getData() {
        OkGo.post<BaseResponse<CommonModel>>(BaseHttp.cooperation_detail)
                .tag(this@LiveDetailActivity)
                .headers("token", getString("token"))
                .params("cooperationId", intent.getStringExtra("cooperationId"))
                .execute(object : JacksonDialogCallback<BaseResponse<CommonModel>>(baseContext) {

                    override fun onSuccess(response: Response<BaseResponse<CommonModel>>) {
                        val data = response.body().`object`.details
                        collectStatus = response.body().`object`.collectCtn

                        ivRight.setImageResource(if (collectStatus == "1") R.mipmap.ass_check_icon01 else R.mipmap.ass_check_icon02)
                        live_title.text = data.title
                        live_content.setUnicodeText(data.content)
                        live_time.text = TimeHelper.getDiffTime(TimeHelper.getInstance().millisecondToLong(data.createDate))
                        live_comment.text = if (data.commentCtn.isEmpty()) "0" else data.commentCtn
                        live_area.text = data.district
                        live_name.text = data.name
                        live_tel.text = data.mobile

                        if (data.name.isEmpty()) live_name.gone()
                        if (data.mobile.isEmpty()) live_tel.gone()
                        if (data.imgs.isEmpty()) live_img.gone()
                        else {
                            imgs = data.imgs
                            live_img.setImageURL(BaseHttp.baseImg + imgs, R.mipmap.default_logo)
                        }
                    }

                })
    }

    override fun getData(pindex: Int) {
        OkGo.post<BaseResponse<ArrayList<CommonData>>>(BaseHttp.comment_list)
                .tag(this@LiveDetailActivity)
                .headers("token", getString("token"))
                .params("cooperationId", intent.getStringExtra("cooperationId"))
                .params("page", pindex)
                .execute(object : JacksonDialogCallback<BaseResponse<ArrayList<CommonData>>>(baseContext) {

                    override fun onSuccess(response: Response<BaseResponse<ArrayList<CommonData>>>) {

                        list.apply {
                            if (pindex == 1) {
                                clear()
                                pageNum = pindex
                            }
                            addItems(response.body().`object`)
                            if (count(response.body().`object`) > 0) pageNum++
                        }

                        if (count(response.body().`object`) > 0) mAdapterEx.updateData(list)
                    }

                    override fun onFinish() {
                        super.onFinish()
                        swipe_refresh.isRefreshing = false
                        isLoadingMore = false
                    }

                })
    }

    fun updateList() {
        swipe_refresh.isRefreshing = true

        if (list.isNotEmpty()) {
            list.clear()
            mAdapterEx.notifyDataSetChanged()
        }

        pageNum = 1
        getData(pageNum)
    }
}
