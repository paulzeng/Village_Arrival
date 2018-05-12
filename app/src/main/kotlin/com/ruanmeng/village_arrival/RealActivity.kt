package com.ruanmeng.village_arrival

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.view.View
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.compress.Luban
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.lzg.extend.StringDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.maning.imagebrowserlibrary.MNImageBrowser
import com.ruanmeng.base.*
import com.ruanmeng.share.BaseHttp
import com.ruanmeng.share.Const
import com.ruanmeng.utils.ActivityStack
import com.ruanmeng.utils.CommonUtil
import com.ruanmeng.utils.NameLengthFilter
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_real.*
import org.json.JSONObject
import java.io.File

class RealActivity : BaseActivity() {

    private var selectList = ArrayList<LocalMedia>()

    private lateinit var passStatus: String
    private var image_type = 1
    private var image_first = ""
    private var image_second = ""
    private var image_third = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_real)
        init_title("实名认证")

        passStatus = getString("pass")
        when (passStatus) {
            "-1" -> {
                real_expand.expand()
                bt_submit.visibility = View.GONE
                et_name.isFocusable = false
                et_card.isFocusable = false
                getRealData()
            }
            "0" -> {
                real_expand.expand()
                real_hint.text = "认证失败"
                bt_submit.text = "重新认证"
                et_name.isFocusable = false
                et_card.isFocusable = false
                getRealData()
            }
            "1" -> {
                real_expand.expand()
                real_hint.text = "实名认证成功"
                bt_submit.visibility = View.GONE
                et_name.isFocusable = false
                et_card.isFocusable = false
                getRealData()
            }
        }
    }

    override fun init_title() {
        super.init_title()
        bt_submit.setBackgroundResource(R.drawable.rec_bg_d0d0d0)
        bt_submit.isClickable = false

        et_name.filters = arrayOf<InputFilter>(NameLengthFilter(10))
        et_name.addTextChangedListener(this@RealActivity)
        et_card.addTextChangedListener(this@RealActivity)

        real_addresss.setOnClickListener { startActivity<AddressActivity>() }
    }

    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.real_positive -> {
                if (image_first.startsWith(BaseHttp.baseImg)) {
                    MNImageBrowser.showImageBrowser(baseContext, real_positive, 0, arrayListOf(image_first))
                } else {
                    image_type = 1
                    selectPicture(true)
                }
            }
            R.id.real_negative -> {
                if (image_second.startsWith(BaseHttp.baseImg)) {
                    MNImageBrowser.showImageBrowser(baseContext, real_negative, 0, arrayListOf(image_second))
                } else {
                    image_type = 2
                    selectPicture(true)
                }
            }
            R.id.real_hand -> {
                if (image_third.startsWith(BaseHttp.baseImg)) {
                    MNImageBrowser.showImageBrowser(baseContext, real_hand, 0, arrayListOf(image_third))
                } else {
                    image_type = 3
                    selectPicture(false)
                }
            }
            R.id.bt_submit -> {
                if (passStatus == "0") {
                    real_expand.collapse()
                    passStatus = ""
                    bt_submit.text = "认证"
                    et_name.isFocusable = true
                    et_name.isFocusableInTouchMode = true
                    et_card.isFocusable = true
                    et_card.isFocusableInTouchMode = true

                    image_first = ""
                    image_second = ""
                    image_third = ""
                    real_positive.setImageResource(R.mipmap.per_cer01)
                    real_negative.setImageResource(R.mipmap.per_cer02)
                    real_hand.setImageResource(R.mipmap.per_cer03)
                    et_name.setText("")
                    et_card.setText("")
                    return
                }

                if (image_first.isEmpty() || image_second.isEmpty()) {
                    showToast("请上传身份证正反面照")
                    return
                }

                if (image_third.isEmpty()) {
                    showToast("请上传手持证件照")
                    return
                }

                if (!CommonUtil.IDCardValidate(et_card.text.toString())) {
                    showToast("请输入正确的身份证号")
                    et_card.requestFocus()
                    et_card.setText("")
                    return
                }

                getData()
            }
        }
    }

    private fun selectPicture(isCrop: Boolean) {
        PictureSelector.create(this@RealActivity)
                .openGallery(PictureMimeType.ofImage())
                .theme(R.style.picture_customer_style)
                .maxSelectNum(1)
                .minSelectNum(1)
                .imageSpanCount(4)
                .selectionMode(PictureConfig.MULTIPLE)
                .previewImage(true)
                .previewVideo(false)
                .isCamera(true)
                .imageFormat(PictureMimeType.PNG)
                .isZoomAnim(true)
                .setOutputCameraPath(Const.SAVE_FILE)
                .compress(true)
                .glideOverride(160, 160)
                .enableCrop(isCrop)
                .withAspectRatio(4, 3)
                .hideBottomControls(true)
                .compressSavePath(cacheDir.absolutePath)
                .freeStyleCropEnabled(false)
                .circleDimmedLayer(false)
                .showCropFrame(true)
                .showCropGrid(true)
                .isGif(false)
                .openClickSound(false)
                .selectionMedia(selectList.apply { clear() })
                .previewEggs(true)
                .minimumCompressSize(100)
                .isDragFrame(false)
                .forResult(PictureConfig.CHOOSE_REQUEST)
    }

    private fun getRealData() {
        OkGo.post<String>(BaseHttp.certification_info)
                .tag(this@RealActivity)
                .headers("token", getString("token"))
                .execute(object : StringDialogCallback(baseContext) {

                    @SuppressLint("SetTextI18n")
                    override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                        val obj = JSONObject(response.body()).getJSONObject("object") ?: JSONObject()
                        image_first = BaseHttp.baseImg + obj.optString("cardNoImg")
                        image_second = BaseHttp.baseImg + obj.optString("cardNoImg2")
                        image_third = BaseHttp.baseImg + obj.optString("cardNoImg3")

                        real_positive.setImageURL(image_first, R.mipmap.per_cer01)
                        real_negative.setImageURL(image_second, R.mipmap.per_cer02)
                        real_hand.setImageURL(image_third, R.mipmap.per_cer03)

                        et_name.setText(obj.optString("userName"))
                        et_card.setText(obj.optString("cardNo"))

                        val reason = obj.optString("reason")
                        if (passStatus == "0" && reason.isNotEmpty()) real_hint.text = "认证失败：$reason"
                    }

                })
    }

    override fun getData() {
        OkGo.post<String>(BaseHttp.certification_sub)
                .tag(this@RealActivity)
                .isMultipart(true)
                .headers("token", getString("token"))
                .params("userName", et_name.text.toString())
                .params("cardNo", et_card.text.toString().toUpperCase())
                .params("idCardPhoto", File(image_first))
                .params("idCardBackPhoto", File(image_second))
                .params("personPhoto", File(image_third))
                .execute(object : StringDialogCallback(baseContext) {

                    @SuppressLint("SetTextI18n")
                    override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                        showToast(msg)
                        putString("pass", "-1")
                        ActivityStack.screenManager.popActivities(this@RealActivity::class.java)
                    }

                })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PictureConfig.CHOOSE_REQUEST -> {
                    // 图片选择结果回调
                    selectList = PictureSelector.obtainMultipleResult(data) as ArrayList<LocalMedia>
                    // LocalMedia 里面返回三种path
                    // 1.media.getPath(); 为原图path
                    // 2.media.getCutPath();为裁剪后path，需判断media.isCut();是否为true
                    // 3.media.getCompressPath();为压缩后path，需判断media.isCompressed();是否为true
                    // 如果裁剪并压缩了，已取压缩路径为准，因为是先裁剪后压缩的

                    when (image_type) {
                        1 -> {
                            image_first = selectList[0].compressPath
                            real_positive.setImageURL(image_first, R.mipmap.per_cer01)
                            compress(1, selectList[0].compressPath)
                        }
                        2 -> {
                            image_second = selectList[0].compressPath
                            real_negative.setImageURL(image_second, R.mipmap.per_cer02)
                            compress(2, selectList[0].compressPath)
                        }
                        3 -> {
                            image_third = selectList[0].compressPath
                            real_hand.setImageURL(image_third, R.mipmap.per_cer03)
                            compress(3, selectList[0].compressPath)
                        }
                    }
                }
            }
        }
    }

    private fun compress(type: Int, path: String) {
        Flowable.just<List<LocalMedia>>(listOf(LocalMedia().apply { this.path = path }))
                .map {
                    return@map Luban.with(baseContext)
                            .setTargetDir(cacheDir.absolutePath)
                            .ignoreBy(400)
                            .loadLocalMedia(it)
                            .get()
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    when (type) {
                        1 -> image_first = it[0].absolutePath
                        2 -> image_second = it[0].absolutePath
                        3 -> image_third = it[0].absolutePath
                    }
                }
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (et_name.text.isNotBlank()
                && et_card.text.isNotBlank()) {
            bt_submit.setBackgroundResource(R.drawable.rec_bg_blue_shade)
            bt_submit.isClickable = true
        } else {
            bt_submit.setBackgroundResource(R.drawable.rec_bg_d0d0d0)
            bt_submit.isClickable = false
        }
    }
}
