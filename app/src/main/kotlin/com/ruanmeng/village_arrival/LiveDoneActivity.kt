package com.ruanmeng.village_arrival

import android.content.Intent
import android.os.Bundle
import com.ruanmeng.base.BaseActivity
import com.ruanmeng.base.gone
import com.ruanmeng.base.startActivity
import com.ruanmeng.utils.ActivityStack
import kotlinx.android.synthetic.main.activity_live_done.*

class LiveDoneActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_done)
        init_title("结果")
    }

    override fun init_title() {
        super.init_title()

        val title = intent.getStringExtra("title") ?: ""
        val cooperationId = intent.getStringExtra("cooperationId") ?: ""

        live_hint.text = title
        when (title) {
            "发布成功" -> live_img.setImageResource(R.mipmap.ass_lab_icon09)
            "发布失败" -> {
                live_img.setImageResource(R.mipmap.ass_lab_icon10)
                bt_look.gone()
            }
        }

        bt_look.setOnClickListener {
            val intent = Intent(baseContext, LiveDetailActivity::class.java)
            intent.putExtra("cooperationId", cooperationId)
            startActivity(intent)
            ActivityStack.screenManager.popActivities(this@LiveDoneActivity::class.java)
        }
    }
}
