package com.ruanmeng.village_arrival

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.flyco.dialog.widget.ActionSheetDialog
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.lzg.extend.StringDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.ruanmeng.base.*
import com.ruanmeng.share.BaseHttp
import com.ruanmeng.share.Const
import com.ruanmeng.utils.CommonUtil
import kotlinx.android.synthetic.main.activity_info.*
import org.json.JSONObject
import java.io.File

class InfoActivity : BaseActivity() {

    private var selectList = ArrayList<LocalMedia>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)
        init_title("个人资料")
    }

    override fun onStart() {
        super.onStart()
        info_name.setRightString(getString("nickName"))
        info_real.setRightString(when (getString("pass")) {
            "-1" -> "认证中"
            "0" -> "未通过"
            "1" -> "已认证"
            else -> "未认证"
        })
    }

    @Suppress("DEPRECATION")
    override fun init_title() {
        super.init_title()
        loadUserHead(getString("userhead"))
        info_phone.setRightString(CommonUtil.phoneReplaceWithStar(getString("mobile")))
        info_gender.setRightString(when (getString("sex")) {
            "0" -> "女"
            else -> "男"
        })

        info_img_ll.setOnClickListener {
            PictureSelector.create(this@InfoActivity)
                    // 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()
                    .openGallery(PictureMimeType.ofImage())
                    // 主题样式(不设置则为默认样式)
                    .theme(R.style.picture_customer_style)
                    // 最大图片选择数量 int
                    .maxSelectNum(1)
                    // 最小选择数量 int
                    .minSelectNum(1)
                    // 每行显示个数 int
                    .imageSpanCount(4)
                    // 多选 or 单选 PictureConfig.MULTIPLE or PictureConfig.SINGLE
                    .selectionMode(PictureConfig.MULTIPLE)
                    // 是否可预览图片 true or false
                    .previewImage(true)
                    // 是否可预览视频 true or false
                    .previewVideo(false)
                    // 是否可播放音频 true or false
                    .enablePreviewAudio(false)
                    // 是否显示拍照按钮 true or false
                    .isCamera(true)
                    // 拍照保存图片格式后缀，默认jpeg
                    .imageFormat(PictureMimeType.PNG)
                    // 图片列表点击 缩放效果 默认true
                    .isZoomAnim(true)
                    // glide 加载图片大小 0~1之间 如设置 .glideOverride()无效
                    // .sizeMultiplier(0.5f)
                    // 自定义拍照保存路径,可不填
                    .setOutputCameraPath(Const.SAVE_FILE)
                    // 是否压缩 true or false
                    .compress(true)
                    // int glide 加载宽高，越小图片列表越流畅，但会影响列表图片浏览的清晰度
                    .glideOverride(160, 160)
                    // 是否裁剪 true or false
                    .enableCrop(true)
                    // int 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
                    .withAspectRatio(1, 1)
                    // 是否显示uCrop工具栏，默认不显示 true or false
                    .hideBottomControls(true)
                    // 压缩图片保存地址
                    .compressSavePath(cacheDir.absolutePath)
                    // 裁剪框是否可拖拽 true or false
                    .freeStyleCropEnabled(false)
                    // 是否圆形裁剪 true or false
                    .circleDimmedLayer(false)
                    // 是否显示裁剪矩形边框 圆形裁剪时建议设为false   true or false
                    .showCropFrame(true)
                    // 是否显示裁剪矩形网格 圆形裁剪时建议设为false    true or false
                    .showCropGrid(true)
                    // 是否显示gif图片 true or false
                    .isGif(false)
                    // 是否开启点击声音 true or false
                    .openClickSound(false)
                    // 是否传入已选图片 List<LocalMedia> list
                    .selectionMedia(selectList.apply { clear() })
                    // 预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中) true or false
                    .previewEggs(true)
                    // 小于100kb的图片不压缩
                    .minimumCompressSize(100)
                    // 是否可拖动裁剪框(固定)
                    .isDragFrame(false)
                    // 结果回调onActivityResult code
                    .forResult(PictureConfig.CHOOSE_REQUEST)
        }
        info_name.setOnClickListener { startActivity<NicknameActivity>() }
        info_gender.setOnClickListener {
            val dialog = ActionSheetDialog(this, arrayOf("男", "女"), null)
            dialog.isTitleShow(false)
                    .lvBgColor(resources.getColor(R.color.white))
                    .dividerColor(resources.getColor(R.color.divider))
                    .dividerHeight(0.5f)
                    .itemTextColor(resources.getColor(R.color.black))
                    .itemHeight(40f)
                    .itemTextSize(15f)
                    .cancelText(resources.getColor(R.color.light))
                    .cancelTextSize(15f)
                    .layoutAnimation(null)
                    .show()
            dialog.setOnOperItemClickL { _, _, position, _ ->

                dialog.dismiss()
                window.decorView.postDelayed({ runOnUiThread { getSexData(position) } }, 300)
            }
        }
        info_real.setOnClickListener { startActivity<RealActivity>() }
        info_qrcode.setOnClickListener { startActivity<InfoCodeActivity>() }
    }

    private fun loadUserHead(path: String) = info_img.loadImage(BaseHttp.baseImg + path)

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

                    if (selectList[0].isCompressed) getHeadData()
                }
            }
        }
    }

    private fun getHeadData() {
        OkGo.post<String>(BaseHttp.userinfo_uploadhead_sub)
                .tag(this@InfoActivity)
                .isMultipart(true)
                .headers("token", getString("token"))
                .params("img", File(selectList[0].compressPath))
                .execute(object : StringDialogCallback(baseContext) {

                    override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                        showToast(msg)
                        val userhead = JSONObject(response.body()).optString("object")
                        putString("userhead", userhead)
                        loadUserHead(userhead)
                    }

                })
    }

    private fun getSexData(position: Int) {
        OkGo.post<String>(BaseHttp.sex_change_sub)
                .tag(this@InfoActivity)
                .headers("token", getString("token"))
                .params("sex", 1 - position)
                .execute(object : StringDialogCallback(baseContext) {

                    override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                        showToast(msg)
                        putString("sex", (1 - position).toString())
                        info_gender.setRightString(when (getString("sex")) {
                            "0" -> "女"
                            else -> "男"
                        })
                    }

                })
    }
}
