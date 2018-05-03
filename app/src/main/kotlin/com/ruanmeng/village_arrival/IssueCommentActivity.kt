package com.ruanmeng.village_arrival

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.lzg.extend.StringDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.ruanmeng.base.BaseActivity
import com.ruanmeng.base.getString
import com.ruanmeng.base.loadImage
import com.ruanmeng.base.showToast
import com.ruanmeng.model.RefreshMessageEvent
import com.ruanmeng.share.BaseHttp
import com.ruanmeng.utils.ActivityStack
import com.ruanmeng.utils.DialogHelper
import kotlinx.android.synthetic.main.activity_issue_comment.*
import org.greenrobot.eventbus.EventBus

class IssueCommentActivity : BaseActivity() {

    private var sendTelephone = ""
    private var serveGrade = 0f
    private var speedGrade = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_issue_comment)
        init_title("评价")
    }

    @SuppressLint("SetTextI18n")
    override fun init_title() {
        super.init_title()
        val sendNickName = intent.getStringExtra("sendNickName")
        val sendUserHead = intent.getStringExtra("sendUserHead")
        sendTelephone = intent.getStringExtra("sendTelephone")
        val userGrade = intent.getStringExtra("userGrade")

        issue_name.text = "抢单员：$sendNickName"
        issue_img.loadImage(BaseHttp.baseImg + sendUserHead)
        issue_rating.rating = if (userGrade.isEmpty()) 0f else userGrade.toFloat()

        comment_service.setOnRatingChangeListener { _, rating -> serveGrade = rating }
        comment_speed.setOnRatingChangeListener { _, rating -> speedGrade = rating }
    }

    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.issue_call -> {
                if (sendTelephone.isEmpty()) {
                    showToast("电话号码为空")
                    return
                }

                DialogHelper.showHintDialog(baseContext,
                        "拨打电话",
                        "抢单员电话：$sendTelephone，确定要拨打吗？ ",
                        "取消",
                        "确定",
                        true) {
                    if (it == "right") {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$sendTelephone"))
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }
                }
            }
            R.id.bt_press -> {
                if (serveGrade == 0f) {
                    showToast("请对服务态度进行评价")
                    return
                }

                if (speedGrade == 0f) {
                    showToast("请对送货速度进行评价")
                    return
                }

                OkGo.post<String>(BaseHttp.evaluate_order)
                        .tag(this@IssueCommentActivity)
                        .isMultipart(true)
                        .headers("token", getString("token"))
                        .params("goodsOrderId", intent.getStringExtra("goodsOrderId"))
                        .params("serveGrade", serveGrade)
                        .params("speedGrade", speedGrade)
                        .params("evaluateContent", comment_content.text.trim().toString())
                        .execute(object : StringDialogCallback(baseContext) {

                            override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {
                                
                                showToast(msg)
                                EventBus.getDefault().post(RefreshMessageEvent("评价成功"))
                                ActivityStack.screenManager.popActivities(this@IssueCommentActivity::class.java)
                            }

                        })
            }
        }
    }
}
