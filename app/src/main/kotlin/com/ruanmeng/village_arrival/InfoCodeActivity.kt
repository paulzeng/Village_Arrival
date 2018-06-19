package com.ruanmeng.village_arrival

import android.graphics.Color
import android.os.Bundle
import cn.bingoogolapple.qrcode.core.BGAQRCodeUtil
import cn.bingoogolapple.qrcode.zxing.QRCodeEncoder
import com.ruanmeng.base.BaseActivity
import com.ruanmeng.base.getString
import com.ruanmeng.share.BaseHttp
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_info_code.*

class InfoCodeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info_code)
        init_title("我的二维码")
    }

    override fun init_title() {
        super.init_title()

        val qr_code = "${BaseHttp.baseImg}forend/register_index.hm?userInfoId=${getString("token")}&type=1"

        Flowable.just(qr_code)
                .map {
                    return@map QRCodeEncoder.syncEncodeQRCode(
                            it,
                            BGAQRCodeUtil.dp2px(baseContext, 200f),
                            Color.BLACK)
                }
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { code_img.setImageBitmap(it) }
    }
}
